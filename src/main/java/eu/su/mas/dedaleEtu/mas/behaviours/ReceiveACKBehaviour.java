package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
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
            // arreter l'envoi du hello
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("sendHello");
            ((BaseExplorerAgent)this.myAgent).deleteBehaviour("move");
            // ajouter l'envoi et la reception de la map
            ReceiveTopoBehaviour receiveTopo = new ReceiveTopoBehaviour(this.myAgent);
            ShareTopoBehaviour shareTopo = new ShareTopoBehaviour((AbstractDedaleAgent) this.myAgent, msgToken.getSender().toString());

//            this.myAgent.addBehaviour(receiveTopo);
//            ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("receiveTopo", receiveTopo);
//            this.myAgent.addBehaviour(shareTopo);
//            ((BaseExplorerAgent)this.myAgent).addBehaviourToMap("shareTopo", shareTopo);
        }
    }

    @Override
    public boolean done() {
        return false;
    }

}
