package lumina.ui.actions;

import lumina.ui.dialogs.AboutDialog;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Display the About box.
 * 
 * @author Fernando Martins
 */
public class ShowAboutDialogActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	/**
	 * Initialize.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Terminate.
	 */
	public void dispose() {
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

	/**
	 * Displays the about box window.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		final AboutDialog dialog = new AboutDialog(window.getShell());
		dialog.open();
	}
}
