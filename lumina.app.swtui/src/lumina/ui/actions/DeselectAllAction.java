package lumina.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Deselect all action.
 * 
 * @see org.eclipse.jface.action.Action
 * @see org.eclipse.ui.ISelectionListener
 */
public class DeselectAllAction extends Action implements ISelectionListener {

	private final IWorkbenchWindow workbenchWindow;
	private final SelectAllAction selectAllAction;

	/**
	 * Deselect all action.<br/>
	 * Adds this to the window selection listener.
	 * 
	 * @param selectAll
	 *            select all action
	 * @param window
	 *            workbench window
	 * 
	 * @see lumina.ui.actions.SelectAllAction
	 */
	public DeselectAllAction(final SelectAllAction selectAll,
			final IWorkbenchWindow window) {
		super();
		workbenchWindow = window;
		selectAllAction = selectAll;

		setEnabled(false);

		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * Trigger for selection change.
	 * 
	 * @param part
	 *            workbench part
	 * @param incoming
	 *            selection
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		final boolean canDeselect = selectAllAction.canRevertSelection();

		setEnabled(canDeselect);
	}

	/**
	 * Executes the deselect all action by revering to the original selection.
	 * 
	 * @see lumina.ui.actions.SelectAllAction#revertToOriginalSelection()
	 */
	public void run() {
		selectAllAction.revertToOriginalSelection();
	}

	/**
	 * Terminates and removes this action from the window listeners.
	 */
	public void dispose() {
		workbenchWindow.getSelectionService().removeSelectionListener(this);
	}
}