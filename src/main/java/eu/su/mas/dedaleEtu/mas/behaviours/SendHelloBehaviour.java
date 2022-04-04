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
        msg.setContent(Integer.toString(((BaseExplorerAgent)this.myAgent).getPhase()));
        //during exploration
        if(((BaseExplorerAgent)this.myAgent).getPhase()==0){
            ((BaseExplorerAgent) this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            for (String agentName: receivers){
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    ReceiveHelloBehaviour.behaviourName,
                    new ReceiveHelloBehaviour((AbstractDedaleAgent) this.myAgent)
            );
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    RestoreExploBehaviour.behaviourName,
                    new RestoreExploBehaviour((AbstractDedaleAgent) this.myAgent, 1000)
            );
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    RestoreSendHelloBehaviour.behaviourName,
                    new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 3000)
            );
            System.out.println("SendHello:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        }
        else if(((BaseExplorerAgent)this.myAgent).getPhase()==1){
            ((BaseExplorerAgent) this.myAgent).endBehaviour(CollectTreasureBehavior.behaviourName);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    ReceiveHelloBehaviour.behaviourName,
                    new ReceiveHelloBehaviour((AbstractDedaleAgent) this.myAgent)
            );
            for (String agentName: receivers){
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    RestoreCollectBehaviour.behaviourName,
                    new RestoreCollectBehaviour((AbstractDedaleAgent) this.myAgent, 1000)
            );
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    RestoreSendHelloBehaviour.behaviourName,
                    new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 3000)
            );
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

    public boolean done(){
        return !((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
    }
}
