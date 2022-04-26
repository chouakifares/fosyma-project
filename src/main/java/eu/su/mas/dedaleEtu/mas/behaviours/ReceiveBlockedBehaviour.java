package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.Map;

public class ReceiveBlockedBehaviour extends SimpleBehaviour {

    public static String behaviourName = "receiveBlocked";

    public ReceiveBlockedBehaviour(AbstractDedaleAgent myAgent){
        super(myAgent);
    }

    @Override
    public void action() {
        MessageTemplate templateBlocked = MessageTemplate.MatchProtocol(SendBlockedBehaviour.protocol);
        ACLMessage msgReceived = this.myAgent.receive(templateBlocked);
        if (msgReceived != null) { // Received a "blocked" message from an Agent
            
            // Unpacking the message
            String nextPosition = "";
            int isLeader = 0;
            String senderPosition = "";
            String finalDestination = "";
            int capacity = 0;
            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> map = null;

            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("map")) {
                    map = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>) ((HashMap) msgReceived.getContentObject()).get("map");
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            if (map != null) {
                ((BaseExplorerAgent) myAgent).getMap().mergeMap(map);
            }

            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("nextPosition")) {
                    nextPosition = (String) ((HashMap) msgReceived.getContentObject()).get("nextPosition");
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("isLeader")) {
                    isLeader = Integer.parseInt((String) ((HashMap) msgReceived.getContentObject()).get("isLeader"));
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("senderPosition")) {
                    senderPosition = (String) ((HashMap) msgReceived.getContentObject()).get("senderPosition");
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("finalDestination")) {
                    finalDestination = (String) ((HashMap) msgReceived.getContentObject()).get("finalDestination");
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            try {
                if (((HashMap) msgReceived.getContentObject()).containsKey("capacity")) {
                    capacity = Integer.parseInt((String) ((HashMap) msgReceived.getContentObject()).get("capacity"));
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            // Checking if I'm targetted by the message
            if (nextPosition.equals(((BaseExplorerAgent) myAgent).getCurrentPosition())) { // if I am the one who is blocking
                System.out.println(myAgent.getLocalName() + " i am blocking " + msgReceived.getSender().getLocalName() + " " + ((BaseExplorerAgent) myAgent).getPhase());
                if (isLeader == 1) { // if the sender has the priority (cul de sac OR is under a leader)
                    ((BaseExplorerAgent) myAgent).endBehaviour(behaviourName);
                    // Interrumpt agent movement
                    if ((((BaseExplorerAgent) myAgent).getPhase() != 2)) {
                        ((BaseExplorerAgent) myAgent).setPhase(2);
                        // Launch a behaviour that will make my agent move to another available position
                        System.out.println(myAgent.getLocalName() + " starts a leader unblocking behaviour for " + msgReceived.getSender().getLocalName());
                        UnblockBehaviour u = new UnblockBehaviour((AbstractDedaleAgent) this.myAgent, isLeader, senderPosition, nextPosition, finalDestination, msgReceived.getSender().getLocalName());
                        ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(UnblockBehaviour.behaviourName, u);
                    }
                } else if(isLeader == 2) {

                }
                else { // he's not in a cul de sac
                    int myCapacity = 0;
                    Observation myAgentType = ((BaseExplorerAgent) myAgent).getMyTreasureType();
                    switch (myAgentType) {
                        case ANY_TREASURE:
                            ;
                            if (((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(1).getRight() < ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(0).getRight()) {
                                myCapacity = ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(1).getRight();
                            } else {
                                myCapacity = ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(0).getRight();
                            }
                        case DIAMOND:
                            myCapacity = ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(1).getRight();
                        case GOLD:
                            myCapacity = ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(0).getRight();

                    }
                    System.out.println(myCapacity + " ? " + capacity);
                    if (myCapacity < capacity) {
                        SimpleBehaviour u = new UnblockBehaviour((AbstractDedaleAgent) this.myAgent, isLeader, senderPosition, nextPosition, finalDestination, msgReceived.getSender().getLocalName());
                        ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(UnblockBehaviour.behaviourName, u);
                        ((BaseExplorerAgent) myAgent).setPhase(2);
                        System.out.println(myAgent.getLocalName() + " starts a capacity unblocking behaviour for " + msgReceived.getSender().getLocalName());

                    } else if (myCapacity == capacity) {
                        if (myAgent.getLocalName().compareTo(msgReceived.getSender().getLocalName()) > 0) {
                            SimpleBehaviour u = new UnblockBehaviour((AbstractDedaleAgent) this.myAgent, isLeader, senderPosition, nextPosition, finalDestination, msgReceived.getSender().getLocalName());
                            ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(UnblockBehaviour.behaviourName, u);
                            ((BaseExplorerAgent) myAgent).setPhase(2);
                            System.out.println(myAgent.getLocalName() + " starts a name unblocking behaviour for " + msgReceived.getSender().getLocalName());
                        }
                    }

                }
            }
        }
    }


    @Override
    public boolean done() {
        return !((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
    }
}
