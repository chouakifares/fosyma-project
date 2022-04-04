package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.WakerBehaviour;

public class RestoreCollectBehaviour extends WakerBehaviour {

    public static String behaviourName = "restoreCollect";
    public RestoreCollectBehaviour(AbstractDedaleAgent a, long timeout) {
        super(a, timeout);
    }
    protected void onWake(){
        if (!((BaseExplorerAgent) this.myAgent).isFull() && ((BaseExplorerAgent) this.myAgent).getBehaviourStatus(behaviourName)) {
            CollectTreasureBehavior b = new CollectTreasureBehavior(this.myAgent);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(CollectTreasureBehavior.behaviourName, b);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        }
    }
}
