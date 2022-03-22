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

public class ShareTopoBehaviour extends OneShotBehaviour {


    public static String behaviourName = "shareTopo";
    public static String protocol = "SHARE-TOPO";
    private String receiver;
    public ShareTopoBehaviour(final AbstractDedaleAgent a, String receiver) {
        super(a);
        this.receiver=receiver;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(protocol);
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg=((BaseExplorerAgent)this.myAgent).getMap().getSerializableGraph();
        try {
            HashMap temp = new HashMap();
            temp.put("map", sg);
            temp.put("position", ((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            msg.setContentObject(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                ReceiveTopoBehaviour.behaviourName,
                new ReceiveTopoBehaviour(this.myAgent)
        );
        ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        System.out.println("ShareTopo:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

}
