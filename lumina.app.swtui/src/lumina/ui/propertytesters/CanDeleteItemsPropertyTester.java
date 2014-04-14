package lumina.ui.propertytesters;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.handlers.InterceptedPropertyTester;

import org.eclipse.jface.viewers.ISelection;

/**
 * A property tester for the delete operation.
 * <p>
 * Tests current a given selection can be deleted.
 */
public class CanDeleteItemsPropertyTester extends InterceptedPropertyTester {

	/**
	 * ID of the delete command.
	 */
	private static final String DELETE_COMMAND_ID = "org.eclipse.ui.edit.delete";

	/**
	 * Checks if a selection can be deleted.
	 * 
	 * @param items
	 *            the current selection.
	 * @return <code>true</code> if all items are of the same type and not a
	 *         project; <code>false</code> otherwise.
	 */
	public static boolean canDelete(final ModelItem[] items) {
		if (items.length == 0) {
			return false;
		} else {
			final ModelItem firstObject = items[0];
			return !ModelUtils.isProject(firstObject)
					&& ModelUtils.sameType(ModelUtils.toModelItems(items));
		}
	}

	/**
	 * Returns the delete command identifier.
	 * 
	 * @return delete command identifier
	 */
	@Override
	public final String getCommandId() {
		return DELETE_COMMAND_ID;
	}

	/**
	 * Checks if the receiver is a selection and, if so, checks if it is
	 * possible to delete the selected model items.
	 * 
	 * @param receiver
	 *            selection ({@link ISelection})
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return true if model items are selected and can be deleted, false
	 *         otherwise
	 */
	@Override
	public final boolean testProperty(Object receiver, String property,
			Object[] args, Object expectedValue) {

		if (receiver instanceof ISelection) {
			final Object[] selection = SelectionUtils
					.getSelection((ISelection) receiver);

			final ModelItem[] items = ModelUtils.toModelItems(selection);
			if (items != null && items.length >= 1) {
				final boolean copyIsEnabled = canDelete(items);
				return copyIsEnabled;
			}
		}
		return false;
	}
}
