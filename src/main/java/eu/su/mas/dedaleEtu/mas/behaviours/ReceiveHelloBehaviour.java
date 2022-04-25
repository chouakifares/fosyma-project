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
            boolean samePhase = ( Integer.parseInt(msgHello.getContent())  == ((BaseExplorerAgent)this.myAgent).getPhase());


            //make the agents communicate even if they're not on the same phase
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    SendACKBehaviour.behaviourName,
                    new SendACKBehaviour(
                            (AbstractDedaleAgent) this.myAgent,
                            msgHello.getSender().getLocalName(),
                            samePhase
                    )
            );
            //stop the agent
            if(((BaseExplorerAgent)this.myAgent).getPhase()==0)
                ((BaseExplorerAgent)this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
            else if(((BaseExplorerAgent)this.myAgent).getPhase()==1)
                ((BaseExplorerAgent)this.myAgent).endBehaviour(CollectTreasureBehavior.behaviourName);

            System.out.println("ReceiveHello:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
        }

    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
    }

}
