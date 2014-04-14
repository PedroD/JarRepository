package lumina.ui.views.control.panels;

import lumina.base.model.Device;
import lumina.base.model.IDeviceDriver;
import lumina.ui.views.control.DeviceUI;

import org.eclipse.swt.widgets.Composite;

/**
 * The base class for implementing the user interface of a device.
 * <p>
 * Implements the feed back of the fade time of a device command.
 */
public abstract class AbstractDevicePanel extends Composite implements DeviceUI {

	/**
	 * The device being controlled.
	 */
	protected final Device deviceUnderDisplay;

	/**
	 * Instantiates a new abstract device panel.
	 * 
	 * @param parent
	 *            the parent panel
	 * @param style
	 *            the SWT style
	 * @param device
	 *            the device to control
	 */
	public AbstractDevicePanel(Composite parent, int style, Device device) {
		super(parent, style);
		deviceUnderDisplay = device;
	}

	/**
	 * Gets the device being controlled.
	 * 
	 * @return the device being controlled
	 */
	protected final Device getDeviceUnderDisplay() {
		return deviceUnderDisplay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.base.model.DeviceUI#updateControls(lumina.base.model.Device)
	 */
	public abstract void updateControls(final Device device);

	/**
	 * Hides the fader.
	 * <p>
	 */
	public void finishLayout() {
		updateFade(false, 0);
	}

	/**
	 * Shows the fade of the last command on the device UI.
	 * <p>
	 * Descending classes should implement this method to provide appropriate
	 * fade report behavior.
	 * 
	 * @param visible
	 *            determines if the fade info show be made visible or not.
	 * @param duration
	 *            the duration of the fade to be shown in milliseconds
	 */
	protected abstract void updateFade(final boolean visible, final int duration);

	/**
	 * Reacts to updates in the fade timer.
	 * <p>
	 * This method interprets the timer elapsed reports, determines if the fade
	 * show be displayed on not and calls {@link #updateFade(boolean, int)} with
	 * the appropriate parameters.
	 * 
	 * @param device
	 *            the device
	 * @param kind
	 *            the kind
	 * @param duration
	 *            the duration
	 */
	public final void updateTimers(final Device device, final ReportKind kind,
			final int duration) {
		final IDeviceDriver driver = device.getDriver();
		if (driver.getCommandDuration() > UIConstants.COMMAND_FADE_DURATION_TRESHOLD) {
			if (kind == ReportKind.STARTING || kind == ReportKind.FINISHING) {
				if (duration != 0) {
					updateFade(true, duration);
				} else {
					/*
					 * hide when we reach the end!
					 */
					updateFade(false, 0);
				}
			} else if (kind == ReportKind.DONT_KNOW) {
				updateFade(false, -1);
			}
		} else {
			updateFade(false, duration);
		}
	}
}
