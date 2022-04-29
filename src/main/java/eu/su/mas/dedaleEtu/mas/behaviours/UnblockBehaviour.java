package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import org.glassfish.pfl.basic.fsm.Guard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class UnblockBehaviour extends SimpleBehaviour
{
    private AbstractDedaleAgent myAgent;
    private String senderPosition;
    public static String behaviourName = "unblock";
    private String finalDestination;
    private String lastPosition;
    private boolean goalReached = false;
    private List<String> compatibleNodesToTry  = new ArrayList<>();
    boolean finished = false;
    private boolean isBlocked = false;
    private String lastTry = null;
    private List <String> path;

    private int priority;



    public UnblockBehaviour(AbstractDedaleAgent myAgent, int priority, String senderPosition, String nextPosition, String finalDestination, String leaderName) {
        System.out.println(myAgent.getLocalName() + " receveived blocked from "+leaderName+ " finalDestination is "+finalDestination );
        this.myAgent = myAgent;
        this.finalDestination = finalDestination;
        this.senderPosition = senderPosition;
        lastPosition = null;
        this.priority = priority;
        this.path = ((BaseExplorerAgent)myAgent).getMap().getShortestPath(senderPosition, finalDestination);
        this.path.add(lastPosition);
    }

    @Override
    public void action() {
        if (((BaseExplorerAgent) myAgent).getPhase() == 2) {
            // 1) Retrieve the agent current position
            String myPosition = myAgent.getCurrentPosition();

            // 2) Test if the agent is blocked
            if(lastPosition != null){
                if (myPosition.equals(lastPosition)) {
                    if (compatibleNodesToTry.isEmpty()) { // TODO: priority shouldn't be always 1
                        System.out.println(myAgent.getLocalName() + " blocked at " + myPosition + " I send block to " + lastTry);
                        SendBlockedBehaviour sb = new SendBlockedBehaviour(myAgent, 1, myPosition, lastTry, lastTry, ((((BaseExplorerAgent) myAgent).getCapacity())));
                        ReceiveBlockedBehaviour rb = new ReceiveBlockedBehaviour(myAgent);
                        ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(SendBlockedBehaviour.behaviourName, sb);
                        ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(ReceiveBlockedBehaviour.behaviourName, rb);
                    }
                }
                // 3) if the agent has finished the job.
                else {

                    if (!path.contains(lastPosition)) {
                        //restore moving behaviours, end this behaviour
                        System.out.println(myAgent.getLocalName() + " I unblocked the agent, job is done. " + ((BaseExplorerAgent) myAgent).getOldPhase());
                        ((BaseExplorerAgent) myAgent).endBehaviour(behaviourName);
                        ((BaseExplorerAgent) myAgent).endBehaviour(ReceiveBlockedBehaviour.behaviourName);
                        ((BaseExplorerAgent) myAgent).setPhase(((BaseExplorerAgent) myAgent).getOldPhase());
                        finished = true;
                        return;
                    }
                }

            }
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

                /////////////////////// Update the map while unblocking///////////////////////////////
                // remove the current node from openlist and add it to closedNodes.
                ((BaseExplorerAgent) (this.myAgent)).getMap().addNode(myPosition, MapRepresentation.MapAttribute.closed);

                // get the surrounding nodes and, if not in closedNodes, add them to open nodes.
                String nex = null;
                Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
                while (iter.hasNext()) {
                    String nodeId = iter.next().getLeft();
                    boolean isNewNode = ((BaseExplorerAgent) (this.myAgent)).getMap().addNewNode(nodeId);
                    //the node may exist, but not necessarily the edge
                    if (myPosition != nodeId) {
                        ((BaseExplorerAgent) (this.myAgent)).getMap().addEdge(myPosition, nodeId);
                        if (nex == null && isNewNode) nex = nodeId;
                    }
                }

                // Compute the path to the final destination
                System.out.println(myAgent.getLocalName() + " at " + myPosition + " final dest : " + finalDestination);
                List<String> next = ((BaseExplorerAgent) myAgent).getMap().getShortestPath(myPosition, finalDestination);

                // get the next node from the path
                String nextNodeFromPath = null;
                if (next.size() > 0) {
                    nextNodeFromPath = next.get(0);
                } else {
                    goalReached = true; // I have reached the final Destination.
                }

                if (lastPosition == null || !lastPosition.equals(myPosition)) {
                    compatibleNodesToTry = new ArrayList<String>(); // If the agent has moved, reset the stack of nodes to try.
                    for (Couple<String, List<Couple<Observation, Integer>>> c : lobs) {
                        if (!c.getLeft().equals(myPosition) &&
                                !path.contains(c.getLeft()) &&
                                !compatibleNodesToTry.contains(c.getLeft())
                        ) {
                            compatibleNodesToTry.add(c.getLeft());
                        }
                    }
                }

                // Decide the next move
                String nextMove = null;

                if (compatibleNodesToTry.size() > 0) { // if there still are compatible nodes to try
                    nextMove = compatibleNodesToTry.remove(0);
                } else if (nextNodeFromPath != null) { // else pick a node from the path
                    nextMove = nextNodeFromPath;
                } else { // else pick a random node from the accessible nodes
                    List<String> allPossibleMoves = new ArrayList<String>();
                    for (Couple<String, List<Couple<Observation, Integer>>> c : lobs) {
                        if (!c.getLeft().equals(myPosition)) {
                            allPossibleMoves.add(c.getLeft());
                        }
                    }

                    Random rand = new Random();
                    nextMove = allPossibleMoves.get(rand.nextInt(allPossibleMoves.size()));
                }

                try {
                    this.myAgent.doWait(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(myAgent.getLocalName() + " I'll try to move to " + nextMove);

                // Save the last position to detect locks
                lastPosition = myPosition;
                // Move
                lastTry = nextMove;
                myAgent.moveTo(nextMove);
            }
        }


    @Override
    public boolean done() {
        return !((BaseExplorerAgent)myAgent).getBehaviourStatus(behaviourName);
    }
}
