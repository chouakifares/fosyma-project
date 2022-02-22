package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendACKBehaviour extends OneShotBehaviour {
    private String receiver;


    public SendACKBehaviour(AbstractDedaleAgent a, String receiver){
        super(a);
        this.receiver = receiver;
    }
    @Override
    public void action() {
        //A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol("token");
        msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));

        //Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        ReceiveTopoBehaviour receiveTopo = new ReceiveTopoBehaviour(this.myAgent);
        ShareTopoBehaviour shareTopo = new ShareTopoBehaviour((AbstractDedaleAgent) this.myAgent, receiver);
//        this.myAgent.addBehaviour(receiveTopo);
//        ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("receiveTopo", receiveTopo);
//        this.myAgent.addBehaviour(shareTopo);
//        ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("shareTopo", shareTopo);
    }
}
