package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import org.glassfish.pfl.basic.fsm.Guard;
import org.graphstream.graph.Node;

import java.sql.SQLOutput;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CollectTreasureBehavior extends SimpleBehaviour {

    public static String behaviourName="CollectTreasureBehavior";
    private boolean finished = false;
    private String lastPosition = null;
    private int nbBlocked = 0;
    private int blockedLimit = 1;

    private String nextPosition;


    public CollectTreasureBehavior(Agent myAgent){
        super(myAgent);
    }


    public void action() {
        ////////////   Blocked detection ////////////////////
        //0) Retrieve the current position
        if(((BaseExplorerAgent)this.myAgent).getPhase() == 1 ){
            String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
            MapRepresentation myMap = ((BaseExplorerAgent)myAgent).getMap();
            if (myPosition != null) {
                // Test si l'agent est potentiellement bloqué
                if (lastPosition != null && lastPosition.equals(myPosition))
                    nbBlocked++;
                else {
                    nbBlocked = 0;
                }
                if (nbBlocked == blockedLimit) {
                    nbBlocked = 0;
                    if (myMap.getGraph().getNode(myPosition) != null) {
                        int isLeader = 0;
                        if (myMap.getGraph().getNode(myPosition).getDegree() == 1) {
                            isLeader = 1;
                        } else {
                            if (myMap.getGraph().getNode(myPosition).getDegree() == 2){
                                for (Iterator<Node> it = myMap.getGraph().getNode(myPosition).neighborNodes().iterator(); it.hasNext(); ) {
                                    Node n = it.next();
                                    if (!n.getId().equals(nextPosition)){
                                        if (n.getAttribute("ui.class").toString().equals(MapRepresentation.MapAttribute.blocked.toString())){
                                            isLeader = 1;
                                        }
                                    }
                                }
                            }
                        }
                        nbBlocked = 0;
                        SimpleBehaviour blockedBehaviour = new SendBlockedBehaviour(this.myAgent, isLeader, myPosition, nextPosition, ((BaseExplorerAgent) myAgent).getCurrentDest(), ((BaseExplorerAgent) myAgent).getCapacity());
                        ((BaseExplorerAgent) myAgent).addBehaviourToBehaviourMap(SendBlockedBehaviour.behaviourName, blockedBehaviour);

                    }
                }
            }
            //////////// END OF BLOCKED DETECTION //////////////


            //agent doesn't know where to go
            if(((BaseExplorerAgent)this.myAgent).getCurrentDest()==null) {
                Couple<String, Integer> closestTreasure = findClosestPackableTreasure();
                if(closestTreasure!=null)
                    ((BaseExplorerAgent) this.myAgent).setCurrentDest(closestTreasure.getLeft());
            }
            String nextNode;
            List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
            // agent wants to go somewhere
            if(((BaseExplorerAgent)this.myAgent).getCurrentDest()!=null) {
                List<String> next = ((BaseExplorerAgent)this.myAgent).getMap().getShortestPath(((BaseExplorerAgent) this.myAgent).getCurrentPosition(), ((BaseExplorerAgent) this.myAgent).getCurrentDest());
                //agent hasn't yet reacehd his destination
                if(next.size() > 0) {
                    nextNode = ((BaseExplorerAgent) this.myAgent).getMap().getShortestPath(((BaseExplorerAgent) this.myAgent).getCurrentPosition(), ((BaseExplorerAgent) this.myAgent).getCurrentDest()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    String currentPos = ((BaseExplorerAgent) this.myAgent).getCurrentPosition();
                    if(currentPos != ((BaseExplorerAgent) this.myAgent).getCurrentDest()) {
                        try {
                            this.myAgent.doWait(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // save next position and current position for unblocking purposes
                        lastPosition = myPosition;
                        nextPosition = nextNode;

                        ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                    }
                }
                //agent reached his destination
                else{
                    // checking
                    Observation treasureType = null;
                    for (Couple<Observation, Integer> o : lobs.get(0).getRight()) {
                        switch (o.getLeft()) {
                            case DIAMOND:
                                ((BaseExplorerAgent) this.myAgent).openLock(o.getLeft());
                                treasureType = Observation.DIAMOND;
                                if (((BaseExplorerAgent) this.myAgent).getMyType() == Observation.ANY_TREASURE)
                                    ((BaseExplorerAgent) this.myAgent).setMyType(Observation.DIAMOND);
                                break;
                            case GOLD:
                                ((BaseExplorerAgent) this.myAgent).openLock(o.getLeft());
                                treasureType = Observation.GOLD;
                                if (((BaseExplorerAgent) this.myAgent).getMyType() == Observation.ANY_TREASURE)
                                    ((BaseExplorerAgent) this.myAgent).setMyType(Observation.GOLD);
                                break;
                        }
                    }
                    if(treasureType!= null) {
                        if(((BaseExplorerAgent)this.myAgent).getMyType() == treasureType || ((BaseExplorerAgent) this.myAgent).getProp()== 0) {
                            ((BaseExplorerAgent) this.myAgent).setMyType(treasureType);
                            ((BaseExplorerAgent) this.myAgent).pick();
                        }
                    }else{
                        ((BaseExplorerAgent) this.myAgent).updateTreasure(((BaseExplorerAgent) this.myAgent).getCurrentPosition(), null, 0, Instant.now().toEpochMilli());
                    }
                    ((BaseExplorerAgent) this.myAgent).setCurrentDest(null);
                }
                //update the agent's perceptions
                lobs = ((AbstractDedaleAgent) this.myAgent).observe();
                //update the agent's treasure list
                if(lobs.get(0).getRight().size()!=0) {
                    boolean done = false;
                    for (Couple<Observation, Integer> o : lobs.get(0).getRight()) {
                        switch (o.getLeft()) {
                            case DIAMOND:
                            case GOLD:
                                done = true;
                                ((BaseExplorerAgent) this.myAgent).updateTreasure(
                                        ((BaseExplorerAgent) this.myAgent).getCurrentPosition(),
                                        o.getLeft(),
                                        o.getRight(),
                                        Instant.now().toEpochMilli()
                                );
                        }
                    }
                    if (!done){
                        ((BaseExplorerAgent) this.myAgent).updateTreasure(
                                ((BaseExplorerAgent) this.myAgent).getCurrentPosition(),
                                null,
                                0,
                                Instant.now().toEpochMilli()
                        );

                    }
                }
                else
                    ((BaseExplorerAgent) this.myAgent).updateTreasure(
                            ((BaseExplorerAgent) this.myAgent).getCurrentPosition(),
                            null,
                            0,
                            Instant.now().toEpochMilli()
                    );
            }
            // agent can't pick up anymore treasures
            else{


                ((BaseExplorerAgent) this.myAgent).setFull(true);
                //Agent's job is done
                ((BaseExplorerAgent) this.myAgent).setPhase(4);

                ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap("randomWalk", new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent));
            }
        }
    }


    private Couple<String, Integer> findClosestPackableTreasure(){
        Observation myType= ((BaseExplorerAgent)this.myAgent).getMyType();
        int mySpace =0;
        if(myType == Observation.ANY_TREASURE){
            mySpace = ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace().stream().min(Comparator.comparing(Couple::getRight)).get().getRight();
        }else{
            if(myType == Observation.GOLD)
                mySpace = (int) ((Couple)((ArrayList)((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()).get(0)).getRight();
            else
                mySpace = (int) ((Couple)((ArrayList)((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()).get(1)).getRight();
        }
        List <String> packableTreasures = new ArrayList<String>();
        List <String> smallestNonPackableTreasures = new ArrayList<String>();
        int  max = 0;
        Double min = Double.POSITIVE_INFINITY;
        int totalAvaiilableTreasures = 0;
        for(Treasure i : ((BaseExplorerAgent) this.myAgent).getTreasures()){
            //TO REFACTOR
            if(myType == Observation.ANY_TREASURE) {
                if(i.getType()!=null){
                    if(i.getQuantity() < mySpace) {
                        if (i.getQuantity() == max)
                            packableTreasures.add(i.getPosition());
                        else if (i.getQuantity() > max) {
                            packableTreasures.clear();
                            packableTreasures.add(i.getPosition());
                            max = i.getQuantity();
                        }
                    }else{
                        if (min == Double.valueOf(i.getQuantity()))
                            smallestNonPackableTreasures.add(i.getPosition());
                        else if (i.getQuantity() < min && i.getQuantity()>0) {
                            smallestNonPackableTreasures.clear();
                            smallestNonPackableTreasures.add(i.getPosition());
                            min = Double.valueOf(i.getQuantity());
                        }
                    }
                }
            }else{
                if(i.getType()==myType){
                    totalAvaiilableTreasures += i.getQuantity();
                    if(i.getQuantity()< mySpace){
                        if (i.getQuantity() == max)
                            packableTreasures.add(i.getPosition());
                        else if (i.getQuantity() > max) {
                            packableTreasures.clear();
                            packableTreasures.add(i.getPosition());
                            max = i.getQuantity();
                        }
                    }else {
                        if (Double.valueOf(i.getQuantity()) == min)
                            smallestNonPackableTreasures.add(i.getPosition());
                        else if (i.getQuantity() < min && i.getQuantity() > 0) {
                            smallestNonPackableTreasures.clear();
                            smallestNonPackableTreasures.add(i.getPosition());
                            min = Double.valueOf(i.getQuantity());
                        }
                    }
                }
            }
        }
        if(mySpace!=0 && totalAvaiilableTreasures >= ((BaseExplorerAgent) this.myAgent).getSameTypeAgentBackPacks()*((BaseExplorerAgent) this.myAgent).getProp()){
            List<String> temp = null;
            if(!packableTreasures.isEmpty()){
                temp = packableTreasures;
            }
            // we look for the smallest treasure that the agent is able to carry
            else if (!smallestNonPackableTreasures.isEmpty()){
                temp = smallestNonPackableTreasures;
            }
            if(temp != null){
                List<Couple<String,Integer>> lc=
                        temp.stream()
                                .map(on -> (((BaseExplorerAgent) this.myAgent).getMap().
                                        getShortestPath(((BaseExplorerAgent) this.myAgent).getCurrentPosition(),on)!=null)?
                                        new Couple<String, Integer>(on,((BaseExplorerAgent) this.myAgent).getMap().getShortestPath(((BaseExplorerAgent) this.myAgent)
                                                .getCurrentPosition(),on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
                                .collect(Collectors.toList());

                Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
                return closest.get();
            }else {
                if (((BaseExplorerAgent) this.myAgent).getProp() == 0) {
                    for (Treasure i : ((BaseExplorerAgent) this.myAgent).getTreasures()) {
                        //TO REFACTOR
                        if (i.getType() != null) {
                            if (i.getQuantity() < mySpace) {
                                if (i.getQuantity() == max)
                                    packableTreasures.add(i.getPosition());
                                else if (i.getQuantity() > max) {
                                    packableTreasures.clear();
                                    packableTreasures.add(i.getPosition());
                                    max = i.getQuantity();
                                }
                            } else {
                                if (min == Double.valueOf(i.getQuantity()))
                                    smallestNonPackableTreasures.add(i.getPosition());
                                else if (i.getQuantity() < min && i.getQuantity() > 0) {
                                    smallestNonPackableTreasures.clear();
                                    smallestNonPackableTreasures.add(i.getPosition());
                                    min = Double.valueOf(i.getQuantity());
                                }
                            }
                        }
                    }
                }
                if(!packableTreasures.isEmpty()){
                    temp = packableTreasures;
                }
                // we look for the smallest treasure that the agent is able to carry
                else if (!smallestNonPackableTreasures.isEmpty()){
                    temp = smallestNonPackableTreasures;
                }
                if(temp != null){
                    List<Couple<String,Integer>> lc=
                            temp.stream()
                                    .map(on -> (((BaseExplorerAgent) this.myAgent).getMap().
                                            getShortestPath(((BaseExplorerAgent) this.myAgent).getCurrentPosition(),on)!=null)?
                                            new Couple<String, Integer>(on,((BaseExplorerAgent) this.myAgent).getMap().getShortestPath(((BaseExplorerAgent) this.myAgent)
                                                    .getCurrentPosition(),on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
                                    .collect(Collectors.toList());

                    Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
                    return closest.get();
                }
            }
        }
        return null;

    }

    @Override
    public boolean done() {
        return !((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
    }
}
