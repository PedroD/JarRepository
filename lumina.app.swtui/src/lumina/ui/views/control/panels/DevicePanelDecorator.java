package lumina.ui.views.control.panels;

import org.eclipse.swt.widgets.Composite;

import lumina.base.model.Device;
import lumina.base.model.devices.paneltypes.OnOffIntensityPanelType;
import lumina.base.model.devices.paneltypes.OpenClosePanelType;
import lumina.base.model.devices.paneltypes.PanelType;
import lumina.base.model.devices.paneltypes.ScenarioPanelType;
import lumina.base.model.devices.paneltypes.UpDownPanelType;

// TODO: Auto-generated Javadoc
/**
 * The Class DevicePanelDecorator.
 * 
 * Receives a device and draws a GUI panel for it.
 */
public final class DevicePanelDecorator {

	/** The device. */
	private Device device;

	/**
	 * Instantiates a new device panel decorator.
	 * 
	 * @param d
	 *            the device to decorate
	 */
	public DevicePanelDecorator(Device d) {
		device = d;
	}

	/**
	 * Gets the panel.
	 * 
	 * @return the panel
	 */
	public Composite getPanel(final Composite parent, final int style,
			final Device device) {
		/*
		 * Such hardcoded.
		 * 
		 * Very programming.
		 * 
		 * Many Java.
		 */
		PanelType panelType = device.getDeviceType().getPanelType();
		if (panelType instanceof ScenarioPanelType)
			return new ScenarioPanel(parent, style, device);
		else if (panelType instanceof UpDownPanelType)
			return new UpDownPanel(parent, style, device);
		else if (panelType instanceof OpenClosePanelType)
			return new OpenClosePanel(parent, style, device);
		else if (panelType instanceof OpenClosePanelType)
			return new OpenClosePanel(parent, style, device);
		else
			return new OnOffIntensityPanel(parent, style, device);
	}
}
