package lumina.ui.actions;

import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.dialogs.InterfaceConfigurationDialog;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Show the Interface Configuration dialog.
 */
public class ConfigureNetworkInterfacesActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	/**
	 * Initializes the network interface action.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Terminates the action.
	 */
	public void dispose() {
	}

	/**
	 * Handler for selection change.
	 * 
	 * @param action
	 *            action performed
	 * @param selection
	 *            new selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Shows the network interface window.<br/>
	 * The window will only be shown if the user has permissions.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		if (!Capabilities.canDo(Capability.NETWORK_EDIT))
			return;

		new InterfaceConfigurationDialog(window.getShell()).open();
	}

}
