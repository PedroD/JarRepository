package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Figure that represents a control panel.
 */
public class ControlPanelFigure extends ConcentricDeviceFigure {

	/**
	 * Predefined shape box size.
	 */
	private static final int PREDEFINED_FIGURE_SIZE = 15;

	private ControlPanel controlPanelFigure;

	public ControlPanelFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	/**
	 * The control panel rectangle figure with a few buttons represented.
	 */
	private class ControlPanel extends RectangleFigure {

		@Override
		protected void fillShape(final Graphics graphics) {
			graphics.setAntialias(SWT.ON);
			graphics.setBackgroundColor(getDeviceOffColor());

			super.fillShape(graphics);

			// CHECKSTYLE:OFF
			// FIXME: This representation is going to be replaced!
			final double w = getSize().width / 5.0;
			final double h = getSize().height / 7.0;

			final int x = getLocation().x;
			final int y = getLocation().y;

			graphics.setBackgroundColor(getDeviceOnColor());

			graphics.fillRectangle((int) (x + w), (int) (y + h), (int) w,
					(int) h);
			graphics.fillRectangle((int) (x + 3 * w), (int) (y + h), (int) w,
					(int) h);

			graphics.fillRectangle((int) (x + w), (int) (y + 3 * h), (int) w,
					(int) h);
			graphics.fillRectangle((int) (x + 3 * w), (int) (y + 3 * h),
					(int) w, (int) h);

			graphics.fillRectangle((int) (x + w), (int) (y + 5 * h), (int) w,
					(int) h);
			graphics.fillRectangle((int) (x + 3 * w), (int) (y + 5 * h),
					(int) w, (int) h);
			// CHECKSTYLE:ON
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.setAntialias(SWT.ON);

			graphics.setForegroundColor(getDeviceOnColor());
			graphics.setBackgroundColor(getDeviceOffColor());

			super.outlineShape(graphics);
		}
	}

	protected void updateStatus(final DeviceStatus status) {
		// Do nothing.
	}

	@Override
	public IFigure createDeviceFigure(final Device device) {
		controlPanelFigure = new ControlPanel();
		controlPanelFigure.setPreferredSize(PREDEFINED_FIGURE_SIZE,
				PREDEFINED_FIGURE_SIZE);
		updateStatus(device.getStatus());
		return controlPanelFigure;
	}
}
