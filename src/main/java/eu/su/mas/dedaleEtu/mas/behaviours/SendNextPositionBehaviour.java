package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class SendNextPositionBehaviour extends OneShotBehaviour {

    AID receiver;

    public SendNextPositionBehaviour(AbstractDedaleAgent a, AID receiver){
        super(a);
        this.receiver = receiver;
    }

    @Override
    public void action() {
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        String nextNode = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPathToClosestOpenNode(myPosition).get(0);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-Next-Pos");
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(receiver);
        String sg = nextNode;
        msg.setContent(sg);
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        ((BaseExplorerAgent)this.myAgent).deleteBehaviour("sendNext");
    }
}
