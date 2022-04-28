package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CheckWumpusBehaviour extends SimpleBehaviour {
    public static String behaviourName = "check_wumpus";
    private boolean finished = false;

    private List<String> blockedNodes = null;
    private String currentNode = null;

    private String lastPosition = null;
    private String nextTry = null;

    private int blockedLimit = 3;
    private int waitLimit = 6;
    private int nbBlocked = 0;

    public CheckWumpusBehaviour(AbstractDedaleAgent a){
        super(a);
        System.out.println("I'll go check the blocked nodes.");
    }

    @Override
    public void action() {
        if(((BaseExplorerAgent)this.myAgent).getPhase() == 3) {

            MapRepresentation myMap = ((BaseExplorerAgent) myAgent).getMap();
            if (blockedNodes == null) {  // If it's the first time this behaviour is called
                // Retrieve the list of blocked nodes
                blockedNodes = new ArrayList<String>();
                for (Iterator<Node> it = myMap.getGraph().nodes().iterator(); it.hasNext(); ) {
                    Node n = it.next();
                    if (n.getAttribute("ui.class").toString().equals(MapRepresentation.MapAttribute.blocked.toString())) {
                        blockedNodes.add(n.getId());
                    }
                }
            }

            if (blockedNodes.isEmpty() && currentNode == null) { // If there are no remaining nodes to check, end this behaviour.
                System.out.println("No remaining nodes to check, check done! I'll comeback in 20 s tho");
                finished = true;

                // IF THE CHECKING BEHAVIOUR HAS DISCOVERED NEW OPEN NODES, GO BACK AGAIN TO EXPLORATION
                if (myMap.hasOpenNode()){
                    ((BaseExplorerAgent) this.myAgent).setPhase(0);
                    // if the exploration behaviour is not active, set it to true
                    if (!((BaseExplorerAgent)myAgent).getBehaviourStatus(ExploCoopBehaviour.behaviourName)){
                        ((BaseExplorerAgent)myAgent).setExplorationDone(false);
                        ((BaseExplorerAgent)myAgent).addBehaviourToBehaviourMap(ExploCoopBehaviour.behaviourName, (Behaviour) ((BaseExplorerAgent)myAgent).getBehaviour(ExploCoopBehaviour.behaviourName).get("behavior"));
                    }

                } else {
                    ((BaseExplorerAgent) this.myAgent).setPhase(((BaseExplorerAgent) this.myAgent).getOldPhase());
                }


                ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(CheckWumpusWakerBehaviour.behaviourName,new CheckWumpusWakerBehaviour((AbstractDedaleAgent) myAgent,20000));
                ((BaseExplorerAgent) myAgent).endBehaviour(behaviourName);
                return;
            }

            if (currentNode == null) {
                currentNode = blockedNodes.remove(0);
            }

            // Get the agent's current position
            String myPosition = ((BaseExplorerAgent) myAgent).getCurrentPosition();


            if (lastPosition != null) {
                if (myPosition.equals(lastPosition)) { // TODO: add a send blocked and a wait limit
                    nbBlocked++;
                    if (nbBlocked == blockedLimit) { // i reached the blocked limit, i consider myself blocked
                        System.out.println("Tired of waiting, i change my destination");
                        currentNode = null;
                        return;
                    }
                } else {
                    // i moved to a different node so i'm not blocked
                    nbBlocked = 0;
                }
            }


            /////////////////////// Update the map while unblocking///////////////////////////////
            // remove the current node from openlist and add it to closedNodes.
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

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
            ////////////////////////////// END OF MAP UPDATE ///////////////////////////////////////////


            // Compute the path to the final destination
            List<String> path = ((BaseExplorerAgent) myAgent).getMap().getShortestPath(myPosition, currentNode);


            try {
                this.myAgent.doWait(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // get the next node from the path
            if (path.size() > 0) {
                nextTry = path.get(0);
                System.out.println("I'm in check i'll try to move to "+ nextTry);
                lastPosition = myPosition;
                ((BaseExplorerAgent) myAgent).moveTo(nextTry);
            } else { // i've reached the destination
                currentNode = null;
                return;
            }


        }
    }

    @Override
    public boolean done() {
        return finished || !((BaseExplorerAgent)myAgent).getBehaviourStatus(behaviourName);
    }
}
