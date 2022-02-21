package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Timer;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveHelloBehaviour extends SimpleBehaviour {

    private boolean finished = false;


    public ReceiveHelloBehaviour(AbstractDedaleAgent myagent){
        super(myagent);
    }



    @Override
    public void action() {
        MessageTemplate templateHello=MessageTemplate.and(
                MessageTemplate.MatchProtocol("hello"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgHello=this.myAgent.receive(templateHello);

        if(msgHello != null) {
            this.myAgent.addBehaviour(new SendACKBehaviour((AbstractDedaleAgent) this.myAgent, msgHello.getSender().toString()));
            this.myAgent.doWait(1000);
        }
    }

    @Override
    public boolean done() {
        return false;
    }


}
