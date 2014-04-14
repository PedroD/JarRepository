package lumina.ui.views.blueprint.propertytesters;

import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tests if the blueprint view can be zoomed in.
 */
public class CanZoomInPropertyTester extends PropertyTester {

	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		final BlueprintView view = BlueprintView.findBlueprintView();

		if (view != null) {
			return view.canZoomIn();
		} else {
			return false;
		}
	}
}
