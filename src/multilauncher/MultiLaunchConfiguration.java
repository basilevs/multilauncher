package multilauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class MultiLaunchConfiguration {
	public static boolean isMultiLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getType().getIdentifier().equals("Multilauncher.MultiLaunch");
	}
	static interface Action {
		void act(ILaunchConfiguration configuration)  throws CoreException;
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
	//
	
	/**
	 * All configurations that can be referenced by current
	 * Complexity: n
	 * @param current - configuration of type MultiLaunch to analyze
	 * @return collection of valid references 
	 * @throws CoreException
	 */
	public static Dictionary<String, ILaunchConfiguration> getPossibleReferences(ILaunchConfiguration current) throws CoreException
	{
		//TODO: consider another key to persist configuration association on rename
		//TODO: deal with raw types
		ILaunchConfiguration[] configurations = getLaunchManager().getLaunchConfigurations();
		Hashtable<String, ILaunchConfiguration> rv = new Hashtable<String, ILaunchConfiguration>(configurations.length);
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
		Dictionary<String, ILaunchConfiguration> configurations = getPossibleReferences(current);
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
	
	public static Dictionary<String, ILaunchConfiguration> getAll() throws CoreException {
		Dictionary<String, ILaunchConfiguration> rv = new Hashtable<String, ILaunchConfiguration>();
		ILaunchConfiguration[] configurations = MultiLaunchConfiguration.getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration iLaunchConfiguration : configurations) {
			//TODO: ensure that id comparison is enough to select configurations of our type
			if (!MultiLaunchConfiguration.isMultiLaunchConfiguration(iLaunchConfiguration))
				continue;
			rv.put(iLaunchConfiguration.getName(), iLaunchConfiguration);
		}
		return rv;
	}

	//TODO: consider replacing with direct iteration
	public static void scanAll(Action action) throws CoreException {
		Dictionary<String, ILaunchConfiguration> ourConfigurations = MultiLaunchConfiguration.getAll();
		Enumeration<ILaunchConfiguration> enumeration = ourConfigurations.elements();
		scan(action, enumeration);
	}
	public static void scan(Action action, Enumeration<ILaunchConfiguration> enumeration) throws CoreException {
		while (enumeration.hasMoreElements()) {
			ILaunchConfiguration configuration = enumeration.nextElement();
			action.act(configuration);
		}
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	


}
