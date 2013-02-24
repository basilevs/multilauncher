package multilauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class MultiLaunch implements ILaunchConfigurationDelegate {
	static final String sequenceFieldName = "sequence";
	public static Dictionary<String, ILaunchConfiguration> getValidConfigurations(ILaunchConfiguration current) throws CoreException
	{
		ILaunchConfiguration[] configurations = getLaunchManager().getLaunchConfigurations();
		Hashtable<String, ILaunchConfiguration> rv = new Hashtable<>(configurations.length);
		@SuppressWarnings("rawtypes")
		Set modes = current.getModes();
		for (ILaunchConfiguration iLaunchConfiguration : configurations) {				
			//TODO: Prevent configuration cycles (deep detection)
			if (iLaunchConfiguration.contentsEqual(current))
				continue;
			if (!iLaunchConfiguration.getModes().containsAll(modes))
				continue;
			
			//TODO: More filters are probably needed here
			//WARN: relies on unique configuration naming (provided by IDE)
			rv.put(iLaunchConfiguration.getName(), iLaunchConfiguration);
		}
		return rv;
	}
	public static Collection<ILaunchConfiguration> getConfigurationsToRun(ILaunchConfiguration current) throws CoreException
	{
		@SuppressWarnings("rawtypes")
		List sequence = current.getAttribute(MultiLaunch.sequenceFieldName, Collections.emptyList());
		ArrayList<ILaunchConfiguration> rv = new ArrayList<ILaunchConfiguration>(sequence.size());
		Dictionary<String, ILaunchConfiguration> configurations = getValidConfigurations(current);
		for (Object name: sequence) {			
			rv.add(configurations.get(name));
		}
		return rv;
	}
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		for (ILaunchConfiguration toRun : getConfigurationsToRun(configuration)) {
			//TODO: progress is shown incorrectly
			toRun.launch(mode, monitor);
		}
	}

	protected static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
