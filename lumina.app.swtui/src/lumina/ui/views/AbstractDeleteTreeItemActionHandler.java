package lumina.ui.views;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.propertytesters.CanDeleteItemsPropertyTester;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Abstract class for handlers of delete actions.
 * <p>
 * The method {@link #doDeleteItems(ModelItem[], IWorkbenchWindow)} must be
 * overriden to delete the specific items of the view.
 */
public abstract class AbstractDeleteTreeItemActionHandler extends Action
		implements ISelectionListener {
	private final IWorkbenchWindow workbenchWindow;

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 * <p>
	 * Adds itself to the selection listener.
	 * 
	 * @param window
	 *            workbench window
	 */
	public AbstractDeleteTreeItemActionHandler(IWorkbenchWindow window) {
		super();
		this.workbenchWindow = window;

		setEnabled(false);
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * Handler for selection change.
	 * 
	 * @param part
	 *            workbench part
	 * @param incoming
	 *            selection
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (!(incoming instanceof IStructuredSelection)) {
			return;
		}

		selection = (IStructuredSelection) incoming;
		final Object[] selections = selection.toArray();

		setEnabled(CanDeleteItemsPropertyTester.canDelete(ModelUtils
				.toModelItems(selections)));
	}

	/**
	 * Deletes the specified items.
	 * 
	 * @param items
	 *            model item array for deletion
	 * @param window
	 *            workbench window
	 */
	protected abstract void doDeleteItems(final ModelItem[] items,
			final IWorkbenchWindow window);

	/**
	 * If items are selected attempts to delete them.
	 */
	public void run() {
		if (selection != null) {
			final ModelItem[] items = ModelUtils.toModelItems(selection
					.toArray());
			doDeleteItems(items, workbenchWindow);
		}
	}

	/**
	 * Terminate.
	 * <p>
	 * Removes itself from the selection listener.
	 */
	public void dispose() {
		workbenchWindow.getSelectionService().removeSelectionListener(this);
	}
}
