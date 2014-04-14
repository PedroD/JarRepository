package lumina.ui.actions;

import lumina.ui.perspectives.PerspectiveHelper;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to open the welcome page.
 */
public class OpenWelcomeActionDelegate implements
		IWorkbenchWindowActionDelegate {

	/**
	 * Initialization.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Terminate.
	 */
	public void dispose() {
	}

	/**
	 * Closes welcome page.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		PerspectiveHelper.openWelcomePage();
	}

	/**
	 * Trigger for selection change.
	 * 
	 * @param action
	 *            action
	 * @param selection
	 *            selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
