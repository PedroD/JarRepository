package lumina.ui.actions;

import lumina.base.model.Floor;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
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
 * Handler for floor deletion.
 */
public class DeleteFloorsAction extends Action {
	/**
	 * Floor delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final Floor[] deletedFloors;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for floor deletion.
	 * 
	 * @param floors
	 *            array with floors for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteFloorsAction(final Floor[] floors,
			final IWorkbenchWindow window, final String operationName) {
		super(getLabelFor(operationName, floors));
		setId("Lumina.DeleteFloorAction");

		if (floors == null) {
			throw new IllegalArgumentException("The floors must be assigned");
		}

		deletedFloors = floors;
		workbenchWindow = window;
	}

	/**
	 * Action for floor deletion.
	 * 
	 * @param floors
	 *            array with floors for deletion
	 * @param window
	 *            window
	 */
	public DeleteFloorsAction(final Floor[] floors,
			final IWorkbenchWindow window) {
		this(floors, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for floor action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param floors
	 *            array with floors to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName, Floor[] floors) {
		if (floors.length > 1) {
			return operationName + " floors";
		} else {
			return operationName + " floor";
		}
	}

	/**
	 * Execute the floor deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final Floor[] originalFloors = deletedFloors;
		if (originalFloors.length > 0) {
			final Project parentProject = originalFloors[0].getParentProject();

			IUndoableOperation deleteFloors = new BaseRedoableOperation(
					getText()) {
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().deleteFloors(originalFloors);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().addFloors(originalFloors,
							parentProject, null);
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};

			deleteFloors.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

			final IWorkbench workbench = workbenchWindow.getWorkbench();
			final IOperationHistory undoHistory = workbench
					.getOperationSupport().getOperationHistory();

			try {
				undoHistory.execute(deleteFloors, null, null);
			} catch (ExecutionException e) {
				// XXX What should I do here?
			}
		}
	}
}
