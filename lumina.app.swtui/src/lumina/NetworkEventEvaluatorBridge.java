package lumina;

import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.network.NetworkInterfaceManager;
import lumina.network.gateways.api.IGateway;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Monitors the network status and forces the re-evaluation of the
 * {@link org.eclipse.core.expressions.PropertyTester}s.
 * <p>
 * This class is needed to assure the re-evaluation of the property testers when
 * the network status changes.
 * <p>
 * It registers it self as PropertyChangeListener in order to register itself
 * automatically when a project is loaded.
 */
public final class NetworkEventEvaluatorBridge implements
		IPropertyChangeListener {

	/**
	 * The lazily created instance.
	 */
	private static NetworkEventEvaluatorBridge createdInstance;

	/**
	 * Constructs a new reselect bridge.
	 * 
	 * @param id
	 *            the item id.
	 */
	private NetworkEventEvaluatorBridge() {

		/*
		 * Register for the property change events
		 */
		ProjectModel.getInstance().addPropertyChangeListener(this);
	}

	private final IGateway.ConnectionStatusListener connectionStatuslistener = new IGateway.ConnectionStatusListener() {
		public void transportPortAbsent(IGateway networkInterface) {
			doEvaluatePropertyTesters();
		}

		public void gatewayConnectionLost(IGateway networkInterface) {
			doEvaluatePropertyTesters();
		}

		public void gatewayFound(IGateway networkInterface) {
			doEvaluatePropertyTesters();
		}

		public void gatewaySearching(IGateway networkInterface) {
			doEvaluatePropertyTesters();
		}

		public void gatewayConnectionClosed(IGateway networkInterface) {
			doEvaluatePropertyTesters();
		}
	};

	/**
	 * Reselects the current selection causing the property testers to be
	 * evaluated.
	 */
	private void doEvaluatePropertyTesters() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		SelectionUtils.doUpdateSelectionSourceProvider(window, null);
	}

	/**
	 * Returns the default instance.
	 * 
	 * @return the default instance.
	 */
	public static NetworkEventEvaluatorBridge getInstance() {
		if (createdInstance == null) {
			createdInstance = new NetworkEventEvaluatorBridge();
		}

		return createdInstance;
	}

	public void dispose() {
		ProjectModel.getInstance().removePropertyChangeListener(this);
	}

	/**
	 * Handles the property change events.
	 * <p>
	 * When the project is changed it registers the network monitor. See
	 * {@link NetworkInterfaceManager#addConnectionStatusListener(lumina.network.gateways.api.IGateway.ConnectionStatusListener)
	 * )}.
	 * 
	 * @param event
	 *            the event
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		final Object subject = event.getSource();
		final String changedProperty = event.getProperty();

		if (PropertyChangeNames.isProjectLoading(changedProperty)) {
			/*
			 * Handle the change of the current project.
			 */
			if (subject instanceof Project) {
				final Project project = (Project) subject;
				final NetworkInterfaceManager manager = project
						.getNetworkInterfaceManager();

				if (manager != null) {
					/*
					 * Set the network status listener as a network monitor in
					 * order to receive all the network events.
					 */
					manager.addConnectionStatusListener(connectionStatuslistener);
				}
			}
		}
	}
}
