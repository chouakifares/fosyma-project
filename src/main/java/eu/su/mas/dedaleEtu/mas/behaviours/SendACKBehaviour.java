package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendACKBehaviour extends OneShotBehaviour {
    private String receiver;
    public static String behaviourName = "sendACK" ;
    public static String protocol = "myACKToken";
    public SendACKBehaviour(AbstractDedaleAgent a, String receiver){
        super(a);
        this.receiver = receiver;
    }
    @Override
    public void action() {
        ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
        if(((BaseExplorerAgent)this.myAgent).getBehaviour(ReceiveACKBehaviour.behaviourName)== null ||
                !((BaseExplorerAgent)this.myAgent).getExploBehaviourStatus(ReceiveACKBehaviour.behaviourName)) {
            ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                    ReceiveACKBehaviour.behaviourName,
                    new ReceiveACKBehaviour((AbstractDedaleAgent) this.myAgent)
            );
        }
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol(protocol);
        msg.addReceiver(new AID(this.receiver,AID.ISLOCALNAME));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        System.out.println("SendACK:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
    }
}
