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
 * Action for schedule move.
 */
public class MoveSchedulesAction extends Action {

	private final Schedule[] movedSchedules;
	private final DeviceTimer destinationTimer;
	private final Schedule targetSchedule;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving schedules into a new location.
	 * 
	 * @param schedules
	 *            array with devices to be moved
	 * @param to
	 *            destination timer
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 */
	public MoveSchedulesAction(final Schedule[] schedules,
			final DeviceTimer to, Schedule target, final IWorkbenchWindow window) {
		super(getLabelFor(schedules, to));

		if (schedules == null) {
			throw new IllegalArgumentException("The schedule must be assigned");
		}
		movedSchedules = schedules;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination device timer must be assigned");
		}
		destinationTimer = to;
		targetSchedule = target;
		workbenchWindow = window;
	}

	/**
	 * Returns the label for schedule action.
	 * 
	 * @param schedules
	 *            array with schedules to which the operation applies
	 * @param to
	 *            destination device timer
	 * @return label with operation
	 */
	private static String getLabelFor(final Schedule[] schedules,
			final DeviceTimer to) {
		if (schedules.length > 1) {
			return "Move schedules to " + to.getName();
		} else {
			return "Move schedule " + to.getName();
		}
	}

	/**
	 * Execute the schedule move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		final Map<DeviceTimer, Schedule[]> originalScheduleTimers = Queries
				.getOriginalSchedulesByTimer(movedSchedules);

		final IUndoableOperation moveSchedule = new BaseRedoableOperation(
				getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveSchedules(movedSchedules,
						destinationTimer, targetSchedule);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveSchedules(
						originalScheduleTimers, targetSchedule);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		moveSchedule.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(moveSchedule, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}
