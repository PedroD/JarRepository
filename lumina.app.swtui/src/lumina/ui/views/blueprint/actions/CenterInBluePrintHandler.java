package lumina.ui.views.blueprint.actions;

import java.util.ArrayList;
import java.util.List;

import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for centering a device in the plan view.
 */
public class CenterInBluePrintHandler extends AbstractHandler {

	public static Rectangle getBoundingBox(final Device[] devices) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = 0;
		int maxY = 0;

		for (Device d : devices) {
			final int x = d.getXCoordinate();
			final int y = d.getYCoordinate();
			if (x < minX) {
				minX = x;
			}
			if (x > maxX) {
				maxX = x;
			}
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}
		}

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	/**
	 * Computes the mean point of a set of devices.
	 * <p>
	 * This method is used to adequately center the plan when the user selects
	 * multiple devices and then selected the center into plan option.
	 * 
	 * @param devices
	 *            the set of devices
	 * @return the centering point of the devices
	 */
	public static final Point getMeanPoint(final Device[] devices) {
		final List<Device> visibleDevices = new ArrayList<Device>();
		for (Device d : devices) {
			if (d.isVisible()) {
				visibleDevices.add(d);
			}
		}

		final Rectangle r = getBoundingBox(visibleDevices
				.toArray(new Device[0]));
		final Point meanPoint = new Point(r.x + r.width / 2, r.y + r.height / 2);
		return meanPoint;
	}

	@Override
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		final Object[] selections = SelectionUtils.getSelection(HandlerUtil
				.getCurrentSelection(execEvent));

		if (selections != null) {
			final ModelItem[] items = ModelUtils.toModelItems(selections);
			final Device[] devicesToCenter = ModelUtils.getAllDevices(items);

			if (BlueprintView.findBlueprintView() != null) {
				if (devicesToCenter != null && devicesToCenter.length > 0) {
					final Point p = getMeanPoint(devicesToCenter);
					BlueprintView.findBlueprintView().centerViewport(p.x, p.y);
				}
			}
		}

		return null;
	}
}
