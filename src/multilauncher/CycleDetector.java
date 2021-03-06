package multilauncher;

import java.util.ArrayList;
import java.util.Stack;

import multilauncher.DepthFirstSearch.VertexListener;
import multilauncher.Graph.Facet;
import multilauncher.Graph.Vertex;

public class CycleDetector {
	/**
	 * Finds all vertices that reference given ones or belong to any cycle
	 * Modified Tarjan algorithm http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
	 * @param hasAccess - holds true for vertices that are already accessible. Updated during execution (filled with those referencing already accessible)  
	 */
	public static void haveAccessToOrCycle(final Graph graph, final Facet<Boolean> hasAccess) 
	{
		VertexListener callback = new VertexListener() {
			private final Facet<Integer> _lowLink =  graph.new Facet<Integer>(Integer.MAX_VALUE);
			private final Stack<Vertex> _stack = new Stack<Vertex>();
			private int  getLowLink(Vertex vertex) {
				return _lowLink.get(vertex);
			}
			private void setLowLink(Vertex vertex, int index) {
				_lowLink.set(vertex, Math.min(getLowLink(vertex), index));
			}
			@Override
			public void verticeVisited(Vertex from, Vertex to, boolean firstTime, int visitIndex) {
				assert(to != null);
				assert(visitIndex >= 0);
				if (firstTime) {
					assert(getLowLink(to) == Integer.MAX_VALUE);
					setLowLink(to, visitIndex);
					assert(from != null || _stack.size() == 0);
					_stack.push(to);
				} else {
					if (from != null && _stack.contains(to))  //cycle detected
						setLowLink(from, getLowLink(to)); 
				}
				//Access poisoning goes up through DFS stack
				if (from != null) {
					if (hasAccess.get(to)) {
						hasAccess.set(from, true);
					}
				}
			}
			@Override
			public void verticeLeft(Vertex from, Vertex to, int visitIndex) {
				assert(from != null);
				assert(visitIndex >= 0);
				if (visitIndex ==  getLowLink(from)) { //Root of strongly connected component found
					//TODO: is a separate container required here?
					ArrayList<Vertex> strongComponent = new ArrayList<Vertex>();
					while(_stack.size() > 0) {
						Vertex cycled = _stack.pop();
						strongComponent.add(cycled); 
						if (cycled == from)
							break;
					}
					//marking whole component as bad, leaving single nodes as is
					if (strongComponent.size() > 1) {
						for (Vertex vertex: strongComponent) {
							hasAccess.set(vertex, true); 
						}
					}
				}
				if (to != null) {
					setLowLink(to,  getLowLink(from));
					//Access poisoning goes up through DFS stack
					if (hasAccess.get(from)) {
						hasAccess.set(to, true);
					}
				}
			}
		};
		DepthFirstSearch dfs = new DepthFirstSearch(graph, callback);
		dfs.depthFirstSearch();
	}

}
