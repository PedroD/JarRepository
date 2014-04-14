package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.devices.status.BidirectionalStatus;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Implements rectangular blinder figure.
 */
public class BlinderFigure extends ConcentricDeviceFigure {

	/**
	 * Predefined shape box size.
	 */
	private static final int PREDEFINED_FIGURE_SIZE = 15;

	/**
	 * The blinder rectangular figure.
	 */
	private class Blinder extends RectangleFigure {
		private boolean isOpen;
		private boolean isStopped;

		public void setStatus(final boolean stopped, final boolean open) {
			isStopped = stopped;
			isOpen = open;

			repaint();
		}

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
			graphics.setForegroundColor(getDeviceOnColor());

			if (isStopped) {
				graphics.drawLine((int) (x + w - 2), (int) (y + h), (int) (x
						+ w * 4 + 1), (int) (y + h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 2 * h),
						(int) (x + w * 4 + 1), (int) (y + 2 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 3 * h),
						(int) (x + w * 4 + 1), (int) (y + 3 * h));
			} else if (isOpen) {
				graphics.drawLine((int) (x + w - 2), (int) (y + h), (int) (x
						+ w * 4 + 1), (int) (y + h));

				// graphics.drawLine((int) (x + w), (int) (y + h), (int) w * 4,
				// (int) h);
				//
				// graphics.drawLine((int) (x + w), (int) (y + 3 * h), (int) w *
				// 4, (int)
				// h);
				// graphics.drawLine((int) (x + 3 * w), (int) (y + 3 * h), (int)
				// w * 4,
				// (int) h);
				//
				// graphics.drawLine((int) (x + w), (int) (y + 5 * h), (int) w *
				// 4, (int)
				// h);
				// graphics.drawLine((int) (x + 3 * w), (int) (y + 5 * h), (int)
				// w * 4,
				// (int) h);

			} else {
				graphics.drawLine((int) (x + w - 2), (int) (y + h), (int) (x
						+ w * 4 + 1), (int) (y + h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 2 * h),
						(int) (x + w * 4 + 1), (int) (y + 2 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 3 * h),
						(int) (x + w * 4 + 1), (int) (y + 3 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 4 * h),
						(int) (x + w * 4 + 1), (int) (y + 4 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 5 * h),
						(int) (x + w * 4 + 1), (int) (y + 5 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 5 * h),
						(int) (x + w * 4 + 1), (int) (y + 5 * h));

				graphics.drawLine((int) (x + w - 2), (int) (y + 6 * h),
						(int) (x + w * 4 + 1), (int) (y + 6 * h));
			}
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

	/**
	 * The blinder figure instance.
	 */
	private Blinder blinderFigure;

	public BlinderFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	protected void updateStatus(final DeviceStatus status) {
		if (status instanceof BidirectionalStatus) {
			final BidirectionalStatus directionStatus = (BidirectionalStatus) status;

			blinderFigure.setStatus(directionStatus.isStopped(),
					directionStatus.getDirection());
		}
	}

	@Override
	public IFigure createDeviceFigure(final Device device) {
		blinderFigure = new Blinder();
		blinderFigure.setPreferredSize(PREDEFINED_FIGURE_SIZE,
				PREDEFINED_FIGURE_SIZE);

		updateStatus(device.getStatus());
		return blinderFigure;
	}
}
