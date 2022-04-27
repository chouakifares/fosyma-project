package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.BaseExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.WakerBehaviour;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CheckWumpusBehaviour extends WakerBehaviour {


    public CheckWumpusBehaviour(AbstractDedaleAgent a, long timeout){
        super(a, timeout);
    }

    @Override
    protected void onWake() {
        // Retrieve the list of blocked nodes
        MapRepresentation myMap = ((BaseExplorerAgent)myAgent).getMap();
        List<String> blockedNodes = new ArrayList<String>();
        for (Iterator<Node> it = myMap.getGraph().nodes().iterator(); it.hasNext(); ) {
            Node n = it.next();
            if (n.getAttribute("ui.class").toString().equals(MapRepresentation.MapAttribute.blocked.toString())){
                blockedNodes.add(n.getId());
            }
        }

        if (!blockedNodes.isEmpty()){

            // CHECK

        }

    }
}
