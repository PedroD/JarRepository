package lumina.ui.propertytesters;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedPropertyTester;

import org.eclipse.jface.viewers.ISelection;

/**
 * A property tester for the
 * {@link lumina.ui.actions.CutItemsToClipboardHandler} operation.
 * <p>
 * Tests if a given selection can be cut to the clipboard. Checks if the
 * selections can first be deleted and the copied into the clipboard.
 */
public class CanCutItemsPropertyTester extends InterceptedPropertyTester {

	/**
	 * ID of the cut command.
	 */
	private static final String CUT_COMMAND_ID = "org.eclipse.ui.edit.cut";

	/**
	 * Returns the cut command identifier.
	 * 
	 * @return cut command identifier
	 */
	@Override
	public final String getCommandId() {
		return CUT_COMMAND_ID;
	}

	/**
	 * Checks if the receiver is a selection and, if so, checks if it is
	 * possible to cut the selected model items.
	 * 
	 * @param receiver
	 *            selection ({@link ISelection})
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return true if model items are selected and can be cut, false otherwise
	 * @see CanCopyItemsPropertyTester#testProperty(Object, String, Object[],
	 *      Object)
	 */
	public final boolean testProperty(Object receiver, String property,
			Object[] args, Object expectedValue) {

		if (receiver instanceof ISelection) {
			final Object[] selection = SelectionUtils
					.getSelection((ISelection) receiver);

			final ModelItem[] items = ModelUtils.toModelItems(selection);
			if (items != null && items.length >= 1) {

				/*
				 * Reuse canCopy and canDelete
				 */
				final boolean cutIsEnabled = CanDeleteItemsPropertyTester
						.canDelete(items)
						&& CanCopyItemsPropertyTester.canCopy(
								ClipboardUtils.getClipboard(), items);

				return cutIsEnabled;
			}
		}

		return false;
	}
}
