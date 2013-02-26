package multilauncher;

import java.util.ArrayList;
import java.util.Iterator;

public class Graph<T> {
	private final ArrayList<Vertex> _vertices = new ArrayList<Vertex>();
	
	public int getVerticeCount() {
		return _vertices.size();
	}
	public Graph<T>.Vertex getVertex(int id) {
		Vertex rv = _vertices.get(id);
		assert(rv.getId() == id);
		return rv;
	} 
	
	public abstract class Vertex {
		final int _id;
		private final T _payload;
		public T getPayload() {return _payload;}
		public int getId() {return _id;}
		public Vertex(T payload) {
			_payload = payload;
			_id = _vertices.size();
			_vertices.add(this);
		}
		
		/**
		 * Iterates over neighbors, can create new vertices
		 * @return iterator over neighbors
		 */
		public abstract Iterator<Vertex> getNeighbors(); 
	}

}
