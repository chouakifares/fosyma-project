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
            String samePhase = msgToken.getContent();
            // arreter l'envoi du hello
            ((BaseExplorerAgent)this.myAgent).endBehaviour(SendHelloBehaviour.behaviourName);
            if(((BaseExplorerAgent) this.myAgent).getBehaviour(SendACKBehaviour.behaviourName) !=  null) {
                ((BaseExplorerAgent) this.myAgent).endBehaviour(SendACKBehaviour.behaviourName);
            }

            if(samePhase.compareTo("2")==0){
                if(((BaseExplorerAgent)this.myAgent).getPhase()==0){
                    //arreter les mouvement de l'agent
                    ((BaseExplorerAgent)this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
                    // ajouter l'envoi et la reception de la map
                    if(!((BaseExplorerAgent) this.myAgent).isBusy()){
                        ((BaseExplorerAgent) this.myAgent).setBusy(true);
                        ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                                ShareExploInfoBehaviour.behaviourName,
                                new ShareExploInfoBehaviour(
                                        (AbstractDedaleAgent) this.myAgent,
                                        msgToken.getSender().getLocalName(), true
                                )
                        );
                    }
                }
                else if(((BaseExplorerAgent)this.myAgent).getPhase()==1){
                    //arreter les mouvement de l'agent
                    ((BaseExplorerAgent)this.myAgent).endBehaviour(CollectTreasureBehavior.behaviourName);
                    if(!((BaseExplorerAgent) this.myAgent).isBusy()){
                        ((BaseExplorerAgent) this.myAgent).setBusy(true);
                        ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                                ShareCollectInfoBehaviour.behaviourName,
                                new ShareCollectInfoBehaviour(
                                        (AbstractDedaleAgent) this.myAgent,
                                        msgToken.getSender().getLocalName()
                                )
                        );
                    }
                }
            }
            //agents not on the same phase
            else {
                //current agent is on collect phase share his map with the agent
                if (((BaseExplorerAgent) this.myAgent).getPhase() == 1) {
                    ((BaseExplorerAgent) this.myAgent).endBehaviour(CollectTreasureBehavior.behaviourName);
                    // ajouter l'envoi et la reception de la map
                    if (!((BaseExplorerAgent) this.myAgent).isBusy()) {
                        ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                                ShareExploInfoBehaviour.behaviourName,
                                new ShareExploInfoBehaviour(
                                        (AbstractDedaleAgent) this.myAgent,
                                        msgToken.getSender().getLocalName(),false
                                )
                        );
                    }
                }
                // current agent is still exploring waits for the other agent to share his map with it
                else if(((BaseExplorerAgent) this.myAgent).getPhase() == 0){
                    ((BaseExplorerAgent) this.myAgent).endBehaviour(ExploCoopBehaviour.behaviourName);
                    if (!((BaseExplorerAgent) this.myAgent).isBusy()) {
                        ((BaseExplorerAgent) this.myAgent).setBusy(true);
                        ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                                ReceiveExploInfoBehaviour.behaviourName,
                                new ReceiveExploInfoBehaviour(this.myAgent)
                        );
                    }
                }
            }
            System.out.println("ReceiveACK:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);

        }
    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent) this.myAgent).getBehaviourStatus(behaviourName);
    }

}
