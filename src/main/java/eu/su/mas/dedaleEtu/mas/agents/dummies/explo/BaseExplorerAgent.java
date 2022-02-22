package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;

public class BaseExplorerAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610241668140L;
    private MapRepresentation myMap;

    /**
     * This method is automatically called when "agent".start() is executed.
     * Consider that Agent is launched for the first time.
     * 			1) set the agent attributes
     *	 		2) add the behaviours
     *
     */
    private HashMap<String, Behaviour> bmap = new HashMap<String, Behaviour>();
    private List<String> list_agentNames=new ArrayList<String>();


    public MapRepresentation getMap(){
        return this.myMap;
    }

    public void setMap(MapRepresentation m){
        this.myMap = m;
    }

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

        /************************************************
         *
         * ADD the behaviours of the Dummy Moving Agent
         *
         ************************************************/

//        lb.add(new ExploCoopBehaviour(this,this.myMap,list_agentNames));
        Behaviour receiveHello = new ReceiveHelloBehaviour(this);
        Behaviour move = new ExploCoopBehaviour(this, myMap);
        Behaviour helloBehaviour = new SendHelloBehaviour(this, (ArrayList<String>) list_agentNames);
        bmap.put("sendHello", helloBehaviour);
        bmap.put("receiveHello", receiveHello);
        bmap.put("move", move);
        lb.add(helloBehaviour);
        lb.add(receiveHello);
        lb.add(move);

        /***
         * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
         */


        addBehaviour(new startMyBehaviours(this,lb));
        System.out.println("the  agent "+this.getLocalName()+ " is started");

    }
    public void deleteBehaviour(String toDelete){
        this.removeBehaviour(bmap.get(toDelete));
        this.bmap.remove(toDelete);
    }
    public void addBehaviourToMap(String toAdd, Behaviour bToAdd){
        bmap.put(toAdd, bToAdd);
    }

    public List<String> getList_agentNames(){
        return list_agentNames;
    }


}
