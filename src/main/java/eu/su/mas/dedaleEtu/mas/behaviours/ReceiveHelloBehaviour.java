package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveHelloBehaviour extends SimpleBehaviour {

    private boolean finished = false;
    public static String behaviourName = "receiveHello";
    public ReceiveHelloBehaviour(AbstractDedaleAgent myagent){
        super(myagent);
    }



    @Override
    public void action() {
        MessageTemplate templateHello= MessageTemplate.MatchProtocol(SendHelloBehaviour.protocol);
        ACLMessage msgHello=this.myAgent.receive(templateHello);

        if(msgHello != null) {
            ((BaseExplorerAgent)this.myAgent).endBehaviour(SendHelloBehaviour.behaviourName);
            ((BaseExplorerAgent)this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                    SendACKBehaviour.behaviourName,
                    new SendACKBehaviour(
                            (AbstractDedaleAgent) this.myAgent,
                            msgHello.getSender().getLocalName()
                    )
            );
            System.out.println("ReceiveHello:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        }
    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent)this.myAgent).getExploBehaviourStatus(behaviourName);
    }

}
