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

public class UnblockBehaviour extends SimpleBehaviour
{
    private int isLeader;
    private AbstractDedaleAgent myAgent;
    private String nextPos;
    private String leaderName;
    private String senderPosition;
    public static String behaviourName = "unblock";
    private String finalDestination;
    private String lastPosition;
    private boolean goalReached = false;
    private List<String> compatibleNodesToTry  = new ArrayList<>();
    private List<String> avoidablePositions = new ArrayList<String>();
    boolean finished = false;
    private boolean isBlocked = false;
    private String lastTry = null;

    public UnblockBehaviour(AbstractDedaleAgent myAgent, int isLeader, String senderPosition, String nextPosition, String finalDestination, String leaderName) {
        System.out.println(myAgent.getLocalName() + " receveived blocked from "+leaderName+ " finalDestination is "+finalDestination );
        this.myAgent = myAgent;
        this.isLeader = isLeader;
        this.nextPos = nextPosition;
        this.finalDestination = finalDestination;
        this.leaderName = leaderName;
        this.senderPosition = senderPosition;
        lastPosition = senderPosition;

    }

    @Override
    public void action() {

        if (((BaseExplorerAgent)myAgent).getPhase()==2){
                    try {
                        this.myAgent.doWait(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String nextNode = null;
                    String myPosition = myAgent.getCurrentPosition();
                    if (myPosition.equals(lastPosition)){
                        isBlocked = true;
                        if (compatibleNodesToTry.isEmpty()){
                            System.out.println(myAgent.getLocalName() + " blocked at " + myPosition + " I send block to "+lastTry);
                            ((BaseExplorerAgent)myAgent).setCurrentDest(lastTry);
                            SendBlockedBehaviour sb = new SendBlockedBehaviour(myAgent, isLeader, lastTry, ((((BaseExplorerAgent) myAgent).getCapacity())));
                            ReceiveBlockedBehaviour rb = new ReceiveBlockedBehaviour(myAgent);
                            ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(SendBlockedBehaviour.behaviourName, sb);
                            ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(ReceiveBlockedBehaviour.behaviourName, rb);
                        }
                      }
                    else {
                        if (lastPosition.equals(finalDestination)) {
                            //restore moving behaviours, end this behaviour
                            System.out.println("I unblocked the agent, job is done.");
                            ((BaseExplorerAgent) myAgent).endBehaviour(behaviourName);
                            ((BaseExplorerAgent) myAgent).endBehaviour(ReceiveBlockedBehaviour.behaviourName);
                            ((BaseExplorerAgent) myAgent).setPhase(((BaseExplorerAgent) myAgent).getOldPhase());
                            finished = true;
                            return;
                        }
                    }
                    List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

                    ///////// MAJ de la map///////////////////////////////
                    //1) remove the current node from openlist and add it to closedNodes.
                        ((BaseExplorerAgent)(this.myAgent)).getMap().addNode(myPosition, MapRepresentation.MapAttribute.closed);

                    //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
                    String nex = null;
                    Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
                    while (iter.hasNext()) {
                        String nodeId = iter.next().getLeft();
                        boolean isNewNode = ((BaseExplorerAgent)(this.myAgent)).getMap().addNewNode(nodeId);
                        //the node may exist, but not necessarily the edge
                        if (myPosition != nodeId) {
                            ((BaseExplorerAgent)(this.myAgent)).getMap().addEdge(myPosition, nodeId);
                            if (nex == null && isNewNode) nex = nodeId;
                        }
                    }
                    //////////////////////////////////////////////
                    if (!((BaseExplorerAgent) this.myAgent).getMap().hasOpenNode()) {
                        ((BaseExplorerAgent) this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
                        ((BaseExplorerAgent)myAgent).setExplorationDone(true);
                    }



            //Compute the path to the leader's current destination
                    System.out.println(myAgent.getLocalName() + " at "+ myPosition +" final dest : "+ finalDestination);
                    List<String> next = ((BaseExplorerAgent)myAgent).getMap().getShortestPath(myPosition, finalDestination);
                    if (next.size() > 0) {
                        nextNode = next.get(0);
                    }
                    else { // i am already in the final destination
                        goalReached = true;
                    }
                    String nextTry = null;
                    if (!lastPosition.equals(myPosition)){
                        compatibleNodesToTry = new ArrayList<String>();
                        for (Couple<String, List<Couple<Observation, Integer>>> c : lobs) {
                            if (!c.getLeft().equals(myPosition) &&
                                    !c.getLeft().equals(lastPosition) &&
                                    !compatibleNodesToTry.contains(c.getLeft()) &&
                                    !c.getLeft().equals((senderPosition)) &&
                                    !c.getLeft().equals(nextNode)) {
                                compatibleNodesToTry.add(c.getLeft());
                            }
                        }
                        if (nextNode != null) {
                            compatibleNodesToTry.add(nextNode);
                        }
                    }



                    if (compatibleNodesToTry.size() > 0) {
                        nextTry = compatibleNodesToTry.remove(0);
                    } else if (goalReached) {
                        for (Couple<String, List<Couple<Observation, Integer>>> c : lobs) {
                            if (!c.getLeft().equals(myPosition) &&
                                    !c.getLeft().equals(lastPosition) &&
                                    !c.getLeft().equals((senderPosition)) &&
                                  !compatibleNodesToTry.contains(c.getLeft())&&
                                    !c.getLeft().equals(finalDestination)) {
//                                System.out.println(c.getLeft() + ": is a final unblocking move");
                                compatibleNodesToTry.add(c.getLeft());
                                ((BaseExplorerAgent)this.myAgent).setCurrentDest(c.getLeft());

                            }
                        }
                    } else if (nextNode != null) {
                        nextTry = nextNode;
                    }

                    lastPosition = myPosition;
                    if (nextTry==null){
                        nextTry = lastTry;
                    } else {
                        lastTry = nextTry;
                    }



                    System.out.println("I'll try to move to "+ nextTry);
                    myAgent.moveTo(nextTry);
        }
    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent)myAgent).getBehaviourStatus(behaviourName);
    }
}
