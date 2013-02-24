package multilauncher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class MultiLaunch implements ILaunchConfigurationDelegate {
	static final String sequenceFieldName = "sequence";
	public static Dictionary<String, ILaunchConfiguration> getValidConfigurations(ILaunchConfiguration current) throws CoreException
	{
		//TODO: consider another key to persist configuration association on rename
		//TODO: deal with raw types
		ILaunchConfiguration[] configurations = getLaunchManager().getLaunchConfigurations();
		Hashtable<String, ILaunchConfiguration> rv = new Hashtable<>(configurations.length);
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
	public static Collection<ILaunchConfiguration> getConfigurationsToRun(ILaunchConfiguration current) throws CoreException
	{
		//WARN: configuration selection is lost on rename
		@SuppressWarnings("rawtypes")
		List sequence = current.getAttribute(MultiLaunch.sequenceFieldName, Collections.emptyList());
		ArrayList<ILaunchConfiguration> rv = new ArrayList<ILaunchConfiguration>(sequence.size());
		Dictionary<String, ILaunchConfiguration> configurations = getValidConfigurations(current);
		for (Object name: sequence) {
			ILaunchConfiguration configuration = configurations.get(name);
			if (configuration!=null)
				rv.add(configuration);
		}
		return rv;
	}
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		for (ILaunchConfiguration toRun : getConfigurationsToRun(configuration)) {
			//TODO: progress might be shown incorrectly
			//TODO: we should probably process ILaunch return value somehow
			toRun.launch(mode, monitor);
		}
	}

	protected static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
