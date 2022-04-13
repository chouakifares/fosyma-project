package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class SendBlockedBehaviour extends OneShotBehaviour {

    public static String behaviourName = "sendBlocked";
    public static String protocol = "sendBlocked";
    private String nextPosition;
    private String finalDestination;
    private int isLeader;
    private int capacity;

    public SendBlockedBehaviour(final Agent myAgent, int isLeader, String nextPosition, int capacity) {
        super(myAgent);
        this.isLeader = isLeader;
        this.nextPosition = nextPosition;
        this.capacity = capacity;
    }




    @Override
    public void action() {
//        System.out.println(" i am " + myAgent.getLocalName() + " trying to send a message");
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol(protocol);
        HashMap<String, String> tmp = new HashMap<String,String>();
        tmp.put("nextPosition", nextPosition);
        tmp.put("isLeader", Integer.toString(isLeader));
        tmp.put("senderPosition",((BaseExplorerAgent)myAgent).getCurrentPosition());
        tmp.put("finalDestination", ((BaseExplorerAgent)myAgent).getCurrentDest());
        tmp.put("capacity", Integer.toString(capacity));

        try {
            msg.setContentObject(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String agentName: ((BaseExplorerAgent)this.myAgent).getList_agentNames()){
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }

        System.out.println("Send Blocked: "+this.myAgent.getLocalName()+" : "+((BaseExplorerAgent) this.myAgent).getCurrentPosition()+" ----> "+ nextPosition);
        ((BaseExplorerAgent)myAgent).addBehaviourToBehaviourMap(ReceiveBlockedBehaviour.behaviourName, new ReceiveBlockedBehaviour((AbstractDedaleAgent) this.myAgent));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }
}