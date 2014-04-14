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
 * Action for timer move.
 */
public class MoveTimersAction extends Action {

	private final DeviceTimer[] movedTimers;
	private final Project destinationProject;
	private final DeviceTimer targetTimer;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving timers into a new location.
	 * 
	 * @param timers
	 *            array with timers to be moved
	 * @param to
	 *            destination project
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 */
	public MoveTimersAction(final DeviceTimer[] timers, final Project to,
			final DeviceTimer target, final IWorkbenchWindow window) {
		super(getLabelFor(timers, to));

		if (timers == null) {
			throw new IllegalArgumentException("The task must be assigned");
		}
		movedTimers = timers;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination schedule must be assigned");
		}
		destinationProject = to;
		targetTimer = target;
		workbenchWindow = window;
	}

	/**
	 * Returns the label for timer action.
	 * 
	 * @param timers
	 *            array with timers to which the operation applies
	 * @param to
	 *            destination project
	 * @return label with operation
	 */
	private static String getLabelFor(final DeviceTimer[] timers,
			final Project to) {
		if (timers.length > 1) {
			return "Move timers to " + to.getName();
		} else {
			return "Move timer " + to.getName();
		}
	}

	/**
	 * Execute the timer move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return;

		IUndoableOperation moveTask = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveTimers(movedTimers,
						destinationProject, targetTimer);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveTimers(movedTimers,
						destinationProject, null);
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
