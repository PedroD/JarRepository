package lumina.ui.views.blueprint.propertytesters;

import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Queries;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;

/**
 * Tests if a set of devices or areas can be centered in the plan.
 */
public class CanCenterInPlanPropertyTester extends PropertyTester {

	public static final boolean canCenterInBluePrint(ISelection selection) {
		final Object[] selectedObjects = SelectionUtils.getSelection(selection);
		final ModelItem[] items = ModelUtils.toModelItems(selectedObjects);
		return canCenterInBluePrint(items);
	}

	public static final boolean canCenterInBluePrint(ModelItem[] items) {
		if (items.length > 0) {
			/*
			 * The devices do not need to be all visible. Those that are not
			 * visible are ignored.
			 */
			if (ModelUtils.areAllAreasOrDevices(items)) {
				final Device[] devicesToCenter = ModelUtils
						.getAllDevices(items);

				if (devicesToCenter.length > 0
						&& ModelUtils.sameFloor(devicesToCenter)) {
					final Floor floor = Queries
							.getAncestorFloor(devicesToCenter[0]);

					if (floor.isFloorPlanAssigned()
							&& !ModelUtils.areAllInvisible(devicesToCenter)) {
						final BlueprintView view = BlueprintView
								.findBlueprintView();
						return view != null && view.canCenterViewPort();
					}
				}
			}
		}

		return false;
	}

	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		if (receiver instanceof ISelection) {
			final ISelection selection = (ISelection) receiver;
			return canCenterInBluePrint(selection);
		}

		return false;
	}
}
