package lumina.ui.actions;

import lumina.base.model.DeviceTimer;
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
 * Handler for timer deletion.
 */
public class DeleteTimersAction extends Action {

	/**
	 * Area delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final DeviceTimer[] deletedTimers;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for timer deletion.
	 * 
	 * @param timers
	 *            array with timers for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteTimersAction(final DeviceTimer[] timers,
			final IWorkbenchWindow window, final String operationName) {
		super(getLabelFor(operationName, timers));

		if (timers == null) {
			throw new IllegalArgumentException("The timers must be assigned");
		}

		deletedTimers = timers;
		workbenchWindow = window;
	}

	/**
	 * Action for timer deletion.
	 * 
	 * @param timers
	 *            array with timers for deletion
	 * @param window
	 *            window
	 */
	public DeleteTimersAction(final DeviceTimer[] timers,
			final IWorkbenchWindow window) {
		this(timers, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for timer action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param timers
	 *            array with timers to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName,
			DeviceTimer[] timers) {
		if (timers.length > 1) {
			return operationName + " timer";
		} else {
			return operationName + " timer";
		}
	}

	/**
	 * Execute the timer deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		final DeviceTimer[] originalTimers = deletedTimers;

		if (originalTimers.length > 0) {

			final Project parentProject = originalTimers[0].getParentProject();

			IUndoableOperation deleteTimers = new BaseRedoableOperation(
					getText()) {
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().deleteTimers(originalTimers);
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().addTimers(originalTimers,
							parentProject, null);
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};

			deleteTimers.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

			final IWorkbench workbench = workbenchWindow.getWorkbench();
			final IOperationHistory undoHistory = workbench
					.getOperationSupport().getOperationHistory();

			try {
				undoHistory.execute(deleteTimers, null, null);
			} catch (ExecutionException e) {
				// XXX What should I do here?
			}
		}
	}
}
