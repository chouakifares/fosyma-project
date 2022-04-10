package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
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
            Observation senderType = (Observation) sg.get("type");
            List<Treasure> Treasures = (List) sg.get("Treasures");
            ((BaseExplorerAgent)this.myAgent).mergeTreasures(Treasures);
            if(senderType == Observation.ANY_TREASURE){
                if(((BaseExplorerAgent)this.myAgent).getMyType() == Observation.ANY_TREASURE){
                    // No type , with empty backpacks
                }else{
                    //the receiver has a type while the sender hasn't yet picked a type
                    // sender will choose its type by looking at the available ressource when taking out the amount of ressource the back pack of the receiver has
                }
            }else {
                //sender already chose a type
                if (((BaseExplorerAgent) this.myAgent).getMyType() == Observation.ANY_TREASURE) {
                    //current agent hasn't chose a type yet , it will choose a type by taking the in consideration the the backpack of the agent it's communicating with
                    // the backpacks of the agents it saw during the exploration phase
                }else{
                    // both agents have already chosen a type
                    // update the knowldge of each agent about the other (backpack wise)
                    if(senderType == ((BaseExplorerAgent) this.myAgent).getMyType()){
                        //if they're both headed for the same destination then
                        //compare the back pack capacity of the of them and the one with the smallest percentage of used backpack goes to pick that treasure
                        // the other recalculates its plan
                    }
                }
            }

            int totalGold = 0;
            int totalDiamond = 0;
            int totalBackPacks = 0;
            List<Treasure> treasures = ((BaseExplorerAgent)this.myAgent).getTreasures();
            Collections.sort(treasures);
            for(Treasure t: treasures){
                if(t.getType() == DIAMOND){
                    totalDiamond+= t.getQuantity();
                }
                else if(t.getType() == GOLD){
                    totalGold+= t.getQuantity();
                }
            }
            System.out.println( this.myAgent.getLocalName() + "done");
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            ((BaseExplorerAgent) this.myAgent).setBusy(false);
        }

    }


    public boolean done(){return  !((BaseExplorerAgent) this.myAgent).getBehaviourStatus(behaviourName);}

}
