package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.HashMap;

public class ShareExploInfoBehaviour extends OneShotBehaviour {


    public static String behaviourName = "shareExploInfo";
    public static String protocol = "SHARE-TOPO";
    private String receiver;
    private boolean receiveBack;
    public ShareExploInfoBehaviour(final AbstractDedaleAgent a, String receiver, boolean receiveBack) {
        super(a);
        this.receiver=receiver;
        this.receiveBack = receiveBack;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(protocol);
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
        // TODO: send the map that is destined for the agent receiver.
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> toSend;
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> myCurrentMap = ((BaseExplorerAgent) this.myAgent).getMap().getSerializableGraph();
        SerializableSimpleGraph lastReceived =  ((BaseExplorerAgent)myAgent).getMapReceived(receiver);
        SerializableSimpleGraph lastSent = ((BaseExplorerAgent)myAgent).getMapSent(receiver);
        if (lastReceived != null && lastSent != null){
            SerializableSimpleGraph receiverCurrentMap = MapRepresentation.getGraphUnion(lastSent,lastReceived);
              toSend = MapRepresentation.getGraphDifference(myCurrentMap, receiverCurrentMap);
        } else {
            toSend = myCurrentMap;
        }
        HashMap temp = new HashMap();
        temp.put("map", toSend);
        temp.put("treasure", ((BaseExplorerAgent) this.myAgent).getTreasures());
        temp.put("position", ((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        if(((BaseExplorerAgent) this.myAgent).getAgentBelievedBackpack(receiver)==-1){
            temp.put("BackPack", ((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace());
        }
        try {
            msg.setContentObject(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Update the sent map
        ((BaseExplorerAgent)myAgent).setMapSent(receiver, toSend);

        ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        System.out.println("ShareTopo:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        if(this.receiveBack){
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    ReceiveExploInfoBehaviour.behaviourName,
                    new ReceiveExploInfoBehaviour(this.myAgent)
            );
        }
        else{

            ((BaseExplorerAgent) this.myAgent).endBehaviour(ReceiveExploInfoBehaviour.behaviourName);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(CollectTreasureBehavior.behaviourName, new CollectTreasureBehavior(this.myAgent));
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

}
