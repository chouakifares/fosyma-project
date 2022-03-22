package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.WakerBehaviour;

import java.util.ArrayList;

public class RestoreSendHelloBehaviour extends WakerBehaviour {

    public static String behaviourName = "restoreHello";

    public RestoreSendHelloBehaviour(AbstractDedaleAgent a, long timeout) {
        super(a, timeout);
    }

    protected void onWake() {
        if (!((BaseExplorerAgent) this.myAgent).isBusy() && ((BaseExplorerAgent) this.myAgent).getExploBehaviourStatus(behaviourName)){
            SendHelloBehaviour b = new SendHelloBehaviour(this.myAgent, (ArrayList<String>) ((BaseExplorerAgent) this.myAgent).getList_agentNames());
            ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(SendHelloBehaviour.behaviourName, b);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        }
    }
}