package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

import java.util.ArrayList;
import java.util.List;

public class UnblockBehaviour extends SimpleBehaviour
{
    private int isLeader;
    private AbstractDedaleAgent myAgent;
    private String nextPos;
    private String leaderName;
    private String senderPosition;
    public static String behaviourName = "unblock";
    private MapRepresentation myMap = null;
    private String finalDestination;
    private String lastPosition;
    private boolean goalReached = false;
    private List<String> compatibleNodesToTry  = null;
    private List<String> avoidablePositions = new ArrayList<String>();
    boolean finished = false;
    private String lastTry = "";

    public UnblockBehaviour(AbstractDedaleAgent myAgent, int isLeader, String senderPosition, String nextPosition, String finalDestination, String leaderName) {
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
            myMap = ((BaseExplorerAgent) myAgent).getMap();
            String nextNode = null;
            String myPosition = myAgent.getCurrentPosition();
            if (!lastPosition.equals(myPosition)) {
                if (goalReached) {
                    //restore moving behaviours, end this behaviour
                    System.out.println("I unblocked the agent, job is done.");
                    ((BaseExplorerAgent) myAgent).endBehaviour(behaviourName);
                    ((BaseExplorerAgent) myAgent).endBehaviour(ReceiveBlockedBehaviour.behaviourName);
                    ((BaseExplorerAgent)myAgent).setPhase(((BaseExplorerAgent)myAgent).getOldPhase());
                    finished = true;
                    return;
                }
            }
            else if (compatibleNodesToTry == null){
                System.out.println("i am blocked at "+myPosition);
                return;
            } else if (compatibleNodesToTry.isEmpty()){
                System.out.println(" i am blocked at "+myPosition);
                return;
            }
            System.out.println("Je suis "+myAgent.getLocalName()+" J'exécute UNBLOCK");
            //Compute the path to the leader's current destination
            List<String> next = this.myMap.getShortestPath(myPosition, finalDestination);
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();
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
                        System.out.println(c.getLeft() + ": is an interesting node");
                    }
                }
                return;
            }


            System.out.println("taille des essais" + compatibleNodesToTry.size());
            if (compatibleNodesToTry.size() > 0) {
                nextTry = compatibleNodesToTry.remove(0);
            } else if (goalReached) {
                for (Couple<String, List<Couple<Observation, Integer>>> c : lobs) {
                    if (!c.getLeft().equals(myPosition) &&
                            !c.getLeft().equals(lastPosition) &&
                            !c.getLeft().equals((senderPosition))
                            && !c.getLeft().equals(finalDestination)) {
                        System.out.println(c.getLeft() + ": is a final unblocking move");
    //                    nextTry = c.getLeft();
                        compatibleNodesToTry.add(c.getLeft());
                        return ;
                    }
                }
            } else if (nextNode != null) {
                nextTry = nextNode;
            }
            lastPosition = myPosition;
            lastTry = nextTry;
            System.out.println("I'll try to move to "+ nextTry);
            try {
                this.myAgent.doWait(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myAgent.moveTo(nextTry);
        }
    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent)myAgent).getBehaviourStatus(behaviourName);
    }
}
