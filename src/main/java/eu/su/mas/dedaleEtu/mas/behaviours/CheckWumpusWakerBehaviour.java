package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.WakerBehaviour;

public class CheckWumpusWakerBehaviour extends WakerBehaviour {

    public static String behaviourName = "checkWaker";

    private AbstractDedaleAgent myAgent;


    public CheckWumpusWakerBehaviour(AbstractDedaleAgent a, long timeout){
        super(a,timeout);
        this.myAgent = a;
    }


    public void onWake(){

        // This behaviour simply launchs the behaviour that is responsible of checking if the blocked nodes are still blocked.
        System.out.println("IT'S TIME ! Checking behaviour launched !");
        CheckWumpusBehaviour cwb = new CheckWumpusBehaviour(myAgent);
        ((BaseExplorerAgent)myAgent).setPhase(3);
        ((BaseExplorerAgent)myAgent).addBehaviourToBehaviourMap(CheckWumpusBehaviour.behaviourName, cwb);

    }
}
