package lumina.ui.actions;

import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.base.model.TaskJob;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Task execution handler.<br/>
 * Tasks are usually scheduled.
 * 
 * @see lumina.base.model.Schedule
 */
public class ExecuteTaskHandler extends AbstractHandler {

	/**
	 * Executes a task.<br/>
	 * Tasks are usually scheduled.
	 * 
	 * @param execEvent
	 *            execution event
	 * @return null
	 * @throws ExecutionException
	 *             not thrown
	 * @see lumina.base.model.Schedule
	 */
	@Override
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		final ISelection currentSelection = HandlerUtil
				.getCurrentSelection(execEvent);
		if (currentSelection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;

			final Object selectedElement = structuredSelection
					.getFirstElement();
			final Schedule enclosingSchedule = Queries
					.getAncestorSchedule(selectedElement);

			assert enclosingSchedule != null;

			final Task selectedTask = (Task) selectedElement;

			if (TaskJob.hasAffectedDevices(selectedTask)) {
				TaskJob.executeTask(selectedTask);
			} else {
				MessageDialog
						.openWarning(
								Display.getCurrent().getActiveShell(),
								Messages.getString("ExecuteTaskActionHandler.taskExecutionTitle"), //$NON-NLS-1$
								Messages.getString("ExecuteTaskActionHandler.taskExecutionMessage")); //$NON-NLS-1$
			}
		}

		return null;
	}
}
