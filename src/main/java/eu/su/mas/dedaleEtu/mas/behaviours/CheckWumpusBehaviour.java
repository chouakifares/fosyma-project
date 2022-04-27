package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.WakerBehaviour;

import java.util.ArrayList;

public class CheckWumpusBehaviour extends WakerBehaviour {


    public CheckWumpusBehaviour(AbstractDedaleAgent a, long timeout){
        super(a, timeout);
    }

    @Override
    protected void onWake() {
        // Retrieve the list of blocked nodes
        MapRepresentation myMap = ((BaseExplorerAgent)myAgent).getMap();
        List<String> blockedNodes = new ArrayList<String>();
        myMap.getGraph().nodes().iterator();
    }
}
