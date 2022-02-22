package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveTopoBehaviour extends OneShotBehaviour {


    public ReceiveTopoBehaviour(Agent myAgent){
        super(myAgent);

    }
    @Override
    public void action() {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
        if (msgReceived!=null) {
            AID sender = msgReceived.getSender();
            MapRepresentation currentMap = ((BaseExplorerAgent)this.myAgent).getMap();
            if(currentMap==null) {
                ((BaseExplorerAgent)this.myAgent).setMap(new MapRepresentation());
            }

            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sgreceived=null;
            try {
                sgreceived = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)msgReceived.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            currentMap.mergeMap(sgreceived);
            ReceiveNextPosBehaviour receiveNext = new ReceiveNextPosBehaviour((AbstractDedaleAgent) this.myAgent);
            SendNextPositionBehaviour sendNext = new SendNextPositionBehaviour((AbstractDedaleAgent) this.myAgent, sender);

            ((BaseExplorerAgent) this.myAgent).addBehaviour(sendNext);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToMap("sendNext", sendNext);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToMap("receiveNext", receiveNext);
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("receiveTopo");



            //String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            //String senderPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            /* merge knowledge
            * */

            /*
            compute sender next position
            compute agent's own next position
            * */
            //String nextNode = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPathToClosestOpenNode(myPosition).get(0);
            //String nextNode_sender = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPathToClosestOpenNode(senderPosition).get(0);


            //((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);


        }
    }

}
