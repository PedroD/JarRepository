package lumina.ui.views.blueprint.propertytesters;

import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * Tests if a set of devices can be removed in the plan.
 */
public class CanRemoveFromPlanPropertyTester extends PropertyTester {

	public static final boolean canRemoveFromBluePrint(ModelItem[] items) {
		if (items.length > 0) {
			if (ModelUtils.areAllDevices(items)) {
				final Device[] devicesToRemove = ModelUtils
						.getAllDevices(items);

				if (devicesToRemove.length > 0
						&& ModelUtils.sameFloor(devicesToRemove)) {
					final BlueprintView planView = BlueprintView
							.findBlueprintView();
					return planView != null && planView.isImageReady()
							&& !ModelUtils.areAllInvisible(devicesToRemove);
				}
			}
		}

		return false;
	}

	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof TreeSelection) {
			final TreeSelection selection = (TreeSelection) receiver;

			final ModelItem[] items = ModelUtils.toModelItems(selection
					.toArray());
			return canRemoveFromBluePrint(items);
		}

		return false;
	}
}
