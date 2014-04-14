package lumina.ui.actions;

import java.util.Map;

import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
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
 * Action for task move.
 */
public class MoveTasksAction extends Action {

	private final Task[] movedTasks;
	private final Schedule destinationSchedule;
	private final Task targetTask;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving tasks into a new location.
	 * 
	 * @param tasks
	 *            array with tasks to be moved
	 * @param to
	 *            destination schedule
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 */
	public MoveTasksAction(final Task[] tasks, final Schedule to,
			final Task target, final IWorkbenchWindow window) {
		super(getLabelFor(tasks, to));

		if (tasks == null) {
			throw new IllegalArgumentException("The task must be assigned");
		}
		movedTasks = tasks;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination schedule must be assigned");
		}
		destinationSchedule = to;
		targetTask = target;
		workbenchWindow = window;
	}

	/**
	 * Returns the label for task action.
	 * 
	 * @param tasks
	 *            array with tasks to which the operation applies
	 * @param to
	 *            destination schedule
	 * @return label with operation
	 */
	private static String getLabelFor(final Task[] tasks, final Schedule to) {
		if (tasks.length > 1) {
			return "Move tasks to " + to.getName();
		} else {
			return "Move task " + to.getName();
		}
	}

	/**
	 * Execute the task move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		final Map<Schedule, Task[]> originalTaskSchedules = Queries
				.getOriginalTasksBySchedule(movedTasks);

		IUndoableOperation moveTask = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveTasks(movedTasks,
						destinationSchedule, targetTask);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveTasks(originalTaskSchedules,
						targetTask);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		moveTask.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(moveTask, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}
