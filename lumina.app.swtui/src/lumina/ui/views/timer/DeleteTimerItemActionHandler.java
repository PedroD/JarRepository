package lumina.ui.views.timer;

import lumina.base.model.DeviceTimer;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.ui.actions.DeleteSchedulesAction;
import lumina.ui.actions.DeleteTasksAction;
import lumina.ui.actions.DeleteTimersAction;
import lumina.ui.views.AbstractDeleteTreeItemActionHandler;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action handler for timer item deletion.
 */
public class DeleteTimerItemActionHandler extends
		AbstractDeleteTreeItemActionHandler {

	/**
	 * Action handler constructor for timer item deletion.
	 * 
	 * @param window
	 *            workbench window
	 */
	public DeleteTimerItemActionHandler(IWorkbenchWindow window) {
		super(window);
	}

	/**
	 * Performs the item deletion action.
	 * <p>
	 * Deletes all items if the items are all timers, schedules or tasks.
	 * 
	 * @param items
	 *            array with timers, schedules or tasks to be deleted
	 * @param window
	 *            workbench window
	 */
	protected void doDeleteItems(final ModelItem[] items,
			final IWorkbenchWindow window) {
		if (ModelUtils.areAllTimers(items)) {
			final DeviceTimer[] timers = ModelUtils.toTimers(items);
			final IAction deleteTimers = new DeleteTimersAction(timers, window);
			deleteTimers.run();
		} else if (ModelUtils.areAllSchedules(items)) {
			final Schedule[] areas = ModelUtils.toSchedules(items);
			final IAction deleteSchedules = new DeleteSchedulesAction(areas,
					window);
			deleteSchedules.run();
		} else if (ModelUtils.areAllTasks(items)) {
			final Task[] floor = ModelUtils.toTasks(items);
			final IAction deleteTasks = new DeleteTasksAction(floor, window);
			deleteTasks.run();
		}
	}
}
