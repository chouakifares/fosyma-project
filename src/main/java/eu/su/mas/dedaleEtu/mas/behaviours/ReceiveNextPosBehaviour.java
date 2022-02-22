package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

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

            Behaviour receiveHello = new ReceiveHelloBehaviour((AbstractDedaleAgent) this.myAgent);
            Behaviour helloBehaviour = new SendHelloBehaviour((AbstractDedaleAgent)this.myAgent, (ArrayList<String>)((BaseExplorerAgent) this.myAgent).getList_agentNames());
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("receiveNext");
            ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("sendHello", helloBehaviour);
            ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("receiveHello",receiveHello);
            ((BaseExplorerAgent) this.myAgent).moveTo(nextNode);

            Behaviour randomWalk = new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent);
            ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("move", randomWalk);
            this.myAgent.addBehaviour(randomWalk);
        }
    }
}

