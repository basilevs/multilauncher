package multilauncher;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import multilauncher.DepthFirstSearch.VertexListener;
import multilauncher.Graph.Vertex;

import org.junit.Test;

public class GraphTest {

	static class StringGraph extends Graph {
		private Map<String, Vertex> _vertices = new HashMap<String, Vertex>();
		private Facet<String> _name = new Facet<String>("");
		private Facet<String[]> _edges= new Facet<String[]>(new String[]{});
		public Facet<Boolean> hasAccess = new Facet<Boolean>(false);
		
		public Vertex addVertex(String name, String[] edges) {
			 Vertex rv = new Vertex();
			_name.set(rv, name);
			_edges.set(rv, edges);
			_vertices.put(name, rv);
			return rv;
		}
		void build() {
			for (Vertex sv: _vertices.values()) {
				for (String name: _edges.get(sv)) {
					Vertex cached  = _vertices.get(name);
					assert(cached != null);
					sv.addEdge(cached);
				}
			}
		}
		Vertex getVertex(String name) {
			Vertex rv = _vertices.get(name);
			assert(rv != null);
			return rv;
		}	
		void mark(String name) {
			hasAccess.set(getVertex(name), true);
		}
		String getName(Vertex vertex) {
			return _name.get(vertex);
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
		rv.addVertex("A", new String[]{"B", "C", "D"});
		rv.addVertex("B", new String[]{});
		rv.addVertex("C", new String[]{"E"});
		rv.addVertex("D", new String[]{"E", "G"});
		rv.addVertex("E", new String[]{});		
		rv.addVertex("G", new String[]{"A"});
		rv.build();
		return rv;
	}
	@Test
	public void testCreate() {
		StringGraph graph = create();
		assert(graph.getVertices().size()==6);
	}
	@Test
	public void testSimpleCycle() {
		StringGraph graph= new StringGraph();
		graph.addVertex("A", new String[]{"B"});
		graph.addVertex("B", new String[]{"A"});
		graph.build();
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "AB", "");
	}
	@Test
	public void testTriCycle() {
		StringGraph graph= new StringGraph();
		graph.addVertex("A", new String[]{"B"});
		graph.addVertex("B", new String[]{"C"});
		graph.addVertex("C", new String[]{});
		graph.build();
		graph.mark("C");
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "ABC", "");
	}
	@Test
	public void testDFS() {	
		class TestListener implements VertexListener {
			public int visitCount = 0;
			@Override
			public void verticeVisited(Vertex from, Vertex to, boolean firstTime, int index) {
				if (firstTime)
					visitCount++;
			}

			@Override
			public void verticeLeft(Vertex from, Vertex to, int index) {
			}
		};
		TestListener callback = new TestListener();
		StringGraph graph = create();
		DepthFirstSearch dfs = new DepthFirstSearch(graph, callback);
		dfs.depthFirstSearch();
		assertEquals(graph.getVertices().size(), callback.visitCount); 
	}
	static void ensureChecked(StringGraph graph, String shouldBeChecked, String shouldBeUnchecked) {
		StringBuffer checked = new StringBuffer();
		StringBuffer unchecked = new StringBuffer();
		for (Vertex vertex: graph.getVertices()) {
			if (graph.hasAccess.get(vertex)) {
				checked.append(graph.getName(vertex));
			} else {
				unchecked.append(graph.getName(vertex));
			}
		}
		assertEquals(shouldBeChecked, checked.toString());
		assertEquals(shouldBeUnchecked, unchecked.toString());
	} 

	@Test
	public void testCycleCheck() {
		StringGraph graph = create();
		graph.mark("A");
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "ADG", "BCE");
		graph = create();
		graph.mark("B");
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "ABDG", "CE");
		graph = create();
		graph.mark("E");
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "ACDEG", "B");
	}
	@Test
	public void testUnconnected() {
		StringGraph graph= new StringGraph();
		graph.addVertex("A", new String[]{});
		graph.addVertex("B", new String[]{});
		graph.addVertex("C", new String[]{});
		graph.addVertex("D", new String[]{});
		graph.mark("D");
		CycleDetector.haveAccessToOrCycle(graph, graph.hasAccess);
		ensureChecked(graph, "D", "ABC");
	}

}
