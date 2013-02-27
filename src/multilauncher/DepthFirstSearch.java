package multilauncher;

import multilauncher.Graph.Vertex;


/**
 * Iterates over a graph in Depth First Search order
 */
public class DepthFirstSearch {
	public interface VertexListener {		
		/**
		 * Called when DFS descends to a vertex.
		 * @param to - lower vertex 
		 * @param from - higher vertex, can be null for tree root
		 * @param firstTime - true if "to" is unvisited
		 * @param nodeIndex - index of "to" in DFS sequence  
		 */
		void verticeVisited(Vertex from,  Vertex to, boolean firstTime, int nodeIndex);
		/**
		 * Called when DFS leaves a vertex (after processing all of its edges)
		 * @param from - vertex to be left 
		 * @param to - parent vertex, can be null for tree root
		 * @param nodeIndex - DFS index of vertex to be left 
		 */
		void verticeLeft(Vertex from, Vertex to, int nodeIndex);
	}

	private int _nextVisitIndex = 0;
	private Graph.Facet<Integer> _visitOrder;
	private VertexListener _callback;
	private Graph _graph;
	public DepthFirstSearch(Graph graph, VertexListener callback)
	{
		_callback = callback;
		_graph = graph;
		_visitOrder = graph.new Facet<Integer>(-1);
	}
	public void depthFirstSearch() {
		_nextVisitIndex = 0;
		for (Vertex vertex: _graph.getVertices()) {
			if (markVisit(vertex)) {
				int visit = _visitOrder.get(vertex);
				_callback.verticeVisited(null, vertex, true, visit);
				depthFirstSearch(vertex);
				_callback.verticeLeft(vertex, null, visit);
			}
		}
	}

	private boolean markVisit(Vertex vertex) {
		if (_visitOrder.get(vertex) < 0) {
			_visitOrder.set(vertex, _nextVisitIndex++);
			return true;
		}
		return false;
	}
	private boolean depthFirstSearch(Vertex start) {
		for(Vertex vertex: start.getNeighbors()) {
			boolean first = markVisit(vertex);
			_callback.verticeVisited(start, vertex, first, _visitOrder.get(vertex));
			if (first) {
				if (!depthFirstSearch(vertex))
					return false;
				_callback.verticeLeft(vertex, start, _visitOrder.get(vertex));
			}				
		}
		return true;
	}		
};
