package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
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
        MessageTemplate templateHello= MessageTemplate.MatchProtocol("sendHello");
        ACLMessage msgHello=this.myAgent.receive(templateHello);

        if(msgHello != null) {
           ((BaseExplorerAgent)this.myAgent).deleteBehaviour("sendHello");
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("move");
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("receiveHello");
            this.myAgent.addBehaviour(new ReceiveACKBehaviour((AbstractDedaleAgent) this.myAgent));
            this.myAgent.addBehaviour(new SendACKBehaviour((AbstractDedaleAgent) this.myAgent, msgHello.getSender().toString()));

        }
    }

    @Override
    public boolean done() {
        return false;
    }


}
