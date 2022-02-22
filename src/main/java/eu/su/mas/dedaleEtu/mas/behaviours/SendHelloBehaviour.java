package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class SendHelloBehaviour extends TickerBehaviour {

    ArrayList<String> receivers;

    public SendHelloBehaviour (final Agent myagent, ArrayList<String> receivers) {
        super(myagent, 4000);
    }

    @Override
    public void onTick() {

        //A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol("hello");
        for (String agentName: receivers){
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        //Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }
}
