package multilauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import multilauncher.DepthFirstSearch.VertexListener;

public class CycleDetector {
	/**
	 * Finds all vertices that reference given ones or belong to any cycle
	 * Modified Tarjan algorithm http://en.wikipedia.org/wiki/Tarjan’s_strongly_connected_components_algorithm
	 * @param isAccessible - vertices that are checked. Updated during execution (filled with those referencing current)  
	 */
	public static <T> void  haveAccessToOrCycle(Graph<T> graph, final ArrayList<Boolean> hasAccess) 
	{
		VertexListener<T> callback = new VertexListener<T>() {
			private final ArrayList<Integer> _lowLink =  new ArrayList<Integer>();
			private final Stack<Integer> _stack = new Stack<Integer>();
			private int _visitIndex = 0;
			public <V> void expandArray(Graph<T>.Vertex vertex, ArrayList<V> array, V value) {
				if (array.size() <= vertex.getId())
					array.addAll(Collections.nCopies(vertex.getId() - array.size() + 1, value));
			}
			public void expandFlagArray(Graph<T>.Vertex vertex) {
				expandArray(vertex, hasAccess, false);
				expandArray(vertex, _lowLink, Integer.MAX_VALUE);
			}
			@Override
			public void verticeVisited(Graph<T>.Vertex from, Graph<T>.Vertex to, boolean firstTime) {
				//If referenced object is accessible, parent is also accessible.
				//If referenced object is already visited, this edge is part of the cycle.
				expandFlagArray(to);
				if (firstTime) {
					assert(_lowLink.get(to.getId()) == Integer.MAX_VALUE);
					_lowLink.set(to.getId(), _visitIndex);
					_lowLink.set(from.getId(), Math.min(_lowLink.get(from.getId()), _visitIndex));
					_visitIndex++;
				}
				if (_lowLink.get(from.getId()) > _lowLink.get(to.getId())) {
					hasAccess.set(to.getId(), true); //cycle detected
				}
				if (hasAccess.get(to.getId())) {
					hasAccess.set(from.getId(), true); 
				}
			}			
			@Override
			public void verticeLeft(Graph<T>.Vertex from, Graph<T>.Vertex to) {
				//If referenced object is accessible, parent is also accessible.
				if (hasAccess.get(from.getId())) {
					hasAccess.set(to.getId(), true);
				}
			}
		};
		DepthFirstSearch<T> dfs = new DepthFirstSearch<T>(graph, callback);
		dfs.depthFirstSearch();
	}

}
