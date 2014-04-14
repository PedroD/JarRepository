package lumina.ui.views.blueprint.actions;

import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handler that zooms-in the plan view.
 */
public class ZoomInHandler extends AbstractHandler {

	@Override
	public final Object execute(ExecutionEvent execEvent)
			throws ExecutionException {
		final BlueprintView blueprintView = BlueprintView.findBlueprintView();
		if (blueprintView != null) {
			blueprintView.zoomIn(null);
		}

		return null;
	}
}
