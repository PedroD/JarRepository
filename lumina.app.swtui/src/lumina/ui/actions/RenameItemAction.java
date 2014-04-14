package lumina.ui.actions;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.ui.jface.BaseRedoableOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action for item rename.
 */
public class RenameItemAction extends Action {
	private final Object changedObject;
	private final String newName;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Constructor.
	 * 
	 * @param subject
	 *            item to be renamed
	 * @param name
	 *            new item name
	 * @param window
	 *            workbench window
	 */
	public RenameItemAction(final Object subject, final String name,
			final IWorkbenchWindow window) {
		super(getLabelFor(subject));

		changedObject = subject;
		newName = name;
		workbenchWindow = window;
	}

	private static String getLabelFor(final Object changed) {
		if (changed != null) {
			final String msg = "Change "
					+ Queries.getObjectName(changed).toLowerCase() + " name";
			return msg;
		} else {
			return "Property change";
		}
	}

	/**
	 * Rename action.
	 */
	public void run() {
		if (!(changedObject instanceof ModelItem))
			return;

		final ModelItem item = (ModelItem) changedObject;

		if (ModelUtils.canRenameItem(item)) {
			final String oldName = item.getName();

			IUndoableOperation changeName = new BaseRedoableOperation(getText()) {
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					if (ProjectModel.getInstance().renameItem(item, newName) != null) {
						return org.eclipse.core.runtime.Status.OK_STATUS;
					} else {
						return org.eclipse.core.runtime.Status.CANCEL_STATUS;
					}
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					if (ProjectModel.getInstance().renameItem(item, oldName) != null) {
						return org.eclipse.core.runtime.Status.OK_STATUS;
					} else {
						return org.eclipse.core.runtime.Status.CANCEL_STATUS;
					}
				}
			};

			changeName.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

			final IWorkbench workbench = workbenchWindow.getWorkbench();
			final IOperationHistory undoHistory = workbench
					.getOperationSupport().getOperationHistory();

			try {
				undoHistory.execute(changeName, null, null);
			} catch (ExecutionException e) {
				// TODO What should I do here?
			}
		}
	}

}