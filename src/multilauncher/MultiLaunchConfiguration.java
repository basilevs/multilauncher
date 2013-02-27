package multilauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import multilauncher.Graph.Vertex;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class MultiLaunchConfiguration {
	public static boolean isMultiLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getType().getIdentifier().equals("Multilauncher.MultiLaunch");
	}
	public static List<String> getReferencesNames(ILaunchConfiguration configuration) throws CoreException {
		assert(isMultiLaunchConfiguration(configuration));
		@SuppressWarnings("rawtypes")
		List sequence = configuration.getAttribute(MultiLaunch.sequenceFieldName, Collections.emptyList());
		ArrayList<String> rv = new ArrayList<String>(sequence.size());
		for (Object item : sequence) {
			rv.add(item.toString());
		}
		return rv;
		
	}
	
	/**
	 * All configurations that can be referenced by current
	 * @param current - configuration of type MultiLaunch to analyze
	 * @return collection of valid references 
	 * @throws CoreException
	 */
	public static Map<String, ILaunchConfiguration> getPossibleReferences(ILaunchConfiguration current) throws CoreException
	{
		//TODO: consider another key to persist configuration association on rename
		//TODO: deal with raw types
		ILaunchConfiguration[] configurations = getLaunchManager().getLaunchConfigurations();
		HashMap<String, ILaunchConfiguration> rv = new HashMap<String, ILaunchConfiguration>(configurations.length);
		@SuppressWarnings("rawtypes")
		Set requiredModes = current.getModes(); // Actually just run and debug
		for (ILaunchConfiguration iLaunchConfiguration : configurations) {				
			@SuppressWarnings("rawtypes")
			Set modes= iLaunchConfiguration.getModes();
			if (!modes.containsAll(requiredModes))
				continue;
			//TODO: More filters are probably needed here
			//WARN: relies on unique configuration naming (provided by IDE)
			rv.put(iLaunchConfiguration.getName(), iLaunchConfiguration);
		}
		//Cycle prevention. Existing cycles and those that can be created by a link to current configuration are eliminated.
		ConfGraph graph = new ConfGraph(rv.values());
		//Current configuration has access to itself
		graph.accesors.set(graph.getVertexByName(current.getName()), true); 
		//Detect configurations with access to current or those with cycles
		//Note, that for cases when current dependency graph is correct,
		//a much simpler algorithm can be used (cycle detection is not required - only access to current configuration could be checked)
		//It is due to this protection sequence don't even need to be checked on save 
		CycleDetector.haveAccessToOrCycle(graph, graph.accesors);
		//Filtering out those configurations that has access (or are in cycle)
		for(Vertex vertex: graph.getVertices())
			if (graph.accesors.get(vertex))
				rv.remove(graph.configuration.get(vertex).getName());
		return rv;
	}
	
	private static class ConfGraph extends Graph {
		Map<String, Vertex> _vertices = new HashMap<String, Vertex>();
		public final Facet<ILaunchConfiguration> configuration = new Facet<ILaunchConfiguration>(null);
		public final Facet<Boolean> accesors = new Facet<Boolean>(false);
		public ConfGraph(Iterable<ILaunchConfiguration> configurations) throws CoreException {
			for (ILaunchConfiguration conf: configurations) {
				if (!isMultiLaunchConfiguration(conf))
					continue;
				Vertex vertex = new Vertex();
				configuration.set(vertex, conf);
				_vertices.put(conf.getName(), vertex);
			}
			for (Vertex vertex: _vertices.values()) {
				for(String name: getReferencesNames(configuration.get(vertex))) {
					Vertex neighbor = _vertices.get(name);
					if (neighbor==null)
						continue;
					vertex.addEdge(neighbor);
				}
			}			
		}
		public Vertex getVertexByName(String name) {
			return _vertices.get(name);
		}
	}
	
	
	/**
	 * Configurations referenced by current
	 * @param current -  configuration of type MultiLaunch to analyze
	 * @return collection of active references
	 * @throws CoreException
	 */
	public static Collection<ILaunchConfiguration> getReferences(ILaunchConfiguration current, Map<String, ILaunchConfiguration> allPossibleConfigurations) throws CoreException
	{
		//WARN: configuration selection is lost on rename
		Collection<String> sequence = getReferencesNames(current);
		ArrayList<ILaunchConfiguration> rv = new ArrayList<ILaunchConfiguration>(sequence.size());
		if (allPossibleConfigurations == null)
			allPossibleConfigurations = getPossibleReferences(current);
		for (String name: sequence) {
			ILaunchConfiguration configuration = allPossibleConfigurations.get(name);
			if (configuration==null) {
				DebugPlugin.logMessage("A reference to invalid configuration " + name + " detected", null);
				continue;
			}
			rv.add(configuration);
		}
		return rv;
	}
	
	public static Map<String, ILaunchConfiguration> getAll() throws CoreException {
		Map<String, ILaunchConfiguration> rv = new HashMap<String, ILaunchConfiguration>();
		ILaunchConfiguration[] configurations = MultiLaunchConfiguration.getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration iLaunchConfiguration : configurations) {
			//TODO: ensure that id comparison is enough to select configurations of our type
			if (!MultiLaunchConfiguration.isMultiLaunchConfiguration(iLaunchConfiguration))
				continue;
			rv.put(iLaunchConfiguration.getName(), iLaunchConfiguration);
		}
		return rv;
	}
	
	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	


}
