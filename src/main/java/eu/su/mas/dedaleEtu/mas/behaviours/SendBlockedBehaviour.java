package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
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
import java.util.Iterator;
import java.util.List;


public class SendBlockedBehaviour extends OneShotBehaviour {

    public static String behaviourName = "sendBlocked";
    public static String protocol = "sendBlocked";
    private String nextPosition;
    private String finalDestination;
    private int isLeader;
    private int capacity;

    private String myPosition;

    public SendBlockedBehaviour(final Agent myAgent, int isLeader, String myPosition, String nextPosition, String finalDestination, int capacity) {
        super(myAgent);
        this.isLeader = isLeader;
        this.nextPosition = nextPosition;
        this.capacity = capacity;
        this.myPosition = myPosition;
        this.finalDestination = finalDestination;
    }

    @Override
    public void action() {
        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        msg.setProtocol(protocol);
        HashMap tmp = new HashMap();
        tmp.put("nextPosition", nextPosition);
        tmp.put("isLeader", Integer.toString(isLeader));
        tmp.put("senderPosition",myPosition);
        if (finalDestination == null) {
            tmp.put("finalDestination", nextPosition);
        } else {
            tmp.put("finalDestination", finalDestination);
        }
        tmp.put("capacity", Integer.toString(capacity));
        tmp.put("map", ((BaseExplorerAgent)myAgent).getMap().getSerializableGraph());

        try {
            msg.setContentObject(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String agentName: ((BaseExplorerAgent)this.myAgent).getList_agentNames()){
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }

        // Check if there is stench
        List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();
        boolean stenchy = false;
        Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
        while (iter.hasNext()) {
            for (Couple<Observation, Integer> observationIntegerCouple : iter.next().getRight()) {
                if (observationIntegerCouple.getLeft().getName().equals(Observation.STENCH.getName())){
                    stenchy = true;
                }
            }
        }
        // if this position is stenchy
        if (stenchy) {
                if (((BaseExplorerAgent) myAgent).getBehaviourStatus(ConfirmWumpusBehaviour.behaviourName + nextPosition) == null || !((BaseExplorerAgent) myAgent).getBehaviourStatus(ConfirmWumpusBehaviour.behaviourName + nextPosition)) {
                    // Add a Behaviour that will consider the node 'nextPosition' as a blocked node after a certain time (timeout)
                    ConfirmWumpusBehaviour cwb = new ConfirmWumpusBehaviour((AbstractDedaleAgent) myAgent, 2000, nextPosition);
                    ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(ConfirmWumpusBehaviour.behaviourName + nextPosition, cwb);
                }
        }


/*        if (((BaseExplorerAgent) myAgent).getBehaviourStatus(SendHelloBehaviour.behaviourName)){
            ((BaseExplorerAgent) myAgent).endBehaviour(SendHelloBehaviour.behaviourName);
        }
        if (((BaseExplorerAgent) myAgent).getBehaviourStatus(ShareCollectInfoBehaviour.behaviourName)){
            ((BaseExplorerAgent) myAgent).endBehaviour(ShareCollectInfoBehaviour.behaviourName);
        }*/



        System.out.println("Send Blocked: "+this.myAgent.getLocalName()+" : "+myPosition+" -> "+ nextPosition + " ---> "+finalDestination);
        ((BaseExplorerAgent)myAgent).addBehaviourToBehaviourMap(ReceiveBlockedBehaviour.behaviourName, new ReceiveBlockedBehaviour((AbstractDedaleAgent) this.myAgent));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }
}