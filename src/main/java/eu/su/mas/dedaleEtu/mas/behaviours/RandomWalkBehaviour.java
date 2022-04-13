package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.TickerBehaviour;

/**************************************
 * 
 * 
 * 				BEHAVIOUR RandomWalk : Illustrates how an agent can interact with, and move in, the environment
 * 
 * 
 **************************************/


public class RandomWalkBehaviour extends TickerBehaviour{
	
	/**
	 * When an agent choose to move
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	public RandomWalkBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 500);
	}

	@Override
	public void onTick() {
		//Example to retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.myAgent.getLocalName()+" -- myCurrentPosition is: "+myPosition);
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			
			//list of observations associated to the currentPosition
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();

			//example related to the use of the backpack for the treasure hunt


			//If the agent picked (part of) the treasure

			//Random move from the current position
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move


			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//The move action (if any) should be the last action of your behaviour
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

}