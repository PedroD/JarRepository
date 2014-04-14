package lumina.ui.actions;

import java.util.ArrayList;
import java.util.List;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.base.model.transfer.AreaTransfer;
import lumina.base.model.transfer.DeviceTimerTransfer;
import lumina.base.model.transfer.DeviceTransfer;
import lumina.base.model.transfer.FloorTransfer;
import lumina.base.model.transfer.ModelItemTransfer;
import lumina.base.model.transfer.ScheduleTransfer;
import lumina.base.model.transfer.TaskTransfer;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Pastes the selected items from the ClipboardUtils.
 * <p>
 * Creates the following transfer types:
 * <ol>
 * <li>Text -- which results in the copying the item names.</li>
 * </ol>
 * 
 * @author Paulo Carreira
 * 
 */
public class PasteItemsFromClipboardHandler extends InterceptedHandler {

	/**
	 * Command identifier.
	 */
	private static final String PASTE_COMMAND_ID = "org.eclipse.ui.edit.paste";

	/**
	 * Returns the command identifier.
	 * 
	 * @return paste command indentifer
	 */
	@Override
	public String getCommandId() {
		return PASTE_COMMAND_ID;
	}

	/**
	 * Returns the paste operation name.
	 * 
	 * @param itemList
	 *            list with items
	 * @return operation name for items
	 */
	static final String getOperationName(final List<?> itemList) {
		final Object[] objects = itemList.toArray(new Object[0]);
		final ModelItem[] items = ModelUtils.toModelItems(objects);
		return "Paste " + ModelUtils.getLabelFor(items);
	}

	private void pasteDevices(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = DeviceTransfer.getInstance();

		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();

		final List<Device> devicesToPaste = new ArrayList<Device>();
		for (ModelItem i : items) {
			if (i instanceof Device) {
				devicesToPaste.add((Device) i);
			}
		}

		PasteOperations.pasteDevices(devicesToPaste, selected, workbenchWindow);
	}

	private void pasteAreas(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = AreaTransfer.getInstance();

		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();

		final List<Area> areas = new ArrayList<Area>();
		for (ModelItem i : items) {
			if (i instanceof Area) {
				areas.add((Area) i);
			}
		}

		PasteOperations.pasteAreas(areas, selected, workbenchWindow);
	}

	private void pasteFloors(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = FloorTransfer.getInstance();

		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();
		final List<Floor> floors = new ArrayList<Floor>();

		for (ModelItem i : items) {
			if (i instanceof Floor) {
				floors.add((Floor) i);
			}
		}

		PasteOperations.pasteFloors(floors, selected, workbenchWindow);
	}

	private void pasteTasks(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = TaskTransfer.getInstance();
		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();

		final List<Task> tasksToPaste = new ArrayList<Task>();
		for (ModelItem i : items) {
			if (i instanceof Task) {
				tasksToPaste.add((Task) i);
			}
		}

		PasteOperations.pasteTasks(tasksToPaste, selected, workbenchWindow);
	}

	private void pasteSchedules(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = ScheduleTransfer.getInstance();
		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();

		final List<Schedule> schedsToPaste = new ArrayList<Schedule>();
		for (ModelItem i : items) {
			if (i instanceof Schedule) {
				schedsToPaste.add((Schedule) i);
			}
		}

		PasteOperations
				.pasteSchedules(schedsToPaste, selected, workbenchWindow);
	}

	private void pasteTimers(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		final ModelItemTransfer transfer = DeviceTimerTransfer.getInstance();
		final ModelItem[] items = (ModelItem[]) clipboard.getContents(transfer);
		if (items == null || items.length == 0)
			return;

		final Object selected = selection.getFirstElement();

		final List<DeviceTimer> timersToPaste = new ArrayList<DeviceTimer>();
		for (ModelItem i : items) {
			if (i instanceof DeviceTimer) {
				timersToPaste.add((DeviceTimer) i);
			}
		}

		PasteOperations.pasteTimers(timersToPaste, selected, workbenchWindow);
	}

	private void pasteModelItems(final Clipboard clipboard,
			final IStructuredSelection selection,
			final IWorkbenchWindow workbenchWindow) {
		if (DeviceTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteDevices(clipboard, selection, workbenchWindow);
		}
		if (AreaTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteAreas(clipboard, selection, workbenchWindow);
		}
		if (FloorTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteFloors(clipboard, selection, workbenchWindow);
		}
		if (TaskTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteTasks(clipboard, selection, workbenchWindow);
		}
		if (ScheduleTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteSchedules(clipboard, selection, workbenchWindow);
		}
		if (DeviceTimerTransfer.getInstance().clipboardHasType(clipboard)) {
			pasteTimers(clipboard, selection, workbenchWindow);
		}
	}

	/**
	 * Performs the paste.<br/>
	 * If the event does not hold selected elements or if the operation is not
	 * allowed, the operation will perform nothing.
	 * 
	 * @param event
	 *            execution event
	 * @return null
	 * @throws ExecutionException
	 *             never thrown
	 */
	@Override
	public Object executeDefault(ExecutionEvent event)
			throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structSelection = (IStructuredSelection) selection;

			if (structSelection.size() == 0) {
				return null;
			}

			// verify if the operation is allowed
			final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof lumina.ui.views.navigation.NavigationView
					&& !Capabilities.canDo(Capability.DEVICE_EDIT_TREE)) {
				return null;
			} else if (activePart instanceof lumina.ui.views.timer.TimerView
					&& !Capabilities.canDo(Capability.TIMER_EDIT_TREE)) {
				return null;
			}

			// paste
			try {
				final IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindow(event);

				pasteModelItems(ClipboardUtils.getClipboard(), structSelection,
						window);

				SelectionUtils.doUpdateSelectionSourceProvider(null, selection);
			} catch (SWTError error) {
				/*
				 * Copy to clipboard failed. This happens when another
				 * application is accessing the clipboard while we copy. Ignore
				 * the error.
				 */
			}
		}

		return null;
	}
}
