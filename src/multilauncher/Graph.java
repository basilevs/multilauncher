package multilauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class Graph {
	private final ArrayList<Vertex> _vertices = new ArrayList<Vertex>();
	@SuppressWarnings("rawtypes")
	private final ArrayList<Facet> _facets = new ArrayList<Facet>();
	
	public Collection<Vertex> getVertices() {
		return _vertices;
	}
	
	
	/**
	 * Typesafe access to vertex payload
	 * Each instance of Facet manipulates its own data.
	 */
	class Facet<T> {
		private int _id;
		private T _default;
		public Facet(T defaultValue) {
			_default = defaultValue;
			_id = _facets.size();
			_facets.add(this);
		}
		T get(Vertex vertex) {
			//TODO: protect against vertices from alien graph
			if (vertex._payload.size() <= _id)
				return _default;
			Object obj = vertex._payload.get(_id);
			if (obj == null)
				return _default;
			@SuppressWarnings("unchecked")
			T t = (T)obj;
			return t;
		}
		void set(Vertex vertex, T payload) {
			//Extra boxing happens here. Our performance goes only so far for now.
			assert(_id < _facets.size());
			assert(_facets.get(_id) == this);
			if (vertex._payload.size() <= _facets.size())
				vertex._payload.addAll(Collections.nCopies(_facets.size() - vertex._payload.size(), null));
			vertex._payload.set(_id, payload);
		}
	}
	
	public class Vertex {
		//int _id; was here but now is replaced by facets 
		private ArrayList<Object> _payload = new ArrayList<Object>(); 
		private	Collection<Vertex> _neighbors = new ArrayList<Vertex>();
		public Vertex() {
			_vertices.add(this);
		}
		
		/**
		 * Iterates over neighbors, can create new vertices
		 * @return iterator over neighbors
		 */
		public Collection<Vertex> getNeighbors() {
			return _neighbors;
		}
		public void addEdge(Vertex vertex) {
			_neighbors.add(vertex);
		}
	}

}
