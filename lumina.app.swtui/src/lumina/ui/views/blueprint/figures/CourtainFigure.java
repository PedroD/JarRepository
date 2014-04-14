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
 * Figure that represents a courtain.
 */
public class CourtainFigure extends ConcentricDeviceFigure {

	/**
	 * Predefined shape box size.
	 */
	private static final int PREDEFINED_FIGURE_SIZE = 15;

	private Courtain blinderFigure;

	/**
	 * Shape that represents a courtain.
	 */
	private class Courtain extends RectangleFigure {
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
			double w = getSize().width / 7.0;
			double h = getSize().height / 5.0;

			int x = getLocation().x;
			int y = getLocation().y;

			graphics.setBackgroundColor(getDeviceOnColor());
			graphics.setForegroundColor(getDeviceOnColor());

			if (isStopped) {
				graphics.drawLine((int) (x + w), (int) (y + h - 2),
						(int) (x + w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 2 * w), (int) (y + h - 2),
						(int) (x + 2 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 5 * w), (int) (y + h - 2),
						(int) (x + 5 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 6 * w), (int) (y + h - 2),
						(int) (x + 6 * w), (int) (y + h * 4 + 1));
			} else if (isOpen) {
				graphics.drawLine((int) (x + w), (int) (y + h - 2),
						(int) (x + w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + w * 6), (int) (y + h - 2),
						(int) (x + w * 6), (int) (y + h * 4 + 1));

			} else {
				graphics.drawLine((int) (x + w), (int) (y + h - 2),
						(int) (x + w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 2 * w), (int) (y + h - 2),
						(int) (x + 2 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 3 * w), (int) (y + h - 2),
						(int) (x + 3 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 4 * w), (int) (y + h - 2),
						(int) (x + 4 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 5 * w), (int) (y + h - 2),
						(int) (x + 5 * w), (int) (y + h * 4 + 1));

				graphics.drawLine((int) (x + 6 * w), (int) (y + h - 2),
						(int) (x + 6 * w), (int) (y + h * 4 + 1));
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

	public CourtainFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	protected void updateStatus(final DeviceStatus status) {
		if (status instanceof BidirectionalStatus) {
			BidirectionalStatus directionStatus = (BidirectionalStatus) status;

			blinderFigure.setStatus(directionStatus.isStopped(),
					directionStatus.getDirection());
		}
	}

	@Override
	public IFigure createDeviceFigure(final Device device) {
		blinderFigure = new Courtain();
		blinderFigure.setPreferredSize(PREDEFINED_FIGURE_SIZE,
				PREDEFINED_FIGURE_SIZE);

		updateStatus(device.getStatus());
		return blinderFigure;
	}
}
