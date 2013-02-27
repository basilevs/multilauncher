package multilauncher;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import multilauncher.DepthFirstSearch.VertexListener;

import org.junit.Test;

public class GraphTest {

	static class StringGraph extends Graph<String> {
		private Map<String, StringVertex> _vertices = new HashMap<String, StringVertex>();

		public class StringVertex
		{
			Graph<String>.Vertex _vertex;
			String[] _neighbors;
			public StringVertex(String name, String[] neighbors)
			{
				_neighbors = neighbors;
				_vertex= new Vertex(name);
				_vertices.put(name, this);
			}
		}
		void build() {
			for (StringVertex sv: _vertices.values()) {
				for (String name: sv._neighbors) {
					StringVertex cached  = _vertices.get(name);
					assert(cached != null);
					sv._vertex.addEdge(cached._vertex);
				}
			}
		}
		StringVertex getVertex(String name) {
			return _vertices.get(name);
		}	
		void mark(String name, ArrayList<Boolean> accessMap) {
			int id = getVertex(name)._vertex.getId(); 
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
		rv. new StringVertex("A", new String[]{"B", "C", "D"});
		rv. new StringVertex("B", new String[]{});
		rv. new StringVertex("C", new String[]{"E"});
		rv. new StringVertex("D", new String[]{"E", "G"});
		rv. new StringVertex("E", new String[]{});		
		rv. new StringVertex("G", new String[]{"A"});
		rv.build();
		return rv;
	}
	@Test
	public void testCreate() {
		StringGraph graph = create();
		assert(graph.getVerticeCount()==6);
	}
	@Test
	public void testSimpleCycle() {
		StringGraph graph= new StringGraph();
		graph.new StringVertex("A", new String[]{"B"});
		graph.new StringVertex("B", new String[]{"A"});
		graph.build();
		ArrayList<Boolean> hasAccess = new ArrayList<Boolean>();
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "AB", "");
	}
	@Test
	public void testTriCycle() {
		StringGraph graph= new StringGraph();
		graph.new StringVertex("A", new String[]{"B"});
		graph.new StringVertex("B", new String[]{"C"});
		graph.new StringVertex("C", new String[]{});
		graph.build();
		ArrayList<Boolean> hasAccess = new ArrayList<Boolean>();
		graph.mark("C", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "ABC", "");
	}
	@Test
	public void testDFS() {	
		class TestListener implements VertexListener<String> {
			public int visitCount = 0;
			@Override
			public void verticeVisited(Graph<String>.Vertex from, Graph<String>.Vertex to, boolean firstTime, int index) {
				if (firstTime)
					visitCount++;
			}

			@Override
			public void verticeLeft(Graph<String>.Vertex from, Graph<String>.Vertex to, int index) {
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
		assertEquals(shouldBeChecked, checked.toString());
		assertEquals(shouldBeUnchecked, unchecked.toString());
	} 

	@Test
	public void testCycleCheck() {
		StringGraph graph = create();
		ArrayList<Boolean> hasAccess = new ArrayList<Boolean>();
		graph.mark("A", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "ADG", "BCE");
		Collections.fill(hasAccess, false);
		graph.mark("B", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "ABDG", "CE");
		Collections.fill(hasAccess, false);
		graph.mark("E", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "ACDEG", "B");
	}
	@Test
	public void testUnconnected() {
		StringGraph graph= new StringGraph();
		graph.new StringVertex("A", new String[]{});
		graph.new StringVertex("B", new String[]{});
		graph.new StringVertex("C", new String[]{});
		graph.new StringVertex("D", new String[]{});
		ArrayList<Boolean> hasAccess = new ArrayList<Boolean>();
		graph.mark("D", hasAccess);
		CycleDetector.haveAccessToOrCycle(graph, hasAccess);
		ensureChecked(graph, hasAccess, "D", "ABC");
	}

}
