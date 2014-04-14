package lumina.ui.actions;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.base.model.transfer.TransferFactory;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedHandler;
import lumina.ui.views.navigation.NavigationView;
import lumina.ui.views.timer.TimerView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Cut the selected items to the clipboard.
 * <p>
 * Creates the following transfer types:
 * <ol>
 * <li>Text -- which results in the copying the items a list of names.</li>
 * <li>ModelItem -- a XML representation of the model tree.</li>
 * </ol>
 */
public final class CutItemsToClipboardHandler extends InterceptedHandler {

	private static final String CUT_COMMAND_ID = "org.eclipse.ui.edit.cut";

	/**
	 * Returns the command identifier.
	 * 
	 * @return command identifier
	 * @see lumina.ui.swt.handlers.InterceptedHandler#getCommandId()
	 */
	@Override
	public String getCommandId() {
		return CUT_COMMAND_ID;
	}

	/**
	 * Translates the selection into text.<br/>
	 * Only ModelItem objects will be translated into strings.
	 * 
	 * @param selection
	 *            object array
	 * @return text selection as text
	 * @see lumina.base.model.ModelUtils#toModelItems(Object[])
	 * @see lumina.base.model.ModelUtils#toText(ModelItem[])
	 */
	protected static String asText(Object[] selection) {
		final ModelItem[] items = ModelUtils.toModelItems(selection);
		final String text = ModelUtils.toText(items);
		return text;
	}

	/**
	 * Cuts the contents of the object into the clipboard.
	 * 
	 * @param event
	 *            event
	 * @return null
	 * @throws ExecutionException
	 *             never thrown
	 * @see lumina.ui.swt.handlers.InterceptedHandler#execute(ExecutionEvent)
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

			final Object[] selectionItems = structSelection.toArray();

			// verify if the operation is allowed
			final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof NavigationView
					&& !Capabilities.canDo(Capability.DEVICE_EDIT_TREE)) {
				return null;
			} else if (activePart instanceof TimerView
					&& !Capabilities.canDo(Capability.TIMER_EDIT_TREE)) {
				return null;
			}

			try {
				final ModelItem[] items = ModelUtils
						.asModelItems(structSelection.toArray());

				final Transfer transfer = TransferFactory.getTransferFor(items);
				final Clipboard targetClipboard = ClipboardUtils.getClipboard();
				targetClipboard.clearContents();

				if (transfer != null) {
					targetClipboard.setContents(
							new Object[] { asText(selectionItems),
									ModelUtils.asModelItems(selectionItems) },
							new Transfer[] { TextTransfer.getInstance(),
									transfer });
				}

				final IWorkbenchWindow workbenchWindow = HandlerUtil
						.getActiveWorkbenchWindow(event);

				// Delete the items
				final Object selected = structSelection.getFirstElement();
				if (selected instanceof Device) {
					Device[] devices = ModelUtils.toDevices(selectionItems);
					IAction action = new DeleteDevicesAction(devices,
							workbenchWindow, "Cut");
					action.run();
				} else if (selected instanceof Area) {
					Area[] areas = ModelUtils.toAreas(selectionItems);
					IAction action = new DeleteAreasAction(areas,
							workbenchWindow, "Cut");
					action.run();
				} else if (selected instanceof Floor) {
					Floor[] floors = ModelUtils.toFloors(selectionItems);
					IAction action = new DeleteFloorsAction(floors,
							workbenchWindow, "Cut");
					action.run();
				} else if (selected instanceof Task) {
					Task[] tasks = ModelUtils.toTasks(selectionItems);
					IAction action = new DeleteTasksAction(tasks,
							workbenchWindow, "Cut");
					action.run();
				} else if (selected instanceof Schedule) {
					Schedule[] scheds = ModelUtils.toSchedules(selectionItems);
					IAction action = new DeleteSchedulesAction(scheds,
							workbenchWindow, "Cut");
					action.run();
				} else if (selected instanceof DeviceTimer) {
					DeviceTimer[] timers = ModelUtils.toTimers(selectionItems);
					IAction action = new DeleteTimersAction(timers,
							workbenchWindow, "Cut");
					action.run();
				}

				// reselect(selection);
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
