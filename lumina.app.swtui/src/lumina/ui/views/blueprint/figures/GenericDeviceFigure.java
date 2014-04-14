package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;

import org.eclipse.swt.widgets.Shell;

/**
 * A generic default device figure for a device without any registered shape.
 */
public final class GenericDeviceFigure extends ConcentricDeviceFigure {

	public GenericDeviceFigure(Device d, FloorFigure f, Shell shell) {
		super(d, f, shell);
	}
}
