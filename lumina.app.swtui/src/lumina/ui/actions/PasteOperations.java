package lumina.ui.actions;

import java.util.List;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.kernel.Logger;
import lumina.license.LicenseLimitsExceededException;
import lumina.ui.jface.RedoableOperationWrapper;
import lumina.ui.swt.SimpleDialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.service.log.LogService;

/**
 * This auxiliary class contains methods for pasting model items, as undoable
 * operations.
 */
public final class PasteOperations {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private PasteOperations() {
	}

	/**
	 * Returns the operation name.
	 * 
	 * @param itemList
	 *            list with items
	 * @return operation name
	 */
	static String getOperationName(final List<?> itemList) {
		final Object[] objects = itemList.toArray(new Object[0]);
		final ModelItem[] items = ModelUtils.toModelItems(objects);
		return "Paste " + ModelUtils.getLabelFor(items);
	}

	/**
	 * Paste floors.
	 * 
	 * @param floors
	 *            floor list to paste
	 * @param dropTarget
	 *            target where floors should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteFloors(final List<Floor> floors,
			final Object dropTarget, final IWorkbenchWindow window) {

		final Project pasteProject = Queries.getAncestorProject(dropTarget);
		final Floor[] floorsToPaste = floors.toArray(new Floor[floors.size()]);

		// Create the paste floors undoable operation
		final RedoableOperationWrapper pasteFloors = new RedoableOperationWrapper(
				getOperationName(floors), window) {
			private Floor[] pastedFloors = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final Floor dropFloor = ModelUtils
						.getDropTargetForFloor(dropTarget);
				try {
					pastedFloors = ProjectModel.getInstance().pasteFloors(
							floorsToPaste, pasteProject, dropFloor);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				} catch (LicenseLimitsExceededException ex) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"License limits exceded while pasting floors:", ex);

					SimpleDialogs
							.showInfo(ex.getTitle(), ex.getMessage(), true);

					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedFloors != null) {
					ProjectModel.getInstance().deleteFloors(pastedFloors);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteFloors.run();
	}

	/**
	 * Identifies paste areas.
	 * 
	 * @param areas
	 *            area list
	 * @param dropTarget
	 *            target where floors should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteAreas(final List<Area> areas,
			final Object dropTarget, final IWorkbenchWindow window) {

		final Floor pasteFloor = Queries.getAncestorFloor(dropTarget);
		final Area[] areasToPaste = areas.toArray(new Area[areas.size()]);

		// Create the paste areas undoable operation
		final RedoableOperationWrapper pasteAreas = new RedoableOperationWrapper(
				getOperationName(areas), window) {
			private Area[] pastedAreas = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final Area dropArea = ModelUtils
						.getDropTargetForArea(dropTarget);
				try {
					pastedAreas = ProjectModel.getInstance().pasteAreas(
							areasToPaste, pasteFloor, dropArea);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				} catch (LicenseLimitsExceededException ex) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"License limits exceded while pasting areas:", ex);

					SimpleDialogs
							.showInfo(ex.getTitle(), ex.getMessage(), true);

					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedAreas != null) {
					ProjectModel.getInstance().deleteAreas(pastedAreas);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteAreas.run();
	}

	/**
	 * Paste devices.
	 * 
	 * @param devices
	 *            device list
	 * @param dropTarget
	 *            target where devices should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteDevices(final List<Device> devices,
			final Object dropTarget, final IWorkbenchWindow window) {

		final Area pasteArea = Queries.getAncestorArea(dropTarget);
		final Device[] devicesToPaste = devices.toArray(new Device[devices
				.size()]);

		// Create the paste device undoable operation
		final RedoableOperationWrapper pasteDevices = new RedoableOperationWrapper(
				getOperationName(devices), window) {
			private Device[] pastedDevices = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final Device dropDeviceTarget = ModelUtils
						.getDropTargetForDevice(dropTarget);
				try {
					pastedDevices = ProjectModel.getInstance().pasteDevices(
							devicesToPaste, pasteArea, dropDeviceTarget);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				} catch (LicenseLimitsExceededException ex) {
					Logger.getInstance()
							.log(LogService.LOG_ERROR,
									"License limits exceded while pasting devices:",
									ex);

					SimpleDialogs
							.showInfo(ex.getTitle(), ex.getMessage(), true);

					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedDevices != null) {
					ProjectModel.getInstance().deleteDevices(pastedDevices);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteDevices.run();
	}

	/**
	 * Pastes timers.
	 * 
	 * @param timers
	 *            timer list
	 * @param dropTarget
	 *            target where timers should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteTimers(final List<DeviceTimer> timers,
			final Object dropTarget, final IWorkbenchWindow window) {

		final Project parentProject = Queries.getAncestorProject(dropTarget);
		final DeviceTimer[] timersToPaste = timers
				.toArray(new DeviceTimer[timers.size()]);

		// Create the paste timers undoable operation
		final RedoableOperationWrapper pasteTimers = new RedoableOperationWrapper(
				getOperationName(timers), window) {
			private DeviceTimer[] pastedTimers = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final DeviceTimer dropTimer = ModelUtils
						.getDropTargetForTimer(dropTarget);
				pastedTimers = ProjectModel.getInstance().pasteTimers(
						timersToPaste, parentProject, dropTimer);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedTimers != null) {
					ProjectModel.getInstance().deleteTimers(pastedTimers);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteTimers.run();
	}

	/**
	 * Pastes schedules.
	 * 
	 * @param schedules
	 *            schedule list
	 * @param dropTarget
	 *            target where schedules should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteSchedules(final List<Schedule> schedules,
			final Object dropTarget, final IWorkbenchWindow window) {

		final DeviceTimer parentTimer = Queries.getAncestorTimer(dropTarget);
		final Schedule[] schedsToPaste = schedules
				.toArray(new Schedule[schedules.size()]);

		// Create the paste schedule undoable operation
		final RedoableOperationWrapper pasteScheds = new RedoableOperationWrapper(
				getOperationName(schedules), window) {
			private Schedule[] pastedScheds = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final Schedule dropTargetSched = ModelUtils
						.getDropTargetForSchedule(dropTarget);
				pastedScheds = ProjectModel.getInstance().pasteSchedules(
						schedsToPaste, parentTimer, dropTargetSched);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedScheds != null) {
					ProjectModel.getInstance().deleteSchedules(pastedScheds);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteScheds.run();
	}

	/**
	 * Pastes tasks.
	 * 
	 * @param tasks
	 *            task list
	 * @param dropTarget
	 *            target where tasks should be dropped
	 * @param window
	 *            workbench window
	 */
	public static void pasteTasks(final List<Task> tasks,
			final Object dropTarget, final IWorkbenchWindow window) {

		final Schedule parentSched = Queries.getAncestorSchedule(dropTarget);
		final Task[] tasksToPaste = tasks.toArray(new Task[tasks.size()]);

		// Create the paste task undoable operation
		final RedoableOperationWrapper pasteTasks = new RedoableOperationWrapper(
				getOperationName(tasks), window) {
			private Task[] pastedTasks = null;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				final Task dropTargetTask = ModelUtils
						.getDropTargetForTask(dropTarget);
				pastedTasks = ProjectModel.getInstance().pasteTasks(
						tasksToPaste, parentSched, dropTargetTask);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				if (pastedTasks != null) {
					ProjectModel.getInstance().deleteTasks(pastedTasks);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		pasteTasks.run();
	}
}
