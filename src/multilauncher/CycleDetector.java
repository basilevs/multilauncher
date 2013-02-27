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
			private final Stack<Graph<T>.Vertex> _stack = new Stack<Graph<T>.Vertex>();
			private <V> void expandArray(Graph<T>.Vertex vertex, ArrayList<V> array, V value) {
				if (array.size() <= vertex.getId())
					array.addAll(Collections.nCopies(vertex.getId() - array.size() + 1, value));
			}
			private void expandFlagArray(Graph<T>.Vertex vertex) {
				expandArray(vertex, hasAccess, false);
				expandArray(vertex, _lowLink, Integer.MAX_VALUE);
			}
			private int getLowLink(Graph<T>.Vertex vertex) {
				return _lowLink.get(vertex.getId());
			}
			private void setLowLink(Graph<T>.Vertex vertex, int index) {
				_lowLink.set(vertex.getId(), Math.min(getLowLink(vertex), index));
			}
			@Override
			public void verticeVisited(Graph<T>.Vertex from, Graph<T>.Vertex to, boolean firstTime, int visitIndex) {
				expandFlagArray(to);
				if (firstTime) {
					assert(getLowLink(to) == Integer.MAX_VALUE);
					setLowLink(to, visitIndex);
					assert(from != null || _stack.size() == 0);
					_stack.push(to);
				} else {
					if (from != null && _stack.contains(to)) {
						setLowLink(from, getLowLink(to)); //cycle detected
					}
				}
				if (from != null && hasAccess.get(to.getId())) {
					hasAccess.set(from.getId(), true); 
				}
			}
			@Override
			public void verticeLeft(Graph<T>.Vertex from, Graph<T>.Vertex to, int visitIndex) {
				
				if (visitIndex ==  getLowLink(from)) {
					ArrayList<Graph<T>.Vertex> strongComponent = new ArrayList<Graph<T>.Vertex>();
					while(_stack.size() > 0) {
						Graph<T>.Vertex cycled = _stack.pop();
						strongComponent.add(cycled); 
						if (cycled == from)
							break;
					}
					//marking whole cycle as bad, leaving single nodes as is
					if (strongComponent.size() > 1) {
						for (Graph<T>.Vertex vertex: strongComponent) {
							hasAccess.set(vertex.getId(), true); 
						}
					}
				}
				if (to != null) {
					setLowLink(to,  getLowLink(from));
					if (hasAccess.get(from.getId())) {
						hasAccess.set(to.getId(), true);
					}
				}
			}
		};
		DepthFirstSearch<T> dfs = new DepthFirstSearch<T>(graph, callback);
		dfs.depthFirstSearch();
	}

}
