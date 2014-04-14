package lumina.ui.propertytesters;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.base.model.transfer.AreaTransfer;
import lumina.base.model.transfer.DeviceTimerTransfer;
import lumina.base.model.transfer.DeviceTransfer;
import lumina.base.model.transfer.FloorTransfer;
import lumina.base.model.transfer.ModelItemTransfer;
import lumina.base.model.transfer.ScheduleTransfer;
import lumina.base.model.transfer.TaskTransfer;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedPropertyTester;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;

/**
 * A property tester for the
 * {@link lumina.ui.actions.PasteItemsFromClipboardHandler} operation.
 * <p>
 * Tests current clipboard contents can be be pasted into a given selection.
 */
public class CanPasteItemsPropertyTester extends InterceptedPropertyTester {

	/**
	 * ID of the paste command.
	 */
	private static final String PASTE_COMMAND_ID = "org.eclipse.ui.edit.paste";

	/**
	 * Returns the paste command identifier.
	 * 
	 * @return paste command identifier
	 */
	@Override
	public final String getCommandId() {
		return PASTE_COMMAND_ID;
	}

	/**
	 * Checks if the currently selected object accepts paste from the clipboard.
	 * 
	 * @param clipboard
	 *            the clipboard object
	 * @param selectedItem
	 *            the object currently selected
	 * @return <code>true</code> if the object in the clipboard can be pasted
	 *         into the currently selected object.
	 */
	private boolean acceptsPaste(final Clipboard clipboard,
			final ModelItem selectedItem) {
		final ModelItemTransfer floorTx = FloorTransfer.getInstance();
		final ModelItemTransfer areaTx = AreaTransfer.getInstance();
		final ModelItemTransfer deviceTx = DeviceTransfer.getInstance();

		final ModelItemTransfer taskTx = TaskTransfer.getInstance();
		final ModelItemTransfer scheduleTx = ScheduleTransfer.getInstance();
		final ModelItemTransfer timerTx = DeviceTimerTransfer.getInstance();

		if (selectedItem instanceof Device) {
			return deviceTx.clipboardHasType(clipboard)
					|| areaTx.clipboardHasType(clipboard)
					|| floorTx.clipboardHasType(clipboard);
		} else if (selectedItem instanceof Area) {
			return deviceTx.clipboardHasType(clipboard)
					|| areaTx.clipboardHasType(clipboard)
					|| floorTx.clipboardHasType(clipboard);
		} else if (selectedItem instanceof Floor) {
			return areaTx.clipboardHasType(clipboard)
					|| floorTx.clipboardHasType(clipboard);
		}
		if (selectedItem instanceof Task) {
			return taskTx.clipboardHasType(clipboard)
					|| scheduleTx.clipboardHasType(clipboard)
					|| timerTx.clipboardHasType(clipboard);
		}
		if (selectedItem instanceof Schedule) {
			return taskTx.clipboardHasType(clipboard)
					|| scheduleTx.clipboardHasType(clipboard)
					|| timerTx.clipboardHasType(clipboard);
		}
		if (selectedItem instanceof DeviceTimer) {
			return scheduleTx.clipboardHasType(clipboard)
					|| timerTx.clipboardHasType(clipboard);
		} else if (selectedItem instanceof Project) {
			return floorTx.clipboardHasType(clipboard);
		} else {
			return false;
		}
	}

	/**
	 * Checks if the items currently on the clipboard can be pasted into the
	 * current selection.
	 * 
	 * @param clipboard
	 *            the clipboard object.
	 * @param selectedItems
	 *            the current selection.
	 * @return <code>true</code> if the clipboard object can be pasted into the
	 *         selection and <code>false</code> otherwise.
	 */
	final boolean canPaste(final Clipboard clipboard,
			final ModelItem[] selectedItems) {
		if (ModelUtils.sameType(selectedItems)
				&& ModelUtils.sameParent(selectedItems)) {
			final boolean clipboardOk = clipboard != null
					&& !clipboard.isDisposed();
			return clipboardOk
					&& acceptsPaste(clipboard, (ModelItem) selectedItems[0]);
		} else {
			return false;
		}
	}

	/**
	 * Checks if the receiver is a selection and, if so, checks if it is
	 * possible to paste the selected model items.
	 * 
	 * @param receiver
	 *            selection ({@link ISelection})
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return true if model items are selected and can be pasted, false
	 *         otherwise
	 */
	public boolean testProperty(Object receiver, String property,
			Object[] args, Object expectedValue) {

		if (receiver instanceof ISelection) {
			final Object[] selection = SelectionUtils
					.getSelection((ISelection) receiver);
			final ModelItem[] items = ModelUtils.toModelItems(selection);
			if (items != null && items.length >= 1) {
				final boolean pasteIsEnabled = canPaste(
						ClipboardUtils.getClipboard(), items);
				return pasteIsEnabled;
			}
		}
		return false;
	}
}
