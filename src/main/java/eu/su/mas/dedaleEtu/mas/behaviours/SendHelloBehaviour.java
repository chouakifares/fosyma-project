package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class SendHelloBehaviour extends SimpleBehaviour {

    ArrayList<String> receivers;
    public static String behaviourName = "sendHello";
    public static String protocol = "sendHello";
    public SendHelloBehaviour (final Agent myagent, ArrayList<String> receivers) {
        super(myagent);
        this.receivers=receivers;
    }

    @Override
    public void action() {
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol(protocol);
        ((BaseExplorerAgent) this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
        ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        for (String agentName: receivers){
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                ReceiveHelloBehaviour.behaviourName,
                new ReceiveHelloBehaviour((AbstractDedaleAgent) this.myAgent)
        );
        ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                RestoreMoveBehaviour.behaviourName,
                new RestoreMoveBehaviour((AbstractDedaleAgent) this.myAgent, 2000)
        );
        ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                RestoreSendHelloBehaviour.behaviourName,
                new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 3000)
        );
        System.out.println("SendHello:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

    }

    public boolean done(){
        return !((BaseExplorerAgent)this.myAgent).getExploBehaviourStatus(behaviourName);
    }
}
