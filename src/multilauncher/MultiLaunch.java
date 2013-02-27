package multilauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;


public class MultiLaunch implements ILaunchConfigurationDelegate {
	static final String sequenceFieldName = "sequence";

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//Children have their own launch object. This one is not needed.
		MultiLaunchConfiguration.getLaunchManager().removeLaunch(launch); 
		for (ILaunchConfiguration toRun : MultiLaunchConfiguration.getReferences(configuration, null)) {
			//TODO: progress might be shown incorrectly
			//TODO: we should probably process ILaunch return value somehow
			toRun.launch(mode, monitor);
		}
	}

}
