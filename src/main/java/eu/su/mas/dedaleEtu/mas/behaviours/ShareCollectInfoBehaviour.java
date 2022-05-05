package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.HashMap;

public class ShareCollectInfoBehaviour extends OneShotBehaviour {


    public static String behaviourName = "shareCollectInfo";
    public static String protocol = "SHARE-COLL";
    private String receiver;
    public ShareCollectInfoBehaviour(final AbstractDedaleAgent a, String receiver) {
        super(a);
        this.receiver=receiver;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(protocol);
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
        Observation type = ((BaseExplorerAgent) this.myAgent).getMyType();
        HashMap temp = new HashMap();


        temp.put("prop", ((BaseExplorerAgent) this.myAgent).getProp());
        temp.put("Treasures", ((BaseExplorerAgent) this.myAgent).getTreasures());
        temp.put("type", type);
        temp.put("dest", ((BaseExplorerAgent) this.myAgent).getCurrentDest());
        temp.put("position", ((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        if(type == Observation.ANY_TREASURE)
            temp.put("BackPack", ((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace());
        else if(type == Observation.GOLD)
            temp.put("BackPack", ((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight());
        else if(type == Observation.DIAMOND)
            temp.put("BackPack", ((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace().get(1).getRight());
        try {
            msg.setContentObject(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                ReceiveCollectInfoBehaviour.behaviourName,
                new ReceiveCollectInfoBehaviour(this.myAgent)
        );
        ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        System.out.println("ShareCollect:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
        System.out.println("Next Position:"+((BaseExplorerAgent)this.myAgent).getCurrentDest());
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

}
