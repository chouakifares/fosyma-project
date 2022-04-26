package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static eu.su.mas.dedale.env.Observation.DIAMOND;
import static eu.su.mas.dedale.env.Observation.GOLD;

public class ReceiveCollectInfoBehaviour extends SimpleBehaviour {

    private boolean finished;
    public static String behaviourName = "receiveCollectInfo";

    public ReceiveCollectInfoBehaviour(Agent myAgent){
        super(myAgent);

    }
    @Override
    public void action() {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol(ShareCollectInfoBehaviour.protocol),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
        if (msgReceived!=null) {
            //add treasure merging function

            // each agent calculates how much ressources the map contains
            HashMap sg = null;
            try {
                sg = (HashMap) msgReceived.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            String senderPos = (String) sg.get("position");
            String senderName = (String) msgReceived.getSender().getLocalName();
            String senderDest = (String) sg.get("dest");
            float senderProp = (float)sg.get("prop");
            Observation senderType = (Observation) sg.get("type");
            List<Treasure> Treasures = (List) sg.get("Treasures");
            ((BaseExplorerAgent)this.myAgent).mergeTreasures(Treasures);

            if(senderType == Observation.ANY_TREASURE){
                if(((BaseExplorerAgent)this.myAgent).getMyType() == Observation.ANY_TREASURE){
                    // No type , with empty backpacks
                    int senderGoldBackPack = (int)((Couple)((ArrayList)sg.get("BackPack")).get(0)).getRight();
                    int senderDiamondBackPack = (int)((Couple)((ArrayList)sg.get("BackPack")).get(1)).getRight();
                    Observation senderPlanAD = this.computeSenderPlan(DIAMOND, senderDiamondBackPack, senderGoldBackPack);
                    Observation senderPlanAG = this.computeSenderPlan(GOLD, senderDiamondBackPack, senderGoldBackPack);
                    if(senderPlanAD == senderPlanAG){
                        //update the agent's belief about the sender
                        ((BaseExplorerAgent) this.myAgent).setAgentType(senderName, senderPlanAD);
                        //compute the type of the agent with the new available knowledge
                        ((BaseExplorerAgent) this.myAgent).setMyType(this.computeAgentPlan(senderPlanAG, senderDiamondBackPack, senderDiamondBackPack));
                    }else{
                        Observation agentPlanSD = this.computeAgentPlan(DIAMOND, senderDiamondBackPack, senderGoldBackPack);
                        Observation agentPlanSG = this.computeAgentPlan(GOLD, senderDiamondBackPack, senderGoldBackPack);
                        if(agentPlanSD == agentPlanSG){
                            Observation inferedSenderType = this.computeSenderPlan(agentPlanSD, senderDiamondBackPack, senderGoldBackPack);
                            ((BaseExplorerAgent) this.myAgent).setAgentType(senderName, inferedSenderType);
                            ((BaseExplorerAgent) this.myAgent).setMyType(agentPlanSD);
                        }else{
                            if(this.myAgent.getLocalName().compareTo(senderName)>0){
                                ((BaseExplorerAgent) this.myAgent).setMyType(GOLD);
                                ((BaseExplorerAgent) this.myAgent).setAgentType(senderName,DIAMOND);
                            }else{
                                ((BaseExplorerAgent) this.myAgent).setMyType(DIAMOND);
                                ((BaseExplorerAgent) this.myAgent).setAgentType(senderName,GOLD);
                            }
                        }
                    }
                    ((BaseExplorerAgent) this.myAgent).setCurrentDest(null);
                }
            }
            else {
                //sender already chose a type
                if (((BaseExplorerAgent) this.myAgent).getMyType() == Observation.ANY_TREASURE) {
                    //current agent hasn't chosen a type yet , it will choose a type by taking the in consideration the the backpack of the agent it's communicating with
                    // the backpacks of the agents it saw during the exploration phase
                    Couple tmp = computeTotalRessources();
                    int totalGold = (int) tmp.getLeft();
                    int totalDiamond = (int) tmp.getRight();
                    if(senderType == GOLD)
                        totalGold -= (int)sg.get("BackPack");
                    if(senderType == DIAMOND)
                        totalDiamond -= (int)sg.get("BackPack");
                    if(totalGold>totalDiamond){
                        ((BaseExplorerAgent) this.myAgent).setMyType(Observation.GOLD);
                    }else{
                        ((BaseExplorerAgent) this.myAgent).setMyType(Observation.DIAMOND);
                    }
                    ((BaseExplorerAgent) this.myAgent).setCurrentDest(null);
                }else{
                    // both agents have already chosen a type
                    // update the knowldge of each agent about the other (backpack wise)
                    ((BaseExplorerAgent) this.myAgent).setAgentBelievedBackpack(msgReceived.getSender().getLocalName(), (int)sg.get("BackPack"));
                    if(senderType == ((BaseExplorerAgent) this.myAgent).getMyType()){
                        //if they're both headed for the same destination then
                        if(((BaseExplorerAgent) this.myAgent).getCurrentDest() == senderDest){
                            float myProp;
                            if((myProp = ((BaseExplorerAgent) this.myAgent).getProp()) > senderProp ){
                                //receiver changes his direction
                                int collectedBySender = (int) ((1-senderProp)* (int)sg.get("BackPack"));
                                ((BaseExplorerAgent) this.myAgent).updateTreasureQuantity(senderDest, collectedBySender);
                                ((BaseExplorerAgent) this.myAgent).setCurrentDest(null);
                            }
                        }
                        //compare the back pack capacity of the of them and the one with the smallest percentage of used backpack goes to pick that treasure
                        // the other recalculates its plan
                    }
                }
            }
            System.out.println( this.myAgent.getLocalName() + " done");
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(RestoreSendHelloBehaviour.behaviourName);
            ((BaseExplorerAgent) this.myAgent).endBehaviour(SendHelloBehaviour.behaviourName);
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(
                    RestoreSendHelloBehaviour.behaviourName,
                    new RestoreSendHelloBehaviour((AbstractDedaleAgent) this.myAgent, 1500)
            );
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap(CollectTreasureBehavior.behaviourName, new CollectTreasureBehavior(this.myAgent));
            ((BaseExplorerAgent) this.myAgent).setBusy(false);
        }

    }


    public Observation computeSenderPlan(Observation agentType, int senderBackPackDiamond, int senderBackPackGold){
        Couple temp = this.computeTotalRessources();
        int totalGold = (int)temp.getLeft();
        int totalDiamond = (int)temp.getRight();
        int agentGoldBackPack = (int)((Couple)((ArrayList)((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace()).get(0)).getRight();
        int agentDiamondBackPack = (int)((Couple)((ArrayList)((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace()).get(1)).getRight();
        if(agentType==DIAMOND){
            totalDiamond -= agentDiamondBackPack+((BaseExplorerAgent)this.myAgent).getSameTypeAgentBackPacks();
        }else{
            totalGold -= agentGoldBackPack +((BaseExplorerAgent) this.myAgent).getSameTypeAgentBackPacks();
        }
        if(totalGold > totalDiamond){
            return GOLD;
        }else if (totalGold < totalDiamond) {
            return DIAMOND;
        }else{
            if(senderBackPackDiamond > senderBackPackGold){
                return DIAMOND;
            }else {
                return GOLD;
            }
        }
    }

    public Observation computeAgentPlan(Observation agentType, int senderBackPackDiamond, int senderBackPackGold){
        Couple temp = this.computeTotalRessources();
        int totalGold = (int)temp.getLeft();
        int totalDiamond = (int)temp.getRight();
        int agentGoldBackPack = (int)((Couple)((ArrayList)((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace()).get(0)).getRight();
        int agentDiamondBackPack = (int)((Couple)((ArrayList)((BaseExplorerAgent) this.myAgent).getBackPackFreeSpace()).get(1)).getRight();
        if(agentType==DIAMOND){
            totalDiamond -= senderBackPackDiamond;
        }else{
            totalGold -= senderBackPackGold +((BaseExplorerAgent) this.myAgent).getSameTypeAgentBackPacks();
        }
        if(totalGold > totalDiamond){
            return GOLD;
        }else if (totalGold < totalDiamond) {
            return DIAMOND;
        }else{
            if(agentDiamondBackPack > agentGoldBackPack){
                return DIAMOND;
            }else {
                return GOLD;
            }
        }
    }

    public Couple computeTotalRessources(){
        int totalGold = 0;
        int totalDiamond = 0;
        List<Treasure> treasures = ((BaseExplorerAgent)this.myAgent).getTreasures();
        for(Treasure t: treasures){
            if(t.getType() == DIAMOND){
                totalDiamond+= t.getQuantity();
            }
            else if(t.getType() == GOLD){
                totalGold+= t.getQuantity();
            }
        }
        return new Couple(totalGold, totalDiamond);
    }

    public boolean done(){return  !((BaseExplorerAgent) this.myAgent).getBehaviourStatus(behaviourName);}

}
