package multilauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Complexity: n
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
			//TODO: Prevent configuration cycles (deep detection)
			if (iLaunchConfiguration.contentsEqual(current))
				continue;
			@SuppressWarnings("rawtypes")
			Set modes= iLaunchConfiguration.getModes();
			if (!modes.containsAll(requiredModes))
				continue;
			
			//TODO: More filters are probably needed here
			//WARN: relies on unique configuration naming (provided by IDE)
			rv.put(iLaunchConfiguration.getName(), iLaunchConfiguration);
		}
		return rv;
	}
	
	
	/**
	 * Configurations referenced by current
	 * @param current -  configuration of type MultiLaunch to analyze
	 * @return collection of active references
	 * @throws CoreException
	 */
	public static Collection<ILaunchConfiguration> getReferences(ILaunchConfiguration current) throws CoreException
	{
		//WARN: configuration selection is lost on rename
		Collection<String> sequence = getReferencesNames(current);
		ArrayList<ILaunchConfiguration> rv = new ArrayList<ILaunchConfiguration>(sequence.size());
		Map<String, ILaunchConfiguration> configurations = getPossibleReferences(current);
		for (String name: sequence) {
			ILaunchConfiguration configuration = configurations.get(name);
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
	
	public static void detectCycles(ILaunchConfiguration start,  Map<String, ILaunchConfiguration> cycled, Map<String, ILaunchConfiguration> all) throws CoreException {
		cycled.put(start.getName(), start);
		for (String referenceName: getReferencesNames(start)) {
			if (cycled.get(referenceName) != null) {
				
			}
			ILaunchConfiguration configuration = all.get(referenceName);
			assert(isMultiLaunchConfiguration(configuration));
			
		}
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	


}
