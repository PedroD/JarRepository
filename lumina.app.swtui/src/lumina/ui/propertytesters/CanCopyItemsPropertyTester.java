package lumina.ui.propertytesters;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedPropertyTester;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;

/**
 * A property tester for the
 * {@link lumina.ui.actions.CopyItemsToClipboardHandler} operation.
 * <p>
 * Tests current a given selection can be copied to the clipboard.
 */
public class CanCopyItemsPropertyTester extends InterceptedPropertyTester {

	/**
	 * The Id of the copy command.
	 */
	private static final String COPY_COMMAND_ID = "org.eclipse.ui.edit.copy";

	/**
	 * Checks if a selection can be copied into the clipboard.
	 * 
	 * @param clipboard
	 *            the clipboard object.
	 * @param items
	 *            the current selection.
	 * @return <code>true</code> if the clipboard is valid and the selection
	 *         items are all of the same type, <code>false</code> otherwise.
	 */
	protected static boolean canCopy(final Clipboard clipboard,
			final ModelItem[] items) {
		// FIXME: This code must be enhanced: to be made dependent on the
		// selection
		// itself.
		final boolean clipboardOk = clipboard != null
				&& !clipboard.isDisposed();
		return clipboardOk && ModelUtils.sameType(items);
	}

	/**
	 * Returns the copy command identifier.
	 * 
	 * @return copy command identifier
	 */
	@Override
	public String getCommandId() {
		return COPY_COMMAND_ID;
	}

	/**
	 * Checks if the receiver is a selection and, if so, checks if it is
	 * possible to copy the selected model items.
	 * 
	 * @param receiver
	 *            selection ({@link ISelection})
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return true if model items are selected and can be copied, false
	 *         otherwise
	 */
	@Override
	public boolean testProperty(Object receiver, String property,
			Object[] args, Object expectedValue) {

		if (receiver instanceof ISelection) {
			final Object[] selection = SelectionUtils
					.getSelection((ISelection) receiver);

			final ModelItem[] items = ModelUtils.toModelItems(selection);
			if (items != null && items.length >= 1) {
				final boolean copyIsEnabled = canCopy(
						ClipboardUtils.getClipboard(), items);
				return copyIsEnabled;
			}
		}
		return false;
	}
}
