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
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg=((BaseExplorerAgent)this.myAgent).getMap().getSerializableGraph();
        HashMap temp = new HashMap();
        temp.put("map", sg);
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
