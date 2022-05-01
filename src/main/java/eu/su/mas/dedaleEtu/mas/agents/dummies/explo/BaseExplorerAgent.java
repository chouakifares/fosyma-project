package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;


import java.time.Instant;
import java.util.*;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.CollectTreasureBehavior;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.behaviours.Behaviour;
import org.graphstream.graph.Graph;

import static eu.su.mas.dedale.env.Observation.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class BaseExplorerAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610241668140L;
    private MapRepresentation myMap;
    private boolean busy = false;
    private float prop = 0;
    private int sameTypeAgentBackPacks = 0;
    private int old_phase = 0;
    private boolean full = false;
    private int totalSpace = 0;
    private int phase = 0;

    public int getSameTypeAgentBackPacks() {
        return sameTypeAgentBackPacks;
    }

    private Observation myType = ANY_TREASURE;
    //used to keep the current destination of the agent and know where is it headed
    private String currentDest;
    private Observation myTtpe = null;

    // list of the agent's beliefs about other agents
    private HashMap<String, HashMap> agentBeliefs = new HashMap<String, HashMap>();

    private boolean explorationDone = false;

    // list of all the behavior that we use during the exploration phase
    public HashMap<String, HashMap> Behaviourmap = new HashMap<String, HashMap>();
    private List<String> list_agentNames = new ArrayList<String>();
    //private List<String> MyTreasures = new ArrayList<String>();


    //list of the treasures that the agents discovered (via exploration or communication)
    private List<Treasure> treasures = new ArrayList<Treasure>();

    private HashMap mapSent = new HashMap();
    private HashMap mapReceived = new HashMap();


    //////////////////////////////////////////////////////// METHODS //////////////////////////////////////////////////////
    public void increaseSameTypeAgentBackPacks(int sameTypeAgentBackPacks) {
        this.sameTypeAgentBackPacks += sameTypeAgentBackPacks;
    }


    public float getProp() {
        int available;
        if (this.myType == GOLD)
            available = (int) ((Couple) ((ArrayList) this.getBackPackFreeSpace()).get(0)).getRight();
        else
            available = (int) ((Couple) ((ArrayList) this.getBackPackFreeSpace()).get(1)).getRight();
        if (totalSpace != 0)
            return (this.totalSpace - available) / this.totalSpace;
        return 0;
    }

    public int getTotalSpace() {
        return totalSpace;
    }


    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }


    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        if (this.phase != 2)
            old_phase = this.phase;
        this.phase = phase;
    }

    public int getCapacity() {
        BaseExplorerAgent myAgent = this;
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
        return myCapacity;
    }

    public int getOldPhase() {
        return old_phase;
    }


    public Observation getMyType() {
        return myType;
    }

    public void setMyType(Observation myType) {
        this.myType = myType;
        if (myType == GOLD)
            this.totalSpace = (int) ((Couple) ((ArrayList) this.getBackPackFreeSpace()).get(0)).getRight();
        else
            this.totalSpace = (int) ((Couple) ((ArrayList) this.getBackPackFreeSpace()).get(1)).getRight();
    }


    public List<Treasure> getTreasures() {
        return treasures;
    }

    public void updateTreasure(String Pos, Observation obs, int quantity, long t) {
        for (int i = 0; i < this.treasures.size(); i++) {
            if (this.treasures.get(i).getPosition() == Pos) {
                this.treasures.remove(i);
            }
        }
        this.treasures.add(new Treasure(Pos, obs, quantity, t));
    }


    public void updateTreasureQuantity(String Pos, int collectedQuantity) {
        Observation obs = null;
        int initialQuantity = 0;
        for (int i = 0; i < this.treasures.size(); i++) {
            if (this.treasures.get(i).getPosition() == Pos) {
                obs = this.treasures.get(i).getType();
                initialQuantity = this.treasures.get(i).getQuantity();
                this.treasures.remove(i);
            }
        }
        this.treasures.add(new Treasure(Pos, obs, max(initialQuantity - collectedQuantity, 0), Instant.now().toEpochMilli()));
    }

    //Merges a list a of treasure that agent A receives from agent B
    public void mergeTreasures(List<Treasure> t) {
        for (int j = 0; j < t.size(); j++) {
            boolean in = false;
            Treasure tr = t.get(j);
            for (int i = 0; i < this.treasures.size(); i++) {
                in = true;
                if (this.treasures.get(i).getPosition() == t.get(j).getPosition()) {
                    //update the trasure if the
                    if (this.treasures.get(i).getObsTime() < t.get(j).getObsTime())
                        this.updateTreasure(tr.getPosition(), tr.getType(), tr.getQuantity(), tr.getObsTime());

                }
            }
            // the agent hasn't find this treasure yet
            if (!in) {
                //add the treasure to the agent's treasure list
                this.treasures.add(new Treasure(tr.getPosition(), tr.getType(), tr.getQuantity(), tr.getObsTime()));
            }
        }
    }


    protected void setup() {
        super.setup();
        this.myType = this.getMyTreasureType();
        //get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();
        if (args.length == 0) {
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        } else {
            int i = 2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i < args.length) {
                list_agentNames.add((String) args[i]);
                i++;
            }
        }
        for (String agentName : list_agentNames) {
            HashMap temp = new HashMap();
            temp.put("Map", null);
            temp.put("BackPack", -1);
            agentBeliefs.put(agentName, temp);
        }

        List<Behaviour> lb = new ArrayList<Behaviour>();
        Behaviour move = new ExploCoopBehaviour(this, myMap);
        Behaviour helloBehaviour = new SendHelloBehaviour(this, (ArrayList<String>) list_agentNames);
        HashMap temp = new HashMap();
        temp.put("behavior", helloBehaviour);
        temp.put("active", true);
        Behaviourmap.put(SendHelloBehaviour.behaviourName, temp);
        temp = new HashMap();
        temp.put("behavior", move);
        temp.put("active", true);
        Behaviourmap.put(ExploCoopBehaviour.behaviourName, temp);
        temp = new HashMap();
        lb.add(helloBehaviour);
        lb.add(move);
        addBehaviour(new startMyBehaviours(this, lb));
        System.out.println("the  agent " + this.getLocalName() + " is started");
        for (String agentName: list_agentNames){
            mapSent.put(agentName, null);
        }
        for (String agentName: list_agentNames){
            mapReceived.put(agentName, null);
        }
    }

    // sets a bahvior states to  false in order to use it in the done methode of that behaviour
    public void endBehaviour(String toDelete) {
        if (this.Behaviourmap.get(toDelete) != null) {
            this.Behaviourmap.get(toDelete).put("active", false);
        }
    }

    // add behaviour to the map of behaviours used in exploration , if the behaviour already exists it sets it to active again
    public void addBehaviourToBehaviourMap(String toAdd, Behaviour bToAdd) {
        this.addBehaviour(bToAdd);
        HashMap temp = new HashMap();
        temp.put("behavior", bToAdd);
        temp.put("active", true);
        Behaviourmap.put(toAdd, temp);
    }

    public HashMap getBehaviour(String bName) {
        return Behaviourmap.get(bName);
    }

    //returns the state of a behaviour (wether it's active or finished)
    public Boolean getBehaviourStatus(String bName) {
        try {
            return (Boolean) Behaviourmap.get(bName).get("active");
        } catch (NullPointerException e) {
            return null;
        }
    }

    public List<String> getList_agentNames() {
        return list_agentNames;
    }

    public MapRepresentation getMap() {
        return this.myMap;
    }

    public void setMap(MapRepresentation m) {
        this.myMap = m;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public String getCurrentDest() {
        String pos = getCurrentPosition();
        if (currentDest == null || pos == currentDest) {
            if (!explorationDone)
                currentDest = this.myMap.getClosestOpenNode(pos).getLeft();
        }
        return currentDest;
    }

    public void setCurrentDest(String currentDest) {
        this.currentDest = currentDest;
    }

    public boolean getExplorationStatus() {
        return explorationDone;
    }


    public void explorationDone() {
        this.explorationDone = true;
        this.currentDest = null;
        this.endBehaviour(ExploCoopBehaviour.behaviourName);
        this.addBehaviourToBehaviourMap(CollectTreasureBehavior.behaviourName, new CollectTreasureBehavior(this));
        this.phase = 1;
        this.busy = false;
        // calculate the plans for collecting the treasures

        /*int totalGold = 0;
        int totalDiamond = 0;
        int totalBackPacks = 0;
        Collections.sort(this.treasures);
        for(Treasure t: this.treasures){
            if(t.getType() == DIAMOND){
                totalDiamond+= t.getQuantity();
            }
            else if(t.getType() == GOLD){
                totalGold+= t.getQuantity();
            }

        }
        //sort the beliefs of the agent
        HashMap<String, Integer> backpacks = new HashMap<String, Integer>();
        for(String agentName: this.list_agentNames) {
            if ((Integer) this.agentBeliefs.get(agentName).get("BackPack") != -1){
                totalBackPacks = (Integer) this.agentBeliefs.get(agentName).get("BackPack") + totalBackPacks;
                backpacks.put(agentName, (Integer) this.agentBeliefs.get(agentName).get("BackPack"));
            }
        }
        backpacks.put(this.getLocalName(), this.getBackPackFreeSpace().get(0).getRight());
        //maybe add this for taking into consideration the rest of riches that won't be gathered by this group of agent
        /*if(totalDiamond+totalGold-totalBackPacks > 0 && backpacks.size()< list_agentNames.size()+1)
            backpacks.put("others", totalDiamond+totalGold-totalBackPacks);
        */
        /*
        backpacks = sortHashMapValues(backpacks);
        // assign each agent to a type using
        // calculate the number of agent to assign to each type of ressource
        if(totalDiamond==0){

        }else if(totalGold==0){

        }else{
            float proportion = totalDiamond/(totalGold+totalDiamond);
            int intProp = (int) proportion;
            if(proportion - intProp < 0.5) {
                int diamondAgents = (int) (proportion * (list_agentNames.size()));
            }else{
                int diamondAgents = (int) (proportion * (list_agentNames.size() + 1));
            }
            int temp_diamond = totalDiamond;
            int temp_gold = totalGold;
            float temp_prop = proportion
            while(temp_diamond>0 || temp_gold>0){
                List list = new LinkedList(backpacks.entrySet());
                if()
            }
        }


        /*
        while(totalTreasures != 0){
            for(String agentName : backpacks.keySet()){
                if(agentName==this.getLocalName()){
                    this.
                }
            }
        }
        */
    }

    public void addTreasure(Treasure t) {
        if (!treasures.contains(t)) {
            treasures.add(t);
        }
    }

    public int getAgentBelievedBackpack(String agentName) {
        return (int) this.agentBeliefs.get(agentName).get("BackPack");
    }

    public void setAgentBelievedBackpack(String agentName, int backPack) {
        this.agentBeliefs.get(agentName).put("BackPack", backPack);
    }

    public void setAgentType(String agentName, Observation type) {
        this.agentBeliefs.get(agentName).put("type", type);
    }

    public void setExplorationDone(boolean b) {
        explorationDone = b;
    }

    public SerializableSimpleGraph getMapSent (String agentName){
        return (SerializableSimpleGraph) mapSent.get(agentName);
    }

    public void setMapSent(String agentName, SerializableSimpleGraph sg){
        mapSent.put(agentName, sg);
    }
    public SerializableSimpleGraph getMapReceived (String agentName){
        return (SerializableSimpleGraph) mapReceived.get(agentName);
    }

    public void setMapReceived(String agentName, SerializableSimpleGraph sg){
        mapReceived.put(agentName, sg);
    }

}
