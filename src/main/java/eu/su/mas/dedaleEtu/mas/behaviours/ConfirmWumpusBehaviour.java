package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.WakerBehaviour;

public class ConfirmWumpusBehaviour extends WakerBehaviour {

    public static String behaviourName = "confirm_wumpus";
    private String nodeToCheck;

    public ConfirmWumpusBehaviour(AbstractDedaleAgent a, long timeout, String positionToCheck) {
        super(a, timeout);
        this.nodeToCheck = positionToCheck;
    }
    protected void onWake(){
        // Si au bout d'un temps timeout il n'a toujours pas reçu de blocked de la position à check, alors on considère qu'il est fermé.
        // On met à jour la map de l'agent
        if (((BaseExplorerAgent)myAgent).getBehaviourStatus(behaviourName + nodeToCheck)) {
            System.out.println("Je n'ai pas reçu de SendBlocked, je considère le noeud " + nodeToCheck + " BLOCKED");
            MapRepresentation myMap = ((BaseExplorerAgent) myAgent).getMap();
            myMap.addNode(nodeToCheck, MapRepresentation.MapAttribute.blocked);
            if (!myMap.hasOpenNode()){
                ((BaseExplorerAgent)myAgent).setExplorationDone(true);
            }
        }

    }
}


