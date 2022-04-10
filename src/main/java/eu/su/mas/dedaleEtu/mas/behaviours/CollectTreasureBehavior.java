package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

import java.sql.SQLOutput;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectTreasureBehavior extends SimpleBehaviour {
    public static String behaviourName="CollectTreasureBehavior";
    private boolean finished = false;
    public CollectTreasureBehavior(Agent myAgent){
        super(myAgent);
    }
    public void action() {
        //agent doesn't know where to go
        if(((BaseExplorerAgent)this.myAgent).getCurrentDest()==null) {
            Couple<String , Integer> closestTreasure = findClosestPackableTreasure();
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
                        this.myAgent.doWait(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                }
            }
            //agnet reached his destination
            else{
                // checking
                Observation treasureType = null;
                for (Couple<Observation, Integer> o : lobs.get(0).getRight()) {
                    switch (o.getLeft()) {
                        case DIAMOND:
                            treasureType = Observation.DIAMOND;
                            if(((BaseExplorerAgent)this.myAgent).getMyType()== Observation.ANY_TREASURE)
                                ((BaseExplorerAgent) this.myAgent).setMyType(Observation.DIAMOND);
                            break;
                        case GOLD:
                            treasureType = Observation.GOLD;
                            if(((BaseExplorerAgent)this.myAgent).getMyType()== Observation.ANY_TREASURE)
                                ((BaseExplorerAgent) this.myAgent).setMyType(Observation.GOLD);
                            break;
                    }
                }

                if(treasureType!= null && ((BaseExplorerAgent)this.myAgent).getMyType() == treasureType) {
                    ((BaseExplorerAgent) this.myAgent).pick();

                }
                ((BaseExplorerAgent) this.myAgent).setCurrentDest(null);
            }
            //update the agent's perceptions
            lobs = ((AbstractDedaleAgent) this.myAgent).observe();
            //update the agent's treasure list
            if(lobs.get(0).getRight().size()!=0)
                ((BaseExplorerAgent) this.myAgent).updateTreasure(
                        ((BaseExplorerAgent) this.myAgent).getCurrentPosition(),
                        (Observation) ((Couple) lobs.get(0).getRight().get(0)).getLeft(),
                        (Integer) ((Couple) lobs.get(0).getRight().get(0)).getRight(),
                        Instant.now().toEpochMilli()
                );
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
            ((BaseExplorerAgent) this.myAgent).endBehaviour(behaviourName);
            ((BaseExplorerAgent) this.myAgent).setFull(true);
            //Agent's job is done
            ((BaseExplorerAgent) this.myAgent).addBehaviourToBehaviourMap("randomWalk", new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent));
        }
    }


    private Couple<String, Integer> findClosestPackableTreasure(){
        Observation myType= ((BaseExplorerAgent)this.myAgent).getMyType();
        int mySpace = (int) ((Couple)((ArrayList)((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()).get(0)).getRight();
        List <String> packableTreasures = new ArrayList<String>();
        for(Treasure i : ((BaseExplorerAgent) this.myAgent).getTreasures()){
            if(myType == Observation.ANY_TREASURE){
                if(((BaseExplorerAgent) this.myAgent).getMap().isContainsDiamond()){
                    if(i.getType() == Observation.DIAMOND)
                        packableTreasures.add(i.getPosition());
                }else{
                    packableTreasures.add(i.getPosition());
                }

            }else{
                if(i.getType()==myType){
                    packableTreasures.add(i.getPosition());
                }
            }
        }
        if(!packableTreasures.isEmpty() && mySpace!=0){
            List<Couple<String,Integer>> lc=
                    packableTreasures.stream()
                            .map(on -> (((BaseExplorerAgent) this.myAgent).getMap().
                                    getShortestPath(((BaseExplorerAgent) this.myAgent).getCurrentPosition(),on)!=null)?
                                    new Couple<String, Integer>(on,((BaseExplorerAgent) this.myAgent).getMap().getShortestPath(((BaseExplorerAgent) this.myAgent)
                                            .getCurrentPosition(),on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
                            .collect(Collectors.toList());
            Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
            return closest.get();
        }
        return null;
    }

    @Override
    public boolean done() {
        return ((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
    }
}
