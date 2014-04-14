package lumina.ui.views.control;

import lumina.base.model.Device;

/**
 * The user interface behavior of a device.
 * <p>
 * User interfaces for devices should are expected to implement this interface
 * override the method {@link #updateControls(Device)}.
 */
public interface DeviceUI {

	/**
	 * Kinds of fade feed-back on the user interface.
	 */
	enum ReportKind {
		/**
		 * Indicates that a timer for the device is about to start. For example
		 * a command as been submitted but was not yet sent to the network or is
		 * connected to a timer.
		 */
		STARTING,
		/**
		 * Indicates that the last command sent is finishing in the specified
		 * amount of time.
		 */
		FINISHING,
		/**
		 * Indicates that the status of the timer is unknown. For example due to
		 * a network error.
		 */
		DONT_KNOW
	};

	/**
	 * Updates the device UI controls according device provided.
	 * 
	 * @param device
	 *            the new device.
	 */
	void updateControls(final Device device);

	/**
	 * Updates the timers of the device UI.
	 * 
	 * @param device
	 *            the device being controlled.
	 * @param kind
	 *            the type of report being performed
	 * @param duration
	 *            the duration, this can be elapsed time of time-to-finish
	 */
	void updateTimers(final Device device, final ReportKind kind,
			final int duration);

	/**
	 * Perform the last operation on the user interface after pack/layout have
	 * been called.
	 * <p>
	 * Experimental: This method exists because we could not find another way of
	 * drawing inside the canvas because the canvas size is only set after
	 * layout() is called.
	 */
	void finishLayout();
}
