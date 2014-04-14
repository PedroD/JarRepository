package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * A device figure whose center is computed by halving the its height and width.
 */
public abstract class ConcentricDeviceFigure extends DeviceFigure {

	public ConcentricDeviceFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}

	/**
	 * @return the center of the rectangle
	 */
	protected final Point getFigureCenter() {
		final Dimension dims = this.getSize();
		return new Point(dims.width, dims.height).scale(1 / 2.0);
	}

}
