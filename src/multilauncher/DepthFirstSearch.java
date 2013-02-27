package multilauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class DepthFirstSearch<T> {
	public interface VertexListener<T> {		
		/**
		 * Called when DFS descends to a vertex.
		 * @param to - lower vertex 
		 * @param from - higher vertex, can be null for tree root
		 * @param firstTime - true if to is unvisited
		 * @param nodeIndex - index of to in DFS sequence  
		 */
		void verticeVisited(Graph<T>.Vertex from,  Graph<T>.Vertex to, boolean firstTime, int nodeIndex);
		/**
		 * Called when DFS leaves a vertex (after processing all of its edges)
		 * @param from - vertex to be left 
		 * @param to - parent vertex, can be null for tree root
		 * @param nodeIndex - DFS index of vertex to be left 
		 */
		void verticeLeft(Graph<T>.Vertex from, Graph<T>.Vertex to, int nodeIndex);
	}

	private int _nextVisitIndex = 0;
	private ArrayList<Integer> _visitOrder =  new ArrayList<Integer>();
	private VertexListener<T> _callback;
	private Graph<T> _graph;
	public DepthFirstSearch(Graph<T> graph, VertexListener<T> callback)
	{
		_callback = callback;
		_graph = graph;
		_visitOrder.ensureCapacity(_graph.getVerticeCount());
	}
	public void depthFirstSearch() {
		reset();
		for (int i = 0; i < _graph.getVerticeCount(); ++i) {
			Graph<T>.Vertex vertex = _graph.getVertex(i);
			if (markVisit(vertex)) {
				int visit = _visitOrder.get(vertex.getId());
				_callback.verticeVisited(null, vertex, true, visit);
				depthFirstSearch(vertex);
				_callback.verticeLeft(vertex, null, visit);
			}
		}
	}

	private void reset() {
		_nextVisitIndex = 0;
		Collections.fill(_visitOrder, -1);
	}
	private boolean markVisit(Graph<T>.Vertex vertex) {
		if (vertex.getId() >= _visitOrder.size()) {
			_visitOrder.addAll(Collections.nCopies(vertex.getId() - _visitOrder.size() + 1, -1));
			assert(_visitOrder.size() == vertex.getId()+1);
		}
		if (_visitOrder.get(vertex.getId()) < 0) {
			_visitOrder.set(vertex.getId(), _nextVisitIndex++);
			return true;
		}
		return false;
	}
	private boolean depthFirstSearch(Graph<T>.Vertex start) {
		//TODO: laziness for getNeighbors seems to be a design error. Performance would be much better on native arrays without resizing
		Iterator<Graph<T>.Vertex> iterator = start.getNeighbors();
		while(iterator.hasNext()) {
			Graph<T>.Vertex vertex = iterator.next();
			boolean first = markVisit(vertex);
			_callback.verticeVisited(start, vertex, first, _visitOrder.get(vertex.getId()));
			if (first) {
				if (!depthFirstSearch(vertex))
					return false;
				_callback.verticeLeft(vertex, start, _visitOrder.get(vertex.getId()));
			}				
		}
		return true;
	}		
};
