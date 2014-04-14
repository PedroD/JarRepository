package lumina.ui.actions;

import lumina.ui.perspectives.PerspectiveHelper;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Close the welcome page action.
 */
public class CloseWelcomeActionDelegate implements
		IWorkbenchWindowActionDelegate {

	/**
	 * Initialize the action delegate.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Dispose action delegate.
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
		PerspectiveHelper.closeWelcomePage();
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
}
