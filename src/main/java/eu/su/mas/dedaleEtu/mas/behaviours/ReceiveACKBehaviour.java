package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class ReceiveACKBehaviour extends SimpleBehaviour {

    private boolean finished = false;
    public static String behaviourName = "receiveACK";

    public ReceiveACKBehaviour(AbstractDedaleAgent myagent){
        super(myagent);
    }



    @Override
    public void action() {
        MessageTemplate templateACK=MessageTemplate.and(
                MessageTemplate.MatchProtocol(SendACKBehaviour.protocol),
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
        ACLMessage msgToken=this.myAgent.receive(templateACK);
        if(msgToken != null) {
            // arreter l'envoi du hello
            ((BaseExplorerAgent)this.myAgent).endBehaviour(SendHelloBehaviour.behaviourName);
            ((BaseExplorerAgent)this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
            if(((BaseExplorerAgent) this.myAgent).getBehaviour(SendACKBehaviour.behaviourName) !=  null) {
                ((BaseExplorerAgent) this.myAgent).endBehaviour(SendACKBehaviour.behaviourName);
            }
            // ajouter l'envoi et la reception de la map
            if(!((BaseExplorerAgent) this.myAgent).isBusy()){
                ((BaseExplorerAgent) this.myAgent).setBusy(true);
                ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                        ShareTopoBehaviour.behaviourName,
                        new ShareTopoBehaviour(
                                (AbstractDedaleAgent) this.myAgent,
                                msgToken.getSender().getLocalName()
                        )
                );
            }
            System.out.println("ReceiveACK:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);

        }
    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent) this.myAgent).getExploBehaviourStatus(behaviourName);
    }

}
