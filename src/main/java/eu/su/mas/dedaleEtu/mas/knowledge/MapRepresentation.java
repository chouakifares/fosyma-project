package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import javafx.application.Platform;

/**
 * This simple topology representation only deals with the graph, not its content.</br>
 * The knowledge representation is not well written (at all), it is just given as a minimal example.</br>
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * 
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {	
		agent,open, closed, blocked;
	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id
	 * @param mapAttribute
	 */
	public synchronized void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
	}

	/**
	 * Add a node to the graph. Do nothing if the node already exists.
	 * If new, it is labeled as open (non-visited)
	 * @param id id of the node
	 * @return true if added
	 */
	public synchronized boolean addNewNode(String id) {
		if (this.g.getNode(id)==null){
			addNode(id,MapAttribute.open);
			return true;
		}
		return false;
	}

	/**
	 * Add an undirect edge if not already existing.
	 * @param idNode1
	 * @param idNode2
	 */
	public synchronized void addEdge(String idNode1,String idNode2){
		this.nbEdges++;
		try {
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			this.nbEdges--;
		} catch(ElementNotFoundException e3){

		}
	}

	/**
	 * Compute the shortest Path from idFrom to IdTo.
	 * It tries first to compute a path without any node labelled as "blocked" in it, if it exists.
	 * Otherwise, it returns a path containing possibly blocked nodes.
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow, null if the targeted node is not currently reachable
	 */
	public synchronized List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();
		Dijkstra dijkstra = new Dijkstra();
		List<Node> path;
		Iterator<Node> iter;

		// If both the source and the target are not labelled as blocked nodes
		if (!g.getNode(idTo).getAttribute("ui.class").toString().equals(MapAttribute.blocked.toString())
			&& !g.getNode(idFrom).getAttribute("ui.class").toString().equals(MapAttribute.blocked.toString())
		   )
		{
			// Create a filtered graph that is a copy of the graph but without 'blocked' nodes
			Graph filteredGraph = new SingleGraph("Graph without blocked nodes");

			// get a list of "free" nodes from the initial graph
			List<Node> freeNodesList = new ArrayList<Node>();
			for (Iterator<Node> it = g.nodes().iterator(); it.hasNext(); ) {
				Node n = it.next();
				if ( ! n.getAttribute("ui.class").toString().equals(MapAttribute.blocked.toString())){
					freeNodesList.add(n);
				}
			}
			// add the free nodes to the filtered graph
			for (Node n: freeNodesList){
				filteredGraph.addNode(n.getId());
			}
			// add the edges that are not part of a blocked node in the filtered graph.
			for (Iterator<Edge> it = g.edges().iterator(); it.hasNext(); ) {
				Edge e = it.next();
				// If both the source and target of the edge are contained in the free nodes list, then add it to the filtered graph
				if (freeNodesList.contains(e.getSourceNode()) && freeNodesList.contains(e.getTargetNode())){
					filteredGraph.addEdge(e.getId(),e.getSourceNode().toString(), e.getTargetNode().toString());
				}
			}

			// Try to find a path with using the filtered graph
			dijkstra.init(filteredGraph);
			dijkstra.setSource(filteredGraph.getNode(idFrom));
			dijkstra.compute();//compute the distance to all nodes from idFrom
			path=dijkstra.getPath(filteredGraph.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
			iter=path.iterator();
			while (iter.hasNext()){
				shortestPath.add(iter.next().getId());
			}
			dijkstra.clear();
			if (!shortestPath.isEmpty()) { //The path exists
				shortestPath.remove(0);//remove the current position
				return shortestPath;
			}
		}

		// if a 'free path' doesn't exist, search for a path with the initial graph.
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}

	public Couple<String , Integer> getSecondClosestOpenNode(String myPosition){
		List<String> opennodes=getOpenNodes();

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
						.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
						.collect(Collectors.toList());

		List<Couple<String,Integer>> sortedOpen=lc.stream().sorted(Comparator.comparing(Couple::getRight)).collect(Collectors.toList());
		//3) Compute shorterPath
		if(sortedOpen.size()>1)
			return sortedOpen.get(1);
		return null;
	}


	public static SerializableSimpleGraph<String, MapAttribute> getGraphDifference(SerializableSimpleGraph<String, MapAttribute> currentMap, SerializableSimpleGraph<String, MapAttribute> sentMap)  {
		//The resultant graph which is the difference between the two graphs
		SerializableSimpleGraph <String, MapAttribute> new_g = new SerializableSimpleGraph <String, MapAttribute>();

		Set<SerializableNode<String, MapAttribute>> diffNodes = new HashSet<SerializableNode<String, MapAttribute>>();


		// get the difference between the two nodes lists (currentMap is the latest)
		for (SerializableNode <String, MapAttribute> currentNode: currentMap.getAllNodes()){
			if (!sentMap.getAllNodes().contains(currentNode)) {  // if the node is worth to be sent
				diffNodes.add(currentNode);
			}
		}

		// Add the edges
		for (SerializableNode <String, MapAttribute> n: diffNodes){
			for (Iterator<String> it = currentMap.getEdges(n.getNodeId()).iterator(); it.hasNext(); ) {
				String se = it.next();
				for (Iterator<SerializableNode<String, MapAttribute>> iter = currentMap.getAllNodes().iterator(); iter.hasNext(); ) {
					SerializableNode<String,MapAttribute> tmp = iter.next();
					if (tmp.getNodeId().equals(se)){
						new_g.addNode(n.getNodeId(),n.getNodeContent());
						new_g.addNode(tmp.getNodeId(), tmp.getNodeContent());
						new_g.addEdge("",n.getNodeId(), tmp.getNodeId());
					}
				}
			}
		}

		return new_g;
	}


	public static SerializableSimpleGraph <String, MapAttribute> getGraphUnion(SerializableSimpleGraph <String, MapAttribute> lastSent, SerializableSimpleGraph <String, MapAttribute> lastReceived){
		//The resultant graph which is the union between the two graphs
		SerializableSimpleGraph <String, MapAttribute> new_g = new SerializableSimpleGraph <String, MapAttribute>();


		// The nodes of the believed last knowledge of the target agent
		List<SerializableNode<String, MapAttribute>> nodesUnited = new ArrayList<>(lastSent.getAllNodes());

		for (SerializableNode<String, MapAttribute> n: lastReceived.getAllNodes()){
			if (!nodesUnited.contains(n)){
				nodesUnited.add(n);
			}
		}

		for (SerializableNode<String,MapAttribute> n: nodesUnited){
			new_g.addNode( n.getNodeId(), MapAttribute.valueOf((n.getNodeContent()).toString()));
		}

		for (SerializableNode<String,MapAttribute> n: nodesUnited){
			if (lastSent.getAllNodes().contains(n)) {
				for (String n2 : lastSent.getEdges(n.getNodeId()))
					new_g.addEdge(n.getNodeId() + n2, n.getNodeId(), n2);
			}
			if (lastReceived.getAllNodes().contains(n)) {
				for (String n2 : lastReceived.getEdges(n.getNodeId()))
					new_g.addEdge(n.getNodeId() + n2, n.getNodeId(), n2);
			}
		}

		return new_g;
	}





	public Couple<String , Integer> getClosestOpenNode(String myPosition) {
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		//3) Compute shorterPath
		if(closest !=null && closest.isPresent()) {
			return closest.get();
		}else{
			return null;
		}
	}



	public List<String> getOpenNodes(){
		return this.g.nodes()
				.filter(x ->x .getAttribute("ui.class")==MapAttribute.open.toString()) 
				.map(Node::getId)
				.collect(Collectors.toList());
	}


	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		serializeGraphTopology();

		closeGui();

		this.g=null;
	}

	/**
	 * Before sending the agent knowledge of the map it should be serialized.
	 */
	private void serializeGraphTopology() {
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}	
	}


	public synchronized SerializableSimpleGraph<String,MapAttribute> getSerializableGraph(){
		serializeGraphTopology();
		return this.sg;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public synchronized void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private synchronized void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			//Platform.runLater(() -> {
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			//});
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private synchronized void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);//GRAPH_IN_GUI_THREAD)
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);

		g.display();
	}

	public Graph getGraph(){
		return g;
	}

	public void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
		//System.out.println("You should decide what you want to save and how");
		//System.out.println("We currently blindy add the topology");

		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			//System.out.println(n);
			boolean alreadyIn =false;
			//1 Add the node
			Node newnode=null;
			try {
				newnode=this.g.addNode(n.getNodeId());
			}	catch(IdAlreadyInUseException e) {
				alreadyIn=true;
				//System.out.println("Already in"+n.getNodeId());
			}
			if (!alreadyIn) {
				newnode.setAttribute("ui.label", newnode.getId());
				newnode.setAttribute("ui.class", n.getNodeContent().toString());
			}else{
				newnode=this.g.getNode(n.getNodeId());
				//3 check its attribute. If it is below the one received, update it.
				if (((String) newnode.getAttribute("ui.class"))==MapAttribute.closed.toString() || n.getNodeContent().toString()==MapAttribute.closed.toString()) {
					newnode.setAttribute("ui.class",MapAttribute.closed.toString());
				}
			}
		}

		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			for(String s:sgreceived.getEdges(n.getNodeId())){
				addEdge(n.getNodeId(),s);
			}
		}
		//System.out.println("Merge done");
	}

	/**
	 * 
	 * @return true if there exist at least one openNode on the graph 
	 */
	public boolean hasOpenNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.open.toString())
				.findAny()).isPresent();
	}




}