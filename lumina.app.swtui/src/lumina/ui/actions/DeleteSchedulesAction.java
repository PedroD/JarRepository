package lumina.ui.actions;

import java.util.Map;

import lumina.base.model.DeviceTimer;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
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
 * Handler for schedule deletion.
 */
public class DeleteSchedulesAction extends Action {

	/**
	 * Schedule delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final Schedule[] deletedSchedules;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for schedule deletion.
	 * 
	 * @param schedules
	 *            array with schedules for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteSchedulesAction(final Schedule[] schedules,
			final IWorkbenchWindow window, final String operationName) {
		super(getLabelFor(operationName, schedules));

		if (schedules == null) {
			throw new IllegalArgumentException("The schedules must be assigned");
		}

		deletedSchedules = schedules;
		workbenchWindow = window;
	}

	/**
	 * Action for schedule deletion.
	 * 
	 * @param schedules
	 *            array with schedules for deletion
	 * @param window
	 *            window
	 */
	public DeleteSchedulesAction(final Schedule[] schedules,
			final IWorkbenchWindow window) {
		this(schedules, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for schedule action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param schedules
	 *            array with schedules to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName,
			Schedule[] schedules) {
		if (schedules.length > 1) {
			return operationName + " schedule";
		} else {
			return operationName + " schedule";
		}
	}

	/**
	 * Execute the schedule deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		final Map<DeviceTimer, Schedule[]> originalSchedulesByTimer = Queries
				.getOriginalSchedulesByTimer(deletedSchedules);

		IUndoableOperation deleteSchedules = new BaseRedoableOperation(
				getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().deleteSchedules(deletedSchedules);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().addSchedules(
						originalSchedulesByTimer);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		deleteSchedules.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(deleteSchedules, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}
