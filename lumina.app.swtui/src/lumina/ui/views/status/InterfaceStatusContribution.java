package lumina.ui.views.status;

import lumina.network.gateways.api.IGateway;
import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A label that displays the network status used in
 * {@link NetworkMonitorStatusLineContribution}
 * <p>
 * .
 */
public final class InterfaceStatusContribution extends CLabel {

	private static final int NOT_CONNECTED_BACKGROUND = SWT.COLOR_WIDGET_BACKGROUND;
	private static final int NOT_CONNECTED_FOREGORUND = SWT.COLOR_WIDGET_FOREGROUND;

	private static final int NORMAL_BACKGROUND = SWT.COLOR_WIDGET_DARK_SHADOW;
	private static final int NORMAL_FOREGORUND = SWT.COLOR_WIDGET_LIGHT_SHADOW;

	private static final int WARNING_BACKGROUND = SWT.COLOR_YELLOW;
	private static final int WARNING_FOREGORUND = SWT.COLOR_WIDGET_DARK_SHADOW;

	private static final int ERROR_BACKGROUND = SWT.COLOR_RED;
	private static final int ERROR_FOREGORUND = SWT.COLOR_WHITE;

	private static final String INTERFACE_ABSENT_STATUS = Messages
			.getString("NetworkMonitorStatus.InterfaceAbsent"); //$NON-NLS-1$ 

	private static final String NOT_CONNECTED_STATUS = Messages
			.getString("NetworkMonitorStatus.NotConnected"); //$NON-NLS-1$ 

	private static final String CONNECT_STATUS = Messages
			.getString("NetworkMonitorStatus.Connected"); //$NON-NLS-1$ 

	private static final String SEARCHING_STATUS = Messages
			.getString("NetworkMonitorStatus.Searching"); //$NON-NLS-1$ 

	private static final String ERROR_STATUS = Messages
			.getString("NetworkMonitorStatus.Error"); //$NON-NLS-1$ 

	private static final String BUSY_STATUS = Messages
			.getString("NetworkMonitorStatus.Busy"); //$NON-NLS-1$ 

	private static final String INTERFACE_ABSENT_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.InterfaceAbsentToolTip"); //$NON-NLS-1$ 

	private static final String NO_CONNECTION_REQUESTED_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.NoConnectionRequestedToolTip"); //$NON-NLS-1$ 

	private static final String VALIDATING_CONNECTION_REQUESTED_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.ValidatingConnectionRequestToolTip"); //$NON-NLS-1$ 

	private static final String FOUND_CONNECTION_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.ConnectionFoundToolTip"); //$NON-NLS-1$ 

	private static final String CONNECTION_LOST_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.ConnectionLostToolTip"); //$NON-NLS-1$ 

	private static final String NETWORK_BUSY_TOOLTIP = Messages
			.getString("NetworkMonitorStatus.BusyToolTip"); //$NON-NLS-1$ 

	/**
	 * Tracks whether the network is busy or not.
	 */
	private boolean networkBusy;

	/**
	 * Creates a new status contribution.
	 * 
	 * @param parent
	 *            a parent (the status bar)
	 */
	public InterfaceStatusContribution(Composite parent) {
		super(parent, SWT.CENTER);

		final StatusLineLayoutData networkStatusLayoutData = new StatusLineLayoutData();
		final Point dims = SWTUtils.getMaxStringDimensions(parent,
				new String[] { INTERFACE_ABSENT_STATUS, NOT_CONNECTED_STATUS,
						CONNECT_STATUS, SEARCHING_STATUS, ERROR_STATUS });

		networkStatusLayoutData.widthHint = dims.x;
		networkStatusLayoutData.heightHint = dims.y;
		this.setLayoutData(networkStatusLayoutData);
	}

	/**
	 * Obtains the correct message according to the status of the network.
	 * <p>
	 * If the interface is <code>null</code> the status is cleared.
	 * 
	 * @param networkInterface
	 *            the interface, can be <code>null</code>
	 */
	private synchronized void doSetupMessage(
			final IGateway networkInterface) {
		/*
		 * since this method is run asynchronously we have to check if we were
		 * not disposed in the meanwhile
		 */
		if (InterfaceStatusContribution.this.isDisposed()) {
			return;
		}

		if (networkInterface != null) {
			if (networkBusy) {
				this.setText(BUSY_STATUS);
				this.setToolTipText(NETWORK_BUSY_TOOLTIP);
				this.setBackground(Display.getCurrent().getSystemColor(
						ERROR_BACKGROUND));
				this.setForeground(Display.getCurrent().getSystemColor(
						ERROR_FOREGORUND));
			} else {
				if (networkInterface.getConnectionStatus() == IGateway.GatewayConnectionStatus.TRANSPORT_CONNECTION_NOT_REQUESTED) {
					this.setText(NOT_CONNECTED_STATUS);
					this.setToolTipText(NO_CONNECTION_REQUESTED_TOOLTIP);
					this.setBackground(Display.getCurrent().getSystemColor(
							NOT_CONNECTED_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							NOT_CONNECTED_FOREGORUND));
				} else if (networkInterface.getConnectionStatus() == IGateway.GatewayConnectionStatus.TRANSPORT_PORT_ABSENT) {
					this.setText(INTERFACE_ABSENT_STATUS);
					this.setToolTipText(INTERFACE_ABSENT_TOOLTIP);
					this.setBackground(Display.getCurrent().getSystemColor(
							ERROR_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							ERROR_FOREGORUND));
				} else if (networkInterface.getConnectionStatus() == IGateway.GatewayConnectionStatus.VALIDATING_GATEWAY_CONNECTION) {
					this.setText(SEARCHING_STATUS);
					this.setToolTipText(VALIDATING_CONNECTION_REQUESTED_TOOLTIP);
					this.setBackground(Display.getCurrent().getSystemColor(
							WARNING_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							WARNING_FOREGORUND));
				} else if (networkInterface.getConnectionStatus() == IGateway.GatewayConnectionStatus.GATEWAY_CONNECTION_VALIDATED) {
					this.setText(CONNECT_STATUS);
					this.setToolTipText(FOUND_CONNECTION_TOOLTIP);
					this.setBackground(Display.getCurrent().getSystemColor(
							NORMAL_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							NORMAL_FOREGORUND));
				} else if (networkInterface.getConnectionStatus() == IGateway.GatewayConnectionStatus.TRANSPORT_CONNECTION_LOST) {
					this.setText(ERROR_STATUS);
					this.setToolTipText(CONNECTION_LOST_TOOLTIP);
					this.setBackground(Display.getCurrent().getSystemColor(
							ERROR_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							ERROR_FOREGORUND));
				} else {
					this.setText("");
					this.setToolTipText(null);
					this.setBackground(Display.getCurrent().getSystemColor(
							NOT_CONNECTED_BACKGROUND));
					this.setForeground(Display.getCurrent().getSystemColor(
							NOT_CONNECTED_FOREGORUND));
				}
			}
		} else {
			this.setText("");
			this.setToolTipText(null);
			this.setBackground(Display.getCurrent().getSystemColor(
					NOT_CONNECTED_BACKGROUND));
			this.setForeground(Display.getCurrent().getSystemColor(
					NOT_CONNECTED_FOREGORUND));
		}
	}

	/**
	 * Updates the status of the status bar contribution according to the status
	 * of the network interface.
	 * 
	 * @param networkInterface
	 *            the network interface to be checked.
	 */
	public synchronized void updateStatus(
			final IGateway networkInterface) {
		final Display display = Display.getDefault();

		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					doSetupMessage(networkInterface);
				}
			});
		}
	}

	/**
	 * Informs the busy status of a network interface.
	 * <p>
	 * A new status message will be displayed.
	 * 
	 * @param networkInterface
	 *            the network interface
	 * @param busy
	 *            indicates whether the network is busy
	 */
	public synchronized void setNetworkBusy(
			final IGateway networkInterface, final boolean busy) {
		networkBusy = busy;
		updateStatus(networkInterface);
	}
}
