package multilauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SequenceTab extends AbstractLaunchConfigurationTab {
	private CheckboxTableViewer _launchesViewer;

	@Override
	public void createControl(Composite parent) {
		Composite mainContainer = new Composite(parent, SWT.NONE);
		setControl(mainContainer);
		mainContainer.setLayout(new FillLayout());
		_launchesViewer = CheckboxTableViewer.newCheckList(mainContainer, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		_launchesViewer.setContentProvider(new ArrayContentProvider());
		Control control = _launchesViewer.getControl();		
		control.setFont(parent.getFont());
		_launchesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});				
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		//Empty sequence is default anyway
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			//TODO: create UI for item reordering 
			@SuppressWarnings("rawtypes")
			Dictionary<String, ILaunchConfiguration> allValidConfigurations = MultiLaunch.getValidConfigurations(configuration);
			Collection<ILaunchConfiguration> configurationsToRun = MultiLaunch.getConfigurationsToRun(configuration);
			//A list of configurations shown to user
			ArrayList<String> ordered = new ArrayList<String>(allValidConfigurations.size());
			//Configurations to check
			ArrayList<String> sequence = new ArrayList<String>(configurationsToRun.size());
			//First are selected ones in the order they appear in sequence
			for (ILaunchConfiguration toRun : configurationsToRun) {
				sequence.add(toRun.getName());
				ordered.add(toRun.getName());
				allValidConfigurations.remove(toRun.getName());
			}
			//Then others sorted by name
			ArrayList<String> unused = Collections.list(allValidConfigurations.keys());
			Collections.sort(unused);
			ordered.addAll(unused);
			_launchesViewer.setInput(ordered);
			_launchesViewer.setCheckedElements(sequence.toArray());
		} catch (CoreException e) {
			DebugPlugin.log(e);
			_launchesViewer.setInput(null);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Object[] checked = _launchesViewer.getCheckedElements();
		configuration.setAttribute(MultiLaunch.sequenceFieldName, Arrays.asList(checked));		
	}

	@Override
	public String getName() {
		return "Runnables";
	}

}
