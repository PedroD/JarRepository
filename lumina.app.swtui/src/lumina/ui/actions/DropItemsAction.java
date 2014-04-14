package lumina.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Drop action, for Drag'N'Drop support in the project tree.
 * 
 * @author Paulo Carreira
 */
public class DropItemsAction extends Action {

	/**
	 * Items being dropped.
	 */
	private final ModelItem[] modelItems;

	/**
	 * Target of the drop operation.
	 */
	private final ModelItem dropTarget;

	/**
	 * Set to <code>true</code> the drop operation is local.
	 */
	private final boolean isLocalDrop;

	/**
	 * Reference to the workbench window.
	 */
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for item drop.
	 * 
	 * @param items
	 *            array with model items
	 * @param target
	 *            target model item
	 * @param local
	 *            is local drop
	 * @param window
	 *            workbench window
	 */
	public DropItemsAction(final ModelItem[] items, final ModelItem target,
			boolean local, final IWorkbenchWindow window) {
		super("Drop");

		modelItems = items;
		dropTarget = target;
		isLocalDrop = local;

		workbenchWindow = window;

		setEnabled(true);
	}

	/**
	 * Checks if the currently selected object accepts paste from the clipboard.
	 * <p>
	 * Note that for usability, we can for example, paste an area over a device.
	 * The area will be pasted on the area corresponding to the device.
	 * 
	 * @param items
	 *            model items array
	 * @param selectedItem
	 *            the object currently selected
	 * @return <code>true</code> if the object in the clipboard can be pasted
	 *         into the currently selected object.
	 */
	public static boolean acceptsPaste(final ModelItem[] items,
			final ModelItem selectedItem) {
		if (selectedItem instanceof Device) {
			return ModelUtils.areAllDevices(items)
					|| ModelUtils.areAllAreas(items)
					|| ModelUtils.areAllFloors(items);
		} else if (selectedItem instanceof Area) {
			return ModelUtils.areAllDevices(items)
					|| ModelUtils.areAllAreas(items)
					|| ModelUtils.areAllFloors(items);
		} else if (selectedItem instanceof Floor) {
			return ModelUtils.areAllAreas(items)
					|| ModelUtils.areAllFloors(items);
		} else if (selectedItem instanceof Project) {
			return ModelUtils.areAllFloors(items)
					|| ModelUtils.areAllTimers(items);
		} else if (selectedItem instanceof Task) {
			return ModelUtils.areAllTasks(items);
		} else if (selectedItem instanceof Schedule) {
			return ModelUtils.areAllSchedules(items)
					|| ModelUtils.areAllTasks(items);
		} else if (selectedItem instanceof DeviceTimer) {
			return ModelUtils.areAllTimers(items)
					|| ModelUtils.areAllSchedules(items);
		} else {
			return false;
		}
	}

	/**
	 * Returns the drop operation name.
	 * 
	 * @param items
	 *            model item array
	 * @return drop operation name
	 */
	static final String getOperationName(ModelItem[] items) {
		return "Drop " + ModelUtils.getLabelFor(items);
	}

	/**
	 * Checks if the items can be pasted or dropped in a specific target.
	 * 
	 * @param modelItems
	 *            array with model items to drop
	 * @param dropTarget
	 *            target where the model items will be dropped
	 * @return true if the items can be pasted or dropped in the specified
	 *         target, false otherwise
	 */
	public static final boolean canPasteOrDrop(final ModelItem[] modelItems,
			final ModelItem dropTarget) {
		return modelItems != null && modelItems.length != 0
				&& acceptsPaste(modelItems, dropTarget);
	}

	private void dropDevices() {
		if (isLocalDrop) {
			final Device[] devices = ModelUtils.toDevices(modelItems);
			final Area targetArea = Queries.getAncestorArea(dropTarget);
			final Device dropDevice = ModelUtils
					.getDropTargetForDevice(dropTarget);
			final MoveDevicesAction moveDevices = new MoveDevicesAction(
					devices, targetArea, dropDevice, workbenchWindow);

			moveDevices.run();
		} else {
			final List<Device> devicesToPaste = new ArrayList<Device>();
			Collections
					.addAll(devicesToPaste, ModelUtils.toDevices(modelItems));
			PasteOperations.pasteDevices(devicesToPaste, dropTarget,
					workbenchWindow);
		}
	}

	private void dropAreas() {
		if (isLocalDrop) {
			final Area[] areas = ModelUtils.toAreas(modelItems);
			final Floor targetFloor = Queries.getAncestorFloor(dropTarget);
			final Area dropArea = ModelUtils.getDropTargetForArea(dropTarget);
			final MoveAreasAction moveAreas = new MoveAreasAction(areas,
					targetFloor, dropArea, workbenchWindow);

			moveAreas.run();
		} else {
			final List<Area> areasToPaste = new ArrayList<Area>();
			Collections.addAll(areasToPaste, ModelUtils.toAreas(modelItems));
			PasteOperations.pasteAreas(areasToPaste, dropTarget,
					workbenchWindow);
		}
	}

	private void dropFloors() {
		if (isLocalDrop) {
			final Floor[] floors = ModelUtils.toFloors(modelItems);
			final Project targetProject = Queries
					.getAncestorProject(dropTarget);
			final Floor dropFloor = ModelUtils
					.getDropTargetForFloor(dropTarget);
			final MoveFloorsAction moveFloors = new MoveFloorsAction(floors,
					targetProject, dropFloor, workbenchWindow);

			moveFloors.run();
		} else {
			final List<Floor> floorsToPaste = new ArrayList<Floor>();
			Collections.addAll(floorsToPaste, ModelUtils.toFloors(modelItems));
			PasteOperations.pasteFloors(floorsToPaste, dropTarget,
					workbenchWindow);
		}
	}

	private void dropTasks() {
		if (isLocalDrop) {
			final Task[] tasks = ModelUtils.toTasks(modelItems);
			final Schedule targetSchedule = Queries
					.getAncestorSchedule(dropTarget);
			final Task dropTask = ModelUtils.getDropTargetForTask(dropTarget);
			final MoveTasksAction moveTasks = new MoveTasksAction(tasks,
					targetSchedule, dropTask, workbenchWindow);

			moveTasks.run();
		} else {
			final List<Task> tasksToPaste = new ArrayList<Task>();
			Collections.addAll(tasksToPaste, ModelUtils.toTasks(modelItems));
			PasteOperations.pasteTasks(tasksToPaste, dropTarget,
					workbenchWindow);
		}
	}

	private void dropSchedules() {
		if (isLocalDrop) {
			final Schedule[] scheds = ModelUtils.toSchedules(modelItems);
			final DeviceTimer targetTimer = Queries
					.getAncestorTimer(dropTarget);
			final Schedule dropSchedule = ModelUtils
					.getDropTargetForSchedule(dropTarget);
			final MoveSchedulesAction moveSchedules = new MoveSchedulesAction(
					scheds, targetTimer, dropSchedule, workbenchWindow);

			moveSchedules.run();
		} else {
			final List<Schedule> schedsToPaste = new ArrayList<Schedule>();
			Collections.addAll(schedsToPaste,
					ModelUtils.toSchedules(modelItems));
			PasteOperations.pasteSchedules(schedsToPaste, dropTarget,
					workbenchWindow);
		}
	}

	private void dropTimers() {
		if (isLocalDrop) {
			final DeviceTimer[] timers = ModelUtils.toTimers(modelItems);
			final Project targetProj = Queries.getAncestorProject(dropTarget);
			final DeviceTimer droptimer = ModelUtils
					.getDropTargetForTimer(dropTarget);
			final MoveTimersAction moveTimers = new MoveTimersAction(timers,
					targetProj, droptimer, workbenchWindow);

			moveTimers.run();
		} else {
			final List<DeviceTimer> timersToPaste = new ArrayList<DeviceTimer>();
			Collections.addAll(timersToPaste, ModelUtils.toTimers(modelItems));
			PasteOperations.pasteTimers(timersToPaste, dropTarget,
					workbenchWindow);
		}
	}

	/**
	 * Execute the drop items action.
	 */
	public void run() {
		if (canPasteOrDrop(modelItems, dropTarget)) {
			if (ModelUtils.areAllDevices(modelItems)) {
				dropDevices();
			} else if (ModelUtils.areAllAreas(modelItems)) {
				dropAreas();
			} else if (ModelUtils.areAllFloors(modelItems)) {
				dropFloors();
			} else if (ModelUtils.areAllTasks(modelItems)) {
				dropTasks();
			} else if (ModelUtils.areAllSchedules(modelItems)) {
				dropSchedules();
			} else if (ModelUtils.areAllTimers(modelItems)) {
				dropTimers();
			}
		}
	}

}
