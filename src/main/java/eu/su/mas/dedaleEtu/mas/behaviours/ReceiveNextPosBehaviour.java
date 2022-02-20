package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveNextPosBehaviour extends OneShotBehaviour {

    public ReceiveNextPosBehaviour(AbstractDedaleAgent a){
        super(a);
    }


    @Override
    public void action() {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-Next-Pos"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
        if (msgReceived!=null) {
            String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            String nextNode = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPathToClosestOpenNode(myPosition).get(0);
            String sg = null;
            sg = msgReceived.getContent();
            if (nextNode.compareTo(sg) != 0){
                    nextNode = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPathToClosestOpenNode(myPosition).get(1);
            }

            //this.myAgent.addBehaviour(SayHelloBehaviour);
            //this.myAgent.addBehaviour(ReceiveHelloBehavior);

            ((BaseExplorerAgent) this.myAgent).moveTo(nextNode);

        }
    }
}

