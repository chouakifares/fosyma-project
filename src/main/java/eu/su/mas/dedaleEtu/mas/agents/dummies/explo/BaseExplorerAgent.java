package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;


import java.util.*;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.CollectTreasureBehavior;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.behaviours.Behaviour;

import static eu.su.mas.dedale.env.Observation.*;

public class BaseExplorerAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610241668140L;
    private MapRepresentation myMap;
    private boolean busy = false;
    private int old_phase = 0;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    private boolean full = false;
    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        old_phase = this.phase;
        this.phase = phase;
    }

    public int getCapacity(){
        BaseExplorerAgent myAgent = this;
        int myCapacity = 0;
        Observation myAgentType = ((BaseExplorerAgent) myAgent).getMyTreasureType();
        switch (myAgentType){
            case ANY_TREASURE: ;
                if (((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(1).getRight() < ((BaseExplorerAgent) myAgent).getBackPackFreeSpace().get(0).getRight()){
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

    public int getOldPhase(){
        return old_phase;
    }

    private int phase = 0;

    public Observation getMyType() {
        return myType;
    }

    public void setMyType(Observation myType) {
        this.myType = myType;
    }

    private Observation myType = ANY_TREASURE;
    //used to keep the current destination of the agent and know where is it headed
    private String currentDest;
    private Observation myTtpe = null;


    private boolean explorationDone = false;

    // list of all the behavior that we use during the exploration phase
    public HashMap<String, HashMap> Behaviourmap = new HashMap<String, HashMap>();
    private List<String> list_agentNames=new ArrayList<String>();
    //private List<String> MyTreasures = new ArrayList<String>();



    //list of the treasures that the agents discovered (via exploration or communication)
    private List<Treasure> treasures = new ArrayList<Treasure>();

    public List<Treasure> getTreasures() {
        return treasures;
    }
    public void deleteTreasure(String Pos){
        for(int i=0 ; i<this.treasures.size(); i++)
            if(this.treasures.get(i).getPosition()==Pos){
                this.treasures.remove(i);
            }
    }

    // list of the agent's beliefs about other agents
    private HashMap<String, HashMap> agentBeliefs = new HashMap<String, HashMap>();


    protected void setup(){
        super.setup();
        this.myType = this.getMyTreasureType();

        //get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();

        if(args.length==0){
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        }else{
            int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i<args.length) {
                list_agentNames.add((String)args[i]);
                i++;
            }
        }

        for(String agentName: list_agentNames) {
            HashMap temp = new HashMap();
            temp.put("Map", null);
            temp.put("BackPack", -1);
            agentBeliefs.put(agentName, temp);
        }


        List<Behaviour> lb=new ArrayList<Behaviour>();

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
        addBehaviour(new startMyBehaviours(this,lb));
        System.out.println("the  agent "+this.getLocalName()+ " is started");

    }
    // sets a bahvior states to  false in order to use it in the done methode of that behaviour
    public void endBehaviour(String toDelete) {
            this.Behaviourmap.get(toDelete).put("active", false);
    }
    // add behaviour to the map of behaviours used in exploration , if the behaviour already exists it sets it to active again
    public void addBehaviourToBehaviourMap(String toAdd, Behaviour bToAdd){
        this.addBehaviour(bToAdd);
        HashMap temp = new HashMap();
        temp.put("behavior", bToAdd);
        temp.put("active", true);
        Behaviourmap.put(toAdd, temp);
    }

    public HashMap getBehaviour(String bName){ return Behaviourmap.get(bName);}

    //returns the state of a behaviour (wether it's active or finished)
    public Boolean getBehaviourStatus(String bName){return (Boolean) Behaviourmap.get(bName).get("active");}

    public List<String> getList_agentNames(){
        return list_agentNames;
    }

    public MapRepresentation getMap(){
        return this.myMap;
    }

    public void setMap(MapRepresentation m){
        this.myMap = m;
    }

    public boolean isBusy() {return busy;}

    public void setBusy(boolean busy) {this.busy = busy;}

    public String getCurrentDest() {
        String pos = getCurrentPosition();
        if( currentDest==null || pos==currentDest){
            if(!explorationDone)
                currentDest = this.myMap.getClosestOpenNode(pos).getLeft();
        }
        return currentDest;
    }

    public void setCurrentDest(String currentDest) {
        this.currentDest = currentDest;
    }


    public void explorationDone() {
        this.explorationDone = true;
        this.currentDest = null;
        this.endBehaviour(ExploCoopBehaviour.behaviourName);
        this.addBehaviourToBehaviourMap(CollectTreasureBehavior.behaviourName,new CollectTreasureBehavior(this));
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

    private static HashMap sortHashMapValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        //Custom Comparator
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        //copying the sorted list in HashMap to preserve the iteration order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    public void addTreasure(Treasure t){
        if (! treasures.contains(t)){
            treasures.add(t);
        }
    }

    public int getAgentBelievedBackpack(String agentName){
        return (int) this.agentBeliefs.get(agentName).get("BackPack");
    }

    public void setAgentBelievedBackpack(String agentName, int backPack){
        this.agentBeliefs.get(agentName).put("BackPack", backPack);
    }
}
