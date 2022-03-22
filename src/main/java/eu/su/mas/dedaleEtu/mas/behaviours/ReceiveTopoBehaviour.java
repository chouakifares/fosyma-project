package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;

public class ReceiveTopoBehaviour extends SimpleBehaviour {

    private boolean finished;
    public static String behaviourName = "receiveTopo";

    public ReceiveTopoBehaviour(Agent myAgent){
        super(myAgent);

    }
    @Override
    public void action() {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol(ShareTopoBehaviour.protocol),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
        if (msgReceived!=null) {
            MapRepresentation currentMap = ((BaseExplorerAgent)this.myAgent).getMap();
            if(currentMap==null) {
                ((BaseExplorerAgent)this.myAgent).setMap(new MapRepresentation());
            }
            String senderPosition = null;
            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sgreceived=null;
            try {
                sgreceived = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)((HashMap)msgReceived.getContentObject()).get("map");
                senderPosition = (String) ((HashMap)msgReceived.getContentObject()).get("position");
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            currentMap.mergeMap(sgreceived);
            if(((BaseExplorerAgent) this.myAgent).getMap().hasOpenNode()){
                Couple<String , Integer> myNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getClosestOpenNode(((BaseExplorerAgent) this.myAgent).getCurrentPosition());
                Couple<String , Integer>  senderNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getClosestOpenNode(senderPosition);
                if(myNewDest.getLeft() == senderNewDest.getLeft()) {
                    if (myNewDest.getRight() > senderNewDest.getRight()) {
                        if((myNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getSecondClosestOpenNode(((BaseExplorerAgent) this.myAgent).getCurrentPosition()))!=null){
                            ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                        }else{
                            myNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getClosestOpenNode(((BaseExplorerAgent) this.myAgent).getCurrentPosition());
                            ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                        }
                    } else if (myNewDest.getRight() == senderNewDest.getRight()) {
                        if (this.myAgent.getLocalName().compareTo(msgReceived.getSender().getLocalName()) > 0) {
                            if((myNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getSecondClosestOpenNode(((BaseExplorerAgent) this.myAgent).getCurrentPosition()))!=null){
                                ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                            }else{
                                myNewDest = ((BaseExplorerAgent) this.myAgent).getMap().getClosestOpenNode(((BaseExplorerAgent) this.myAgent).getCurrentPosition());
                                ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                            }
                        }else{
                            ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                        }
                    } else {
                        ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                    }
                }else{
                    ((BaseExplorerAgent) this.myAgent).setCurrentDest(myNewDest.getLeft());
                }
                ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                        ExploCoopBehaviour.behaviourName,
                        new ExploCoopBehaviour((AbstractDedaleAgent) this.myAgent, ((BaseExplorerAgent) this.myAgent).getMap())
                );
                ((BaseExplorerAgent)this.myAgent).endBehaviour(RestoreSendHelloBehaviour.behaviourName);
                ((BaseExplorerAgent) this.myAgent).addBehaviourToExploBehaviourMap(
                        RestoreSendHelloBehaviour.behaviourName,
                        new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 1000)
                );
                ((BaseExplorerAgent)this.myAgent).endBehaviour(RestoreMoveBehaviour.behaviourName);
                ((BaseExplorerAgent) this.myAgent).setBusy(false);
                ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
                System.out.println(this.myAgent.getLocalName() + ((BaseExplorerAgent) this.myAgent).ExploBehaviourmap);
                System.out.println("ReceiveTOPO:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
            }
        }
    }

    public boolean done(){return  !((BaseExplorerAgent) this.myAgent).getExploBehaviourStatus(behaviourName);}

}
