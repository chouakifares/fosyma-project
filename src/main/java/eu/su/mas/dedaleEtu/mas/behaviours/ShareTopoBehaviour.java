package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import org.glassfish.pfl.basic.fsm.Guard;

import java.io.IOException;

public class ShareTopoBehaviour extends OneShotBehaviour {

    String receiver;

    public ShareTopoBehaviour(final AbstractDedaleAgent a, String receiver) {
        super(a);
        this.receiver=receiver;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-TOPO");
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg=((BaseExplorerAgent)this.myAgent).getMap().getSerializableGraph();
        try {
            msg.setContentObject(sg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        ((BaseExplorerAgent)this.myAgent).deleteBehaviour("shareTopo");
    }
}
