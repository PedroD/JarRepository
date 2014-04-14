package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.devices.status.OnOffIntensityStatus;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * The figure that represents an fluorescent lamp.
 */
public class FlourescentLampFigure extends ConcentricDeviceFigure {

	/**
	 * Predefined shape box size.
	 */
	private static final int PREDEFINED_FIGURE_SIZE = 15;

	private static final int MAX_INTENSITY_LEVEL = 100;

	private static final int DEFAULT_INTENSITY_LEVEL = 50;

	private LampFigure lampFigure;

	/**
	 * Rectangle shape representing the lamp figure.
	 */
	private class LampFigure extends RectangleFigure {

		private int intensity = DEFAULT_INTENSITY_LEVEL;

		private int getExtent() {
			final double pixels = (intensity * getSize().width)
					/ (MAX_INTENSITY_LEVEL * 1.0);
			return (int) pixels;
		}

		public void setIntensityPercent(final int i) {
			if (intensity >= 0 && intensity <= MAX_INTENSITY_LEVEL) {
				intensity = i;

				repaint();
			}
		}

		@Override
		protected void fillShape(final Graphics graphics) {
			graphics.setAntialias(SWT.ON);
			graphics.setBackgroundColor(getDeviceOffColor());

			super.fillShape(graphics);

			graphics.setBackgroundColor(getDeviceOnColor());
			final Rectangle intensityGauge = getBounds().getCopy().setSize(
					getExtent(), getSize().height);

			graphics.fillRectangle(intensityGauge);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.setAntialias(SWT.ON);
			graphics.setForegroundColor(getDeviceOnColor());
			graphics.setBackgroundColor(getDeviceOffColor());

			super.outlineShape(graphics);
		}
	}

	public FlourescentLampFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	protected final void updateStatus(final DeviceStatus status) {
		if (status instanceof OnOffIntensityStatus) {
			final OnOffIntensityStatus intensityStatus = (OnOffIntensityStatus) status;

			lampFigure.setIntensityPercent(intensityStatus
					.getIntensityPercent());
		}
	}

	@Override
	public final IFigure createDeviceFigure(final Device device) {
		lampFigure = new LampFigure();
		lampFigure.setPreferredSize(PREDEFINED_FIGURE_SIZE * 2,
				(int) (PREDEFINED_FIGURE_SIZE / 2.0));

		updateStatus(device.getStatus());

		return lampFigure;
	}
}
