package lumina.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lumina.network.NetworkInterfaceManager;
import lumina.network.gateways.api.IGateway;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * Handler for a network interface menu line.
 */
public class ToggleInterfaceConnectionStatusHandler extends AbstractHandler
		implements IElementUpdater {

	/**
	 * The menu elements for tracking.
	 */
	private List<UIElement> uiElements = new LinkedList<UIElement>();

	/**
	 * Checked/unchecked status of the communication interface.
	 */
	private volatile boolean checkedStatus;

	/**
	 * Refernce to the interface manager.
	 */
	private final NetworkInterfaceManager protocolManager;

	/**
	 * The communication interface monitored by this handler.
	 */
	private final IGateway communicationInterface;

	/**
	 * Updates the "check" mark according to the network interface status.
	 */
	private final IGateway.ConnectionStatusListener connectionStatusListener = new IGateway.ConnectionStatusListener() {

		/**
		 * Network found.
		 * 
		 * @param protocol
		 *            network protocol
		 */
		public synchronized void gatewayFound(IGateway protocol) {
			if (protocol == communicationInterface) {
				checkedStatus = true;
				ToggleInterfaceConnectionStatusHandler.this.updateElements();
			}
		}

		/**
		 * Network search.
		 * 
		 * @param protocol
		 *            network protocol
		 */
		public synchronized void gatewaySearching(IGateway protocol) {
			if (protocol == communicationInterface) {
				checkedStatus = true;
				ToggleInterfaceConnectionStatusHandler.this.updateElements();
			}
		}

		/**
		 * Interface port not present.
		 * 
		 * @param protocol
		 *            network protocol
		 */
		public synchronized void transportPortAbsent(IGateway protocol) {
			if (protocol == communicationInterface) {
				checkedStatus = false;
				ToggleInterfaceConnectionStatusHandler.this.updateElements();
			}
		}

		/**
		 * Network connection lost.
		 * 
		 * @param protocol
		 *            network protocol
		 */
		public synchronized void gatewayConnectionLost(
				IGateway protocol) {
			if (protocol == communicationInterface) {
				ToggleInterfaceConnectionStatusHandler.this.checkedStatus = false;
				updateElements();
			}
		}

		/**
		 * Network closed.
		 * 
		 * @param protocol
		 *            network protocol
		 */
		public synchronized void gatewayConnectionClosed(
				IGateway protocol) {
			if (protocol == communicationInterface) {
				checkedStatus = false;
				ToggleInterfaceConnectionStatusHandler.this.updateElements();
			}
		}
	};

	/**
	 * Creates a new interface menu item.
	 * 
	 * @param manager
	 *            the network manager object
	 * @param networkInterface
	 *            the network interface
	 */
	public ToggleInterfaceConnectionStatusHandler(
			NetworkInterfaceManager manager,
			final IGateway networkInterface) {
		if (manager == null) {
			throw new IllegalArgumentException(
					"The protocol manager cannot be null");
		}

		if (networkInterface == null) {
			throw new IllegalArgumentException(
					"The communication interface object cannot be null");
		}

		protocolManager = manager;
		communicationInterface = networkInterface;

		checkedStatus = networkInterface.hasTransportConnection();

		protocolManager.addConnectionStatusListener(connectionStatusListener);
	}

	/**
	 * Terminate.<br/>
	 * Removes itself from the network manager status listener.
	 * 
	 * @see lumina.network.NetworkInterfaceManager
	 */
	@Override
	public void dispose() {
		protocolManager.removeNetworkStatusListener(connectionStatusListener);
		super.dispose();
	}

	/**
	 * Updates the state of the menu.
	 */
	private void updateElements() {
		final IWorkbench wb = PlatformUI.getWorkbench();
		if (wb != null) {
			final Display display = wb.getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						for (UIElement e : uiElements) {
							e.setChecked(checkedStatus);
						}
					}
				});
			}
		}
	}

	/**
	 * Toggles the plan view expanded state.
	 * <p>
	 * Switches to the plan view and then toggles its zoom state.
	 * 
	 * @param execEvent
	 *            the execution event
	 * @return <code>null</code>
	 * @throws ExecutionException
	 *             the execution exception
	 */
	@Override
	public synchronized Object execute(final ExecutionEvent execEvent)
			throws ExecutionException {
		checkedStatus = !checkedStatus;

		if (checkedStatus) {
			communicationInterface.open();
		} else {
			communicationInterface.close();
		}

		updateElements();
		return null;
	}

	/**
	 * Saves the UI element and hooks a window listener.
	 * 
	 * @param element
	 *            the element to be saved
	 * @param parameters
	 *            ignored
	 */
	public void updateElement(final UIElement element,
			@SuppressWarnings("rawtypes") final Map parameters) {
		uiElements.add(element);

		updateElements();
	}
}
