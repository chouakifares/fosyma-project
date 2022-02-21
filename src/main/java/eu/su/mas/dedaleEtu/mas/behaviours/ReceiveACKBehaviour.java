package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveACKBehaviour extends SimpleBehaviour {

    private boolean finished = false;


    public ReceiveACKBehaviour(AbstractDedaleAgent myagent){
        super(myagent);
    }



    @Override
    public void action() {
        MessageTemplate templateHello=MessageTemplate.and(
                MessageTemplate.MatchProtocol("token"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgToken=this.myAgent.receive(templateHello);

        if(msgToken != null) {
            // Lancer Share Behaviour
            this.myAgent.addBehaviour(new ShareTopoBehaviour((AbstractDedaleAgent) this.myAgent, msgToken.getSender().toString()));
        }
    }

    @Override
    public boolean done() {
        return false;
    }

}
