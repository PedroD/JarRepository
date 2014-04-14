package lumina.ui.views.blueprint.actions;

import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handler that zooms the blueprint to the default zoom level.
 */
public class ZoomToDefaultHandler extends AbstractHandler {

	@Override
	public final Object execute(ExecutionEvent execEvent)
			throws ExecutionException {
		final BlueprintView blueprintView = BlueprintView.findBlueprintView();
		if (blueprintView != null) {
			blueprintView.zoomToDefault(null);
		}

		return null;
	}
}
