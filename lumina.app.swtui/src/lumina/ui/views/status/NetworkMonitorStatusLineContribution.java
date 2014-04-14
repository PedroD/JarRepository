package lumina.ui.views.status;

import lumina.base.model.Area;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.network.MessageScheduler;
import lumina.network.NetworkInterfaceManager;
import lumina.network.gateways.api.IGateway;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A contribution to the Status Line containing a network monitor.
 * <p>
 * Displays the status of the network connection at the bottom.
 * <p>
 * It registers it self as PropertyChangeListener in order to register itself
 * automatically when a project is loaded. It also registers itself as a
 * SelectionListener.
 */
public final class NetworkMonitorStatusLineContribution extends
		ContributionItem implements IPropertyChangeListener, ISelectionListener {

	/**
	 * The contribution id.
	 */
	private static final String ID = "lumina.contributions.statusbar.network"; // NON-NLS-1

	/**
	 * The relative path of the interface icon.
	 */
	private static final String INTERFACE_ICON = "icons/model/interface.png"; // NON-NLS-1

	/**
	 * The Constant MANUAL.
	 */
	private static final String MANUAL = Messages
			.getString("NetworkMonitorStatus.RefreshManual"); //$NON-NLS-1$ 

	/**
	 * The Constant AUTO.
	 */
	private static final String AUTO = Messages
			.getString("NetworkMonitorStatus.RefreshAutomatic"); //$NON-NLS-1$ 

	/**
	 * String used to compute the space of two letter boxes.
	 */
	private static final char M = 'M';

	/**
	 * The length to be reserved for the name of the interface.
	 */
	private static final int INTERFACE_NAME_CHAR_LENGTH = 18;

	/**
	 * The lazily created instance.
	 */
	private static NetworkMonitorStatusLineContribution createdInstance;

	/**
	 * Track whether we are already registered as selection listeners.
	 */
	private boolean selectionListenerRegistered;

	/**
	 * Tracks the currently selected interface. Can be <code>null</code> if no
	 * valid interface is selected.
	 */
	private IGateway currentInterface;

	/**
	 * The last selection needed to recompute the current interface.
	 */
	private ISelection currentSelection;

	/**
	 * Maintains the reference to the display.
	 */
	private Display display;

	private CLabel interfaceNameLabel;

	private CLabel autoManualLabel;

	private InterfaceStatusContribution networkConnectionFeedbackLabel;

	/**
	 * Constructs a new item with the give id. Cannot be instantiated from the
	 * outside.
	 * 
	 * @param id
	 *            the item id.
	 */
	private NetworkMonitorStatusLineContribution(final String id) {
		super(id);

		/*
		 * Register for the property change events
		 */
		ProjectModel.getInstance().addPropertyChangeListener(this);
	}

	/**
	 * Registers the selection listeners.
	 */
	private void ensureSelectionListenersRegistered() {
		if (!selectionListenerRegistered) {
			final IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();

			if (window != null) {
				final ISelectionService selectionService = window
						.getSelectionService();

				if (selectionService != null) {
					selectionService.addPostSelectionListener(this);

					selectionListenerRegistered = true;
				}
			}
		}
	}

	/**
	 * Removes the monitor from the list of selection listeners.
	 */
	private void removeSelectionListeners() {
		if (selectionListenerRegistered) {
			final IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();

			if (window != null) {
				final ISelectionService selectionService = window
						.getSelectionService();

				if (selectionService != null) {
					selectionService.removePostSelectionListener(this);

					selectionListenerRegistered = false;
				}
			}
		}
	}

	/**
	 * Obtains the project interface.
	 * 
	 * @return the project interface if at least one interface is defines or
	 *         <code>null</code> otherwise.
	 */
	private static IGateway getProjectInterface() {
		final Project p = ProjectModel.getInstance().getProject();
		if (p != null) {
			return p.getDefaultInterface();
		}
		return null;
	}

	/**
	 * Computes the interface in the scope of a selection.
	 * <p>
	 * If there are multiple interfaces defined for the items selected, no
	 * interface applies. If no interface corresponds to the selection then it
	 * checks the current project to find one.
	 * 
	 * @param selection
	 *            a selection
	 * @return a interface (the same) shared by all items of the selection; or
	 *         <code>null</code> if multiple interfaces are in scope.
	 */
	private static IGateway getInterfaceFromSelection(
			final ISelection selection) {
		if (selection != null) {
			final Object[] selectedItems = SelectionUtils
					.getSelection(selection);
			if (selectedItems != null && selectedItems.length > 0) {
				final ModelItem[] items = ModelUtils
						.toModelItems(selectedItems);
				final IGateway[] interfaces = Queries
						.getAllInterfaces(items);

				if (interfaces.length == 0) {
					return getProjectInterface();
				} else if (interfaces.length == 1) {
					return interfaces[0];
				} else {
					/*
					 * Multiple interfaces found: no single interface can be
					 * selected
					 */
					return null;
				}
			}

			/*
			 * No selection: return the project's default
			 */
			return getProjectInterface();
		}

		return null;
	}

	/**
     * 
     */
	private final IGateway.ConnectionStatusListener connectionStatusListener = new IGateway.ConnectionStatusListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.IGateway.StatusListener#interfacePortAbsent
		 * (lumina.network .INetworkInterface)
		 */
		public void transportPortAbsent(final IGateway networkInterface) {
			if (networkInterface == currentInterface) {
				doUpdateNetworkStatus(networkInterface);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.IGateway.StatusListener#networkConnectionLost
		 * (lumina.network .INetworkInterface)
		 */
		public void gatewayConnectionLost(
				final IGateway networkInterface) {
			if (networkInterface == currentInterface) {
				doUpdateNetworkStatus(networkInterface);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.IGateway.StatusListener#networkFound(lumina
		 * .network. IGateway)
		 */
		public void gatewayFound(final IGateway networkInterface) {
			if (networkInterface == currentInterface) {
				doUpdateNetworkStatus(networkInterface);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.IGateway.StatusListener#networkSearching(
		 * lumina.network. IGateway)
		 */
		public void gatewaySearching(final IGateway networkInterface) {
			if (networkInterface == currentInterface) {
				doUpdateNetworkStatus(networkInterface);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.IGateway.StatusListener#networkClosed(lumina
		 * .network. IGateway)
		 */
		public void gatewayConnectionClosed(
				final IGateway networkInterface) {
			if (networkInterface == currentInterface) {
				doUpdateNetworkStatus(networkInterface);
			}
		}
	};

	/**
     * 
     */
	private final MessageScheduler.DispatcherStatusListener dispatcherStatusListener = new MessageScheduler.DispatcherStatusListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.MessageScheduler.StatusListener#dispatcherBusy(lumina
		 * .network. MessageScheduler)
		 */
		public void dispatcherBusy(final MessageScheduler scheduler) {
			final IGateway networkInterface = scheduler.getInterface();
			if (networkInterface == currentInterface) {
				networkConnectionFeedbackLabel.setNetworkBusy(networkInterface,
						true);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.MessageScheduler.StatusListener#dispatcherNormal(lumina
		 * .network. MessageScheduler)
		 */
		public void dispatcherNormal(final MessageScheduler scheduler) {
			final IGateway networkInterface = scheduler.getInterface();
			if (networkInterface == currentInterface) {
				networkConnectionFeedbackLabel.setNetworkBusy(networkInterface,
						false);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * lumina.network.MessageScheduler.StatusListener#dispatcherStopped(
		 * lumina.network .MessageScheduler)
		 */
		public void dispatcherStopped(final MessageScheduler scheduler) {
			final IGateway networkInterface = scheduler.getInterface();
			if (networkInterface == currentInterface) {
				networkConnectionFeedbackLabel.setNetworkBusy(networkInterface,
						false);
			}
		}
	};

	/**
	 * Do select interface.
	 * 
	 * @param networkInterface
	 *            the network interface
	 */
	private void doSelectInterface(final IGateway networkInterface) {
		currentInterface = networkInterface;
		doUpdateNetworkStatus(networkInterface);
	}

	/**
	 * Do update network status.
	 * 
	 * @param networkInterface
	 *            the network interface
	 */
	private void doUpdateNetworkStatus(final IGateway networkInterface) {
		if (display != null && !display.isDisposed()) {
			// ensure that the network label is updated!
			networkConnectionFeedbackLabel.updateStatus(networkInterface);

			display.asyncExec(new Runnable() {
				public void run() {
					if (networkInterface != null) {
						setLabel(interfaceNameLabel,
								networkInterface.getGatewayConnectionName());
						final Image networkInterfaceImage = ApplicationImageCache
								.getInstance().getImage(INTERFACE_ICON);

						if (!interfaceNameLabel.isDisposed()) {
							interfaceNameLabel.setImage(networkInterfaceImage);
						}

						if (networkInterface.getRefreshMode() == IGateway.DeviceStatusPollMode.CONTINUOUS) {
							setLabel(autoManualLabel, AUTO);
						} else {
							if (networkInterface.getRefreshMode() == IGateway.DeviceStatusPollMode.ON_REQUEST) {
								setLabel(autoManualLabel, MANUAL);
							}
						}
					} else {
						setLabel(interfaceNameLabel, "");

						if (!interfaceNameLabel.isDisposed()) {
							interfaceNameLabel.setImage(null);
						}

						setLabel(autoManualLabel, "");
					}
				}
			});
		}
	}

	/**
	 * Sets the label.
	 * 
	 * @param label
	 *            the label
	 * @param s
	 *            the s
	 */
	private void setLabel(final CLabel label, final String s) {
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					if (!label.isDisposed()) {
						label.setText(s);
					}
				}
			});
		}
	}

	/**
	 * Create a string with with a char repeated a number of times.
	 * 
	 * @param chr
	 *            the char to be repeated
	 * @param n
	 *            the number of times that the string should be repeated
	 * @return a new string with filled with the specified char n times.
	 */
	private static String repeat(final char chr, final int n) {
		if (n > 0) {
			final char[] buffer = new char[n];
			for (int i = 0; i < n; i++) {
				buffer[i] = chr;
			}

			return new String(buffer);
		} else {
			return "";
		}
	}

	/**
	 * Returns the default instance.
	 * 
	 * @return the default instance.
	 */
	public static NetworkMonitorStatusLineContribution getInstance() {
		if (createdInstance == null) {
			createdInstance = new NetworkMonitorStatusLineContribution(ID);
		}

		return createdInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void fill(final Composite parent) {
		display = parent.getDisplay();

		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		interfaceNameLabel = new CLabel(parent, SWT.CENTER);
		final Label separator2 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		networkConnectionFeedbackLabel = new InterfaceStatusContribution(parent);
		final Label separator3 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		autoManualLabel = new CLabel(parent, SWT.CENTER);

		final StatusLineLayoutData autoManualLayoutData = new StatusLineLayoutData();
		final Point autoManualDims = SWTUtils.getMaxStringDimensions(parent,
				new String[] { MANUAL, AUTO });
		autoManualLayoutData.widthHint = autoManualDims.x;
		autoManualLayoutData.heightHint = autoManualDims.y;
		autoManualLabel.setLayoutData(autoManualLayoutData);

		final StatusLineLayoutData separatorLayoutData = new StatusLineLayoutData();
		separatorLayoutData.widthHint = 1;
		separatorLayoutData.heightHint = autoManualDims.y;
		separator.setLayoutData(separatorLayoutData);
		separator2.setLayoutData(separatorLayoutData);
		separator3.setLayoutData(separatorLayoutData);

		final StatusLineLayoutData interfaceNameLayoutData = new StatusLineLayoutData();
		final Point interfNamelDims = SWTUtils.getMaxStringDimensions(parent,
				new String[] { repeat(M, INTERFACE_NAME_CHAR_LENGTH) });
		interfaceNameLayoutData.widthHint = interfNamelDims.x;
		interfaceNameLayoutData.heightHint = interfNamelDims.y;
		interfaceNameLabel.setLayoutData(interfaceNameLayoutData);

		doSelectInterface(null);
		parent.layout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	@Override
	public void dispose() {
		removeSelectionListeners();
		ProjectModel.getInstance().removePropertyChangeListener(this);

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IWorkbenchPart part,
			final ISelection selection) {
		currentSelection = selection;

		doSelectInterface(getInterfaceFromSelection(selection));
	}

	/**
	 * Handles the property change events.
	 * <p>
	 * When the project is changed it registers the network monitor. See
	 * 
	 * @param event
	 *            the event
	 *            {@link NetworkInterfaceManager#addConnectionStatusListener(lumina.network.gateways.api.IGateway.ConnectionStatusListener)}
	 *            .
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		final Object subject = event.getSource();
		final String changedProperty = event.getProperty();

		if (PropertyChangeNames.isMetadataChange(changedProperty)) {
			/*
			 * Handle the change of metadata of a interface like its name,
			 * refresh rate or status.
			 */
			if (subject instanceof IGateway) {
				final IGateway p = (IGateway) subject;
				if (p == currentInterface) {
					doUpdateNetworkStatus(p);
				}
			}

			/*
			 * Handle the change of the interface of an area
			 */
			if (subject instanceof Area) {
				final IGateway p = getInterfaceFromSelection(currentSelection);
				doSelectInterface(p);
			}

			/*
			 * Handle the change of the project default interface
			 */
			if (subject instanceof Project) {
				final IGateway p = getInterfaceFromSelection(currentSelection);
				doSelectInterface(p);
			}
		} else if (changedProperty
				.equals(PropertyChangeNames.INTERFACE_REMOVED)) {
			/*
			 * Handle the removal of the interface.
			 */
			final IGateway p = getInterfaceFromSelection(currentSelection);
			doUpdateNetworkStatus(p);
		} else if (changedProperty.equals(PropertyChangeNames.INTERFACE_ADDED)) {
			/*
			 * Handle the add of the interface.
			 */
			final IGateway p = getInterfaceFromSelection(currentSelection);
			doSelectInterface(p);
		} else if (PropertyChangeNames.isProjectLoading(changedProperty)) {
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
					manager.addConnectionStatusListener(connectionStatusListener);
					manager.addDispatcherListener(dispatcherStatusListener);

					ensureSelectionListenersRegistered();
				}
			}

			currentInterface = null;
			doUpdateNetworkStatus(null);
		}
	}
}
