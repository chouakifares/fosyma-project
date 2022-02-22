package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class SendHelloBehaviour extends TickerBehaviour {

    ArrayList<String> receivers;
    private boolean finished;

    public SendHelloBehaviour (final Agent myagent, ArrayList<String> receivers) {

        super(myagent, 2000);
        this.receivers=receivers;
    }

    @Override
    public void onTick() {

        //A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol("sendHello");
        for (String agentName: receivers){
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        //Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
//        this.myAgent.addBehaviour(new ReceiveACKBehaviour((AbstractDedaleAgent) this.myAgent));
//        this.myAgent.doWait(1000);

    }

}
