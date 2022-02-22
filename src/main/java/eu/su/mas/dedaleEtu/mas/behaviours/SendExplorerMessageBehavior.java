package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendExplorerMessageBehavior extends SimpleBehaviour {



    MessageTemplate templateHello=MessageTemplate.and(
            MessageTemplate.MatchProtocol("hello"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    ACLMessage msgHello=this.myAgent.receive(templateHello);

    MessageTemplate templateToken=MessageTemplate.and(
            MessageTemplate.MatchProtocol("token"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    ACLMessage msgToken=this.myAgent.receive(templateToken);

    MessageTemplate templateShareTopo=MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    ACLMessage msgShareTopo=this.myAgent.receive(templateShareTopo);

    MessageTemplate templateShareNextPos=MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-next-pos"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    ACLMessage msgShareNextPos=this.myAgent.receive(templateShareNextPos);
/*    if(msgHello != null){

    }
    if(msgToken != null){

    }
    if(msgShareTopo != null){

    }
    if(msgShareNextPos != null){

    }
*/
    @Override
    public void action() {

    }

    @Override
    public boolean done() {
        return false;
    }
}
