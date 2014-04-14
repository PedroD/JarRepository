package lumina.ui.views.control.propertytesters;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tests if the blueprint view can be zoomed in.
 */
public class CanShowDeviceControlPropertyTester extends PropertyTester {

	public static final boolean canDisplayDeviceControl(
			final StructuredSelection selection) {
		final ModelItem[] items = ModelUtils.toModelItems(selection.toArray());
		return items.length == 1 && ModelUtils.areAllDevices(items);
	}

	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof StructuredSelection) {
			final StructuredSelection selection = (StructuredSelection) receiver;
			return canDisplayDeviceControl(selection);
		}
		return false;
	}
}
