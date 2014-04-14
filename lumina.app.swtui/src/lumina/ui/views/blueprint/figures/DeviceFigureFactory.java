package lumina.ui.views.blueprint.figures;

import lumina.base.model.Device;
import lumina.base.model.devices.BlindDevice;
import lumina.base.model.devices.ControlPanelDevice;
import lumina.base.model.devices.CurtainsDevice;
import lumina.base.model.devices.FluorescentLampDevice;
import lumina.base.model.devices.IncandescentLampDevice;

import org.eclipse.swt.widgets.Shell;

/**
 * Factory that creates the device figures.
 */
public final class DeviceFigureFactory {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private DeviceFigureFactory() {
	}

	/**
	 * Creates a figure for a device on a floor figure.
	 * 
	 * @param d
	 *            the device object
	 * @param f
	 *            the floor figure
	 * @param shell
	 *            the shell
	 * @return a device figure dependent on the type of device.
	 */
	public static DeviceFigure createDeviceFigure(Device d, FloorFigure f,
			Shell shell) {
		if (d instanceof IncandescentLampDevice) {
			return new IncandescentLampFigure(d, f, shell);
		} else if (d instanceof FluorescentLampDevice) {
			return new FlourescentLampFigure(d, f, shell);
		} else if (d instanceof ControlPanelDevice) {
			return new ControlPanelFigure(d, f, shell);
		} else if (d instanceof BlindDevice) {
			return new BlinderFigure(d, f, shell);
		} else if (d instanceof CurtainsDevice) {
			return new CourtainFigure(d, f, shell);
		} else {
			return new GenericDeviceFigure(d, f, shell);
		}
	}
}
