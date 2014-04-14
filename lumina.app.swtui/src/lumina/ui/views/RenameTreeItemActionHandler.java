package lumina.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action handler for tree item rename.
 */
public class RenameTreeItemActionHandler extends Action implements
		ISelectionListener {

	private final TreeViewer treeViewer;
	private final IWorkbenchWindow workbenchWindow;

	private TreeItem selectedItem;

	/**
	 * Constructor.
	 * <p>
	 * Adds itself to the window selection listener.
	 * 
	 * @param viewer
	 *            tree viewer
	 * @param window
	 *            workbench window
	 */
	public RenameTreeItemActionHandler(final TreeViewer viewer,
			final IWorkbenchWindow window) {
		super();
		treeViewer = viewer;
		workbenchWindow = window;

		setEnabled(false);
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * Selection change event handler.
	 * 
	 * @param part
	 *            workbench part
	 * @param incoming
	 *            selection
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (!(incoming instanceof IStructuredSelection))
			return;

		final IStructuredSelection selection = (IStructuredSelection) incoming;

		if (selection.size() == 1) {
			final TreeItem[] items = treeViewer.getTree().getSelection();
			if (items.length == 1) {
				selectedItem = (TreeItem) items[0];
				setEnabled(true);
				return;
			}
		}
		selectedItem = null;
		setEnabled(false);
	}

	/**
	 * Action execution.
	 * <p>
	 * If there is a selected item activates the edit tree item.
	 */
	public void run() {
		if (selectedItem != null) {
			TreeItemInlineEditListener.editTreeItem(selectedItem,
					workbenchWindow);
		}
	}

	/**
	 * Terminates.
	 * <p>
	 * Removes itself from the selection listener.
	 */
	public void dispose() {
		workbenchWindow.getSelectionService().removeSelectionListener(this);
	}
}
