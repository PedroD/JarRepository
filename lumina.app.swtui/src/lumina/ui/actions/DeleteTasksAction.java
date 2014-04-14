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
 * Handler for task deletion.
 */
public class DeleteTasksAction extends Action {

	/**
	 * Task delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final Task[] deletedTasks;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for task deletion.
	 * 
	 * @param tasks
	 *            array with tasks for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteTasksAction(final Task[] tasks, final IWorkbenchWindow window,
			final String operationName) {
		super(getLabelFor(operationName, tasks));

		if (tasks == null) {
			throw new IllegalArgumentException("The tasks must be assigned");
		}

		deletedTasks = tasks;
		workbenchWindow = window;
	}

	/**
	 * Action for task deletion.
	 * 
	 * @param tasks
	 *            array with tasks for deletion
	 * @param window
	 *            window
	 */
	public DeleteTasksAction(final Task[] tasks, final IWorkbenchWindow window) {
		this(tasks, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for task action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param tasks
	 *            array with tasks to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName, Task[] tasks) {
		if (tasks.length > 1) {
			return operationName + " tasks";
		} else {
			return operationName + " task";
		}
	}

	/**
	 * Execute the task deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		final Map<Schedule, Task[]> originalTasksBySchedule = Queries
				.getOriginalTasksBySchedule(deletedTasks);

		IUndoableOperation deleteTasks = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().deleteTasks(deletedTasks);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().addTasks(originalTasksBySchedule);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		deleteTasks.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(deleteTasks, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}
