package lumina.ui.views.blueprint.propertytesters;

import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Queries;
import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * Tests if a device can be added to the plan.
 */
public class CanAddToPlanPropertyTester extends PropertyTester {

	/**
	 * Checks if an array of devices can be added to the plan view.
	 * 
	 * @param devices
	 *            the array of devices
	 * @param floor
	 *            the floor to which their images are to be added
	 * @return <code>true</code> if all devices pertain to the floor specified
	 *         and if at least one of them is still not visible.
	 */
	public static final boolean canAddToBluePrint(final Device[] devices,
			final Floor floor) {
		if (devices.length > 0 && ModelUtils.sameFloor(devices, floor)) {
			final BlueprintView planView = BlueprintView.findBlueprintView();
			return planView != null && planView.isImageReady()
					&& !ModelUtils.areAllVisible(devices);
		}

		return false;
	}

	public static final boolean canAddToBluePrint(final ModelItem[] items) {
		if (items.length > 0) {
			if (ModelUtils.areAllDevices(items)) {
				final Device[] devicesToAdd = ModelUtils.getAllDevices(items);

				if (devicesToAdd.length > 0
						&& ModelUtils.sameFloor(devicesToAdd)) {
					final Floor floor = Queries
							.getAncestorFloor(devicesToAdd[0]);
					/*
					 * This is not full-proof since we the image can be assigned
					 * but rendering can still be failing.
					 */
					return floor.isFloorPlanAssigned()
							&& !ModelUtils.areAllVisible(devicesToAdd);
				} else {
					return false;
				}
			}
		}

		return false;
	}

	public final boolean test(final Object receiver, final String property,
			final Object[] args, final Object expectedValue) {
		if (receiver instanceof TreeSelection) {
			final TreeSelection selection = (TreeSelection) receiver;

			final ModelItem[] items = ModelUtils.toModelItems(selection
					.toArray());
			return canAddToBluePrint(items);
		}

		return false;
	}
}
