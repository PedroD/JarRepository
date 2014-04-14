package lumina.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Handler for deselect all action.
 * 
 * @see org.eclipse.jface.action.Action
 * @see org.eclipse.ui.ISelectionListener
 */
public class DeselectAllActionHandler extends Action implements
		ISelectionListener {

	private final IWorkbenchWindow workbenchWindow;
	private final SelectAllActionHandler selectAllAction;
	private IStructuredSelection selection;

	/**
	 * Deselect all action handler.<br/>
	 * Adds this to the window selection listener.
	 * 
	 * @param selectAll
	 *            select all action handler
	 * @param window
	 *            workbench window
	 * 
	 * @see lumina.ui.actions.SelectAllActionHandler
	 */
	public DeselectAllActionHandler(final SelectAllActionHandler selectAll,
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
		selection = (IStructuredSelection) incoming;

		final boolean canUnselect = selection.size() > 0;
		setEnabled(canUnselect);
	}

	/**
	 * Executes the deselect all action handler by revering to the original
	 * selection.
	 * 
	 * @see lumina.ui.actions.SelectAllActionHandler#revertToOriginalSelection()
	 */
	public void run() {
		selectAllAction.revertToOriginalSelection();
	}

	/**
	 * Terminates and removes this action hanlder from the window listeners.
	 */
	public void dispose() {
		workbenchWindow.getSelectionService().removeSelectionListener(this);
	}
}
