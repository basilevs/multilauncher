package multilauncher;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import multilauncher.DepthFirstSearch.VertexListener;
import multilauncher.Graph.Vertex;

import org.junit.Test;

public class GraphTest {

	static class StringGraph extends Graph<String> {
		private Map<String, StringVertex> _vertices = new HashMap<String, StringVertex>();

		public class StringVertex extends Graph<String>.Vertex 
		{
			String[] _neighbors;
			public StringVertex(String name, String[] neighbors)
			{
				super(name);
				_neighbors = neighbors;
				_vertices.put(name, this);
			}
			@Override
			public Iterator<Graph<String>.Vertex> getNeighbors() {
				ArrayList<Graph<String>.Vertex> rv = new ArrayList<Graph<String>.Vertex>(_neighbors.length);
				for (String name: _neighbors) {
					StringVertex cached  = _vertices.get(name);
					assert(cached != null);
					rv.add(cached);
				}
				return rv.iterator();
			}			
		}
		StringVertex getVertex(String name) {
			return _vertices.get(name);
		}	
		void mark(String name, ArrayList<Boolean> accessMap) {
			int id = getVertex(name).getId(); 
			if (accessMap.size() < getVerticeCount())
				accessMap.addAll(Collections.nCopies(id - accessMap.size() + 1, false));
			accessMap.set(id, true);
		}
	}
	
	static StringGraph create() {
		//        B
		//        ^
		//        |
		//   C <- A <- G
		//   |    |    ^
		//   .    .   |
		//   E <- D --/
		//
		//
		StringGraph rv = new StringGraph();
		rv. new StringVertex("A", new String[]{"B", "C", "D", "G"});
		rv. new StringVertex("B", new String[]{});
		rv. new StringVertex("C", new String[]{"E"});
		rv. new StringVertex("D", new String[]{"E", "G"});
		rv. new StringVertex("E", new String[]{});		
		rv. new StringVertex("G", new String[]{"A"});		
		return rv;
	}
	@Test
	public void testCreate() {
		StringGraph graph = create();
		assert(graph.getVerticeCount()==6);
	}
	@Test
	public void testDFS() {	
		class TestListener implements VertexListener<String> {
			public int visitCount = 0;
			@Override
			public void verticeVisited(Vertex from, Vertex to, boolean firstTime) {
				if (firstTime)
					visitCount++;
			}

			@Override
			public void verticeLeft(Vertex from, Vertex to) {
			}
		};
		TestListener callback = new TestListener();
		StringGraph graph = create();
		DepthFirstSearch<String> dfs = new DepthFirstSearch<String>(graph, callback);
		dfs.depthFirstSearch();
		assert(callback.visitCount == graph.getVerticeCount()); 
	}
	static void ensureChecked(StringGraph graph, ArrayList<Boolean> checks, String shouldBeChecked, String shouldBeUnchecked) {
		StringBuffer checked = new StringBuffer();
		StringBuffer unchecked = new StringBuffer();
		for (int i = 0; i < checks.size(); ++i) {
			if (checks.get(i)) {
				checked.append(graph.getVertex(i).getPayload());
			} else {
				unchecked.append(graph.getVertex(i).getPayload());
			}
		}
		assertEquals(shouldBeChecked, checked);
		assertEquals(shouldBeUnchecked, unchecked);
	} 

	@Test
	public void testCycleCheck() {
		StringGraph graph = create();
		ArrayList<Boolean> hasAccess = new ArrayList<Boolean>();
		graph.mark("A", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "AGD", "BCE");
	}

}
