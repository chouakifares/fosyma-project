package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReceiveExploInfoBehaviour extends SimpleBehaviour {

    private boolean finished;
    public static String behaviourName = "receiveExploInfo";

    public ReceiveExploInfoBehaviour(Agent myAgent){
        super(myAgent);

    }
    @Override
    public void action() {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol(ShareExploInfoBehaviour.protocol),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
        if (msgReceived!=null) {
            MapRepresentation currentMap = ((BaseExplorerAgent)this.myAgent).getMap();
            if(currentMap==null) {
                ((BaseExplorerAgent)this.myAgent).setMap(new MapRepresentation());
            }
            String senderPosition = null;
            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sgreceived=null;
            List<Treasure> senderTreasures = null;
            try {
                if(((HashMap)msgReceived.getContentObject()).containsKey("map")) {
                    sgreceived = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>) ((HashMap) msgReceived.getContentObject()).get("map");
                    //updata map
                    currentMap.mergeMap(sgreceived);
                }
                if(((HashMap)msgReceived.getContentObject()).containsKey("treasure")) {
                     senderTreasures = (List<Treasure>) ((HashMap)msgReceived.getContentObject()).get("treasure");
                    //updata map
                    ((BaseExplorerAgent) this.myAgent).mergeTreasures(senderTreasures);
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            //update backpack
            try {
                if(((HashMap)msgReceived.getContentObject()).containsKey("BackPack")){
                    ((BaseExplorerAgent) this.myAgent).setAgentBelievedBackpack(msgReceived.getSender().getLocalName(),
                            (int) ((Couple)((ArrayList)((HashMap)msgReceived.getContentObject()).get("BackPack")).get(0)).getRight());
                }
            }catch (UnreadableException e) {
                e.printStackTrace();
            }

            try {
                senderPosition = (String) ((HashMap)msgReceived.getContentObject()).get("position");
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            // coordinated exploration
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
                ((BaseExplorerAgent)this.myAgent).endBehaviour(RestoreExploBehaviour.behaviourName);
                ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                        ExploCoopBehaviour.behaviourName,
                        new ExploCoopBehaviour((AbstractDedaleAgent) this.myAgent, ((BaseExplorerAgent) this.myAgent).getMap())
                );
                ((BaseExplorerAgent)this.myAgent).endBehaviour(RestoreSendHelloBehaviour.behaviourName);
                ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                        RestoreSendHelloBehaviour.behaviourName,
                        new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 1500)
                );
                ((BaseExplorerAgent) this.myAgent).setBusy(false);
                System.out.println(this.myAgent.getLocalName() + ((BaseExplorerAgent) this.myAgent).Behaviourmap);
                System.out.println("ReceiveInfo:"+this.myAgent.getLocalName()+":"+((BaseExplorerAgent) this.myAgent).getCurrentPosition());
                ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            }
            else{
                ((BaseExplorerAgent) this.myAgent).explorationDone();
            }
        }
    }

    public boolean done(){return  !((BaseExplorerAgent) this.myAgent).getBehaviourStatus(behaviourName);}

}
