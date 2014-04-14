package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.devices.status.OnOffIntensityStatus;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * The figure that represents an incandescent lamp.
 */
public class IncandescentLampFigure extends ConcentricDeviceFigure {

	/**
	 * Predefined shape box size.
	 */
	private static final int PREDEFINED_FIGURE_SIZE = 15;

	private static final int MAX_INTENSITY_LEVEL = 100;

	private static final int DEFAULT_INTENSITY_LEVEL = 50;

	private static final int ZERO_GRADES = 90;

	private LampFigure lampFigure;

	/**
	 * Shape that represents an incandecent lamp figure.
	 */
	private class LampFigure extends Ellipse {

		private int intensity = DEFAULT_INTENSITY_LEVEL;

		private int getExtent() {
			// CHECKSTYLE:OFF
			// FIXME: change this
			final double grades = (intensity * 360) / 100.0;
			// CHECKSTYLE:ON
			return (int) -grades;
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
			graphics.fillArc(getBounds(), ZERO_GRADES, getExtent());
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.setAntialias(SWT.ON);
			graphics.setForegroundColor(getDeviceOnColor());
			graphics.setBackgroundColor(getDeviceOffColor());

			super.outlineShape(graphics);
		}
	}

	public IncandescentLampFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	protected void updateStatus(final DeviceStatus status) {
		if (status instanceof OnOffIntensityStatus) {
			final OnOffIntensityStatus intensityStatus = (OnOffIntensityStatus) status;
			lampFigure.setIntensityPercent(intensityStatus
					.getIntensityPercent());
		}
	}

	@Override
	public IFigure createDeviceFigure(final Device device) {
		lampFigure = new LampFigure();
		lampFigure.setPreferredSize(PREDEFINED_FIGURE_SIZE,
				PREDEFINED_FIGURE_SIZE);

		updateStatus(device.getStatus());

		return lampFigure;
	}
}
