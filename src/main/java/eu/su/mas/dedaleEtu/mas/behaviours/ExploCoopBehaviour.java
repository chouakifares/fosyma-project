package eu.su.mas.dedaleEtu.mas.behaviours;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;


import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import jade.core.behaviours.SimpleBehaviour;

import static java.time.Instant.now;


public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	public static String behaviourName = "move";
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;

		
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			((BaseExplorerAgent) this.myAgent).setMap(new MapRepresentation()) ;
			this.myMap = ((BaseExplorerAgent) this.myAgent).getMap();
		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){

			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(200);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				((BaseExplorerAgent)this.myAgent).endBehaviour(behaviourName);
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				((BaseExplorerAgent)this.myAgent).explorationDone();
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					if(((BaseExplorerAgent)this.myAgent).getCurrentDest()!=null) {
						List<String> next = this.myMap.getShortestPath(myPosition, ((BaseExplorerAgent) this.myAgent).getCurrentDest());
						if(next.size() > 0) {
							nextNode = this.myMap.getShortestPath(myPosition, ((BaseExplorerAgent) this.myAgent).getCurrentDest()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
						}
					}
				}
				if(((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName) && nextNode != null) {
					if(nextNode == ((BaseExplorerAgent) this.myAgent).getCurrentDest())
						((BaseExplorerAgent) this.myAgent).setCurrentDest(null);

					((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);

				}
			}

			for (Couple <Observation, Integer> o: lobs.get(0).getRight()){
				switch (o.getLeft()){
					case DIAMOND:
						((BaseExplorerAgent) this.myAgent).getMap().setContainsDiamond(true);
						case GOLD:
						// ADD THE TREASURE TO THE LIST
						Treasure t = new Treasure(myPosition, o.getLeft(), o.getRight(),Instant.now().toEpochMilli());
						((BaseExplorerAgent)myAgent).addTreasure(t);
				}
			}



		}
	}

	@Override
	public boolean done() {
		return !((BaseExplorerAgent)this.myAgent).getBehaviourStatus(behaviourName);
	}

}
