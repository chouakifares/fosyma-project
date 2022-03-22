package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.WakerBehaviour;

public class RestoreMoveBehaviour extends WakerBehaviour {

    public static String behaviourName = "restoreMove";
    public RestoreMoveBehaviour(AbstractDedaleAgent a, long timeout) {
        super(a, timeout);
    }
    protected void onWake(){
        if (!((BaseExplorerAgent) this.myAgent).isBusy() && ((BaseExplorerAgent) this.myAgent).getExploBehaviourStatus(behaviourName)) {
            ExploCoopBehaviour b = new ExploCoopBehaviour((AbstractDedaleAgent) this.myAgent, ((BaseExplorerAgent)this.myAgent).getMap());
            ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(ExploCoopBehaviour.behaviourName, b);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        }
    }
}
