package multilauncher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "Multilauncher"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * Watcher for deletions and renames. References to renamed configurations
	 * are persisted. References to deleted configurations are cleaned out.
	 */
	final ILaunchConfigurationListener _configurationListener = new ILaunchConfigurationListener() {

		public void updateConfiguration(ILaunchConfiguration configuration, List<String> sequence) throws CoreException {
			assert (MultiLaunchConfiguration.isMultiLaunchConfiguration(configuration));
			ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
			workingCopy.setAttribute(MultiLaunch.sequenceFieldName, sequence);
			// TODO: is it ok not to save parent?
			workingCopy.doSave();
		}

		public void launchConfigurationRenamed(final ILaunchConfiguration movedFrom,
				final ILaunchConfiguration movedTo) {
			try {
				for (ILaunchConfiguration configuration : MultiLaunchConfiguration.getAll().values()) {
					List<String> sequence = MultiLaunchConfiguration.getReferencesNames(configuration);
					// TODO: consider smarter in-place replace
					List<String> newSequence = new ArrayList<String>(sequence.size());
					Boolean modified = false;
					for (String name : sequence) {
						if (name.equals(movedFrom.getName()))
							name = movedTo.getName();
						newSequence.add(name);
						modified = true;
					}
					if (modified)
						updateConfiguration(configuration, newSequence);
				}
			} catch (CoreException e) {
				// TODO: consider better exception handling
				DebugPlugin.log(e);
			}
		}

		@Override
		public void launchConfigurationRemoved(final ILaunchConfiguration configuration) {
			ILaunchConfiguration movedTo = MultiLaunchConfiguration.getLaunchManager().getMovedTo(configuration);
			if (movedTo != null) {
				launchConfigurationRenamed(configuration, movedTo);
				return;
			}
			// when referenced configuration is removed, we should ensure that
			// another configuration with same name won't become referenced
			// instead
			try {
				for (ILaunchConfiguration cleanupCandidate : MultiLaunchConfiguration.getAll().values()) {
					List<String> newSequence = MultiLaunchConfiguration.getReferencesNames(cleanupCandidate);
					if (newSequence.remove(configuration.getName()))
						updateConfiguration(cleanupCandidate, newSequence);

				}
			} catch (CoreException e) {
				// TODO: consider better exception handling
				DebugPlugin.log(e);
			}
		}

		@Override
		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		}

		@Override
		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		}
	};

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		MultiLaunchConfiguration.getLaunchManager().addLaunchConfigurationListener(
				_configurationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		MultiLaunchConfiguration.getLaunchManager().removeLaunchConfigurationListener(
				_configurationListener);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

}
