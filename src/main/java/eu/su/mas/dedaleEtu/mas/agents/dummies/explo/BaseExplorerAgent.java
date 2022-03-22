package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;

public class BaseExplorerAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610241668140L;
    private MapRepresentation myMap;
    private boolean busy = false;

    //used to keep the current destination of the agent and know where is it headed
    private String currentDest;



    private boolean explorationDone = false;

    // list of all the behavior that we use during the exploration phase
    public HashMap<String, HashMap> ExploBehaviourmap = new HashMap<String, HashMap>();
    private List<String> list_agentNames=new ArrayList<String>();




    protected void setup(){
        super.setup();



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

        List<Behaviour> lb=new ArrayList<Behaviour>();

        Behaviour move = new ExploCoopBehaviour(this, myMap);
        Behaviour helloBehaviour = new SendHelloBehaviour(this, (ArrayList<String>) list_agentNames);
        HashMap temp = new HashMap();
        temp.put("behavior", helloBehaviour);
        temp.put("active", true);
        ExploBehaviourmap.put(SendHelloBehaviour.behaviourName, temp);
        temp = new HashMap();
        temp.put("behavior", move);
        temp.put("active", true);
        ExploBehaviourmap.put(ExploCoopBehaviour.behaviourName, temp);
        temp = new HashMap();
        lb.add(helloBehaviour);
        lb.add(move);
        addBehaviour(new startMyBehaviours(this,lb));
        System.out.println("the  agent "+this.getLocalName()+ " is started");

    }
    // sets a bahvior states to  false in order to use it in the done methode of that behaviour
    public void endBehaviour(String toDelete) {
        try {
            this.ExploBehaviourmap.get(toDelete).put("active", false);
        }
        catch(NullPointerException e){
            System.out.println(toDelete+" EXECEPTION");
        }
    }
    // add behaviour to the map of behaviours used in exploration , if the behaviour already exists it sets it to active again
    public void addBehaviourToExploBehaviourMap(String toAdd, Behaviour bToAdd){
        this.addBehaviour(bToAdd);
        HashMap temp = new HashMap();
        temp.put("behavior", bToAdd);
        temp.put("active", true);
        ExploBehaviourmap.put(toAdd, temp);
    }

    public HashMap getBehaviour(String bName){ return ExploBehaviourmap.get(bName);}

    //returns the state of a behaviour (wether it's active or finished)
    public Boolean getExploBehaviourStatus(String bName){return (Boolean) ExploBehaviourmap.get(bName).get("active");}

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
        if( (currentDest==null && !explorationDone) || pos==currentDest ){
            currentDest = this.myMap.getClosestOpenNode(pos).getLeft();
        }
        return currentDest;
    }

    public void setCurrentDest(String currentDest) {
        this.currentDest = currentDest;
    }

    public String getNextPosition(){
        List<String> openNodes = this.myMap.getOpenNodes();

        return null;
    }

    public void explorationDone() {
        this.explorationDone = true;
    }
}
