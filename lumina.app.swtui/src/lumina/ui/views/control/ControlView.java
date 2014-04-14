package lumina.ui.views.control;

import java.util.Timer;
import java.util.TimerTask;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.base.model.validators.DeviceConnectivityProblem;
import lumina.network.MessageScheduler;
import lumina.network.NetworkInterfaceManager;
import lumina.network.TimestampedConnectivityStatus;
import lumina.network.gateways.api.IGateway;
import lumina.ui.views.ViewUtils;
import lumina.ui.views.control.panels.DevicePanelDecorator;
import lumina.ui.views.control.panels.UIConstants;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Control view.
 */
public class ControlView extends ViewPart implements ISelectionListener,
		IPropertyChangeListener {

	/**
	 * The view ID.
	 */
	public static final String ID = "lumina.views.control"; //$NON-NLS-1$

	/**
	 * Name of the update timer for debug.
	 */
	private static final String UPDATE_TIMER_NAME = "ControlView update timer"; //$NON-NLS-1$

	/**
	 * The refresh rate for updating the interface timers.
	 */
	private static final int UPDATE_TIMER_REFRESH_RATE = 500;

	private static final String NO_VALID_SELECTION = Messages
			.getString("ControlView.noDeviceSelected"); //$NON-NLS-1$

	private static final String MULTIPLE_ITEMS_SELECTED = Messages
			.getString("ControlView.multipleItemsSelected"); //$NON-NLS-1$    

	private static final String WARNING_PANEL_MESSAGE = Messages
			.getString("ControlView.cannotContactDevice"); //$NON-NLS-1$    

	private static final String BUSY_PANEL_MESSAGE = Messages
			.getString("ControlView.networkBusy"); //$NON-NLS-1$

	/**
	 * Contains the reference to the label containing the warning.
	 */
	private CLabel labelWarning;

	/**
	 * A reference to the device under control.
	 */
	private Device deviceUnderDisplay;

	/**
	 * A composite that keeps the device interface.
	 */
	private Composite compositeUnderDisplay;

	/**
	 * The last selectionCache cache. Used to detect changes of selectionCache
	 * and refrain from creating controls every time a select event comes in.
	 */
	private IStructuredSelection selectionCache;

	/**
	 * The top composite. Used to create the device UI panels.
	 */
	private Composite top;

	/**
	 * The timer that sends the timer update events to the device panel while
	 * commands are still not updated.
	 */
	private Timer completionUpdateTimer;

	/**
	 * Indicates if a command progress report is in progress. Used to track
	 * start and finish of the commands.
	 */
	private boolean isInforming;

	/**
	 * Reports the time to the current device and UI if set.
	 * <p>
	 * Perform the appropriate thread protection.
	 * 
	 * @param kind
	 *            the kind of report
	 * @param duration
	 *            the duration
	 */
	private void reportTime(final DeviceUI.ReportKind kind, final int duration) {
		if (compositeUnderDisplay != null
				&& !compositeUnderDisplay.isDisposed()) {
			final Display display = top.getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(new Runnable() {
					public void run() {
						/*
						 * since this method is run asynchronously, check that
						 * the composite was not disposed in the meanwhile
						 */
						if (!compositeUnderDisplay.isDisposed()) {
							final DeviceUI ui = (DeviceUI) compositeUnderDisplay;
							ui.updateTimers(deviceUnderDisplay, kind, duration);
						}
					}
				});
			}
		}
	}

	/**
	 * Sets the timer update task.
	 */
	private void scheduleUpdateTimer() {
		assert completionUpdateTimer == null : "Timer object was created";

		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (deviceUnderDisplay != null) {
					final int timeToFinish = deviceUnderDisplay.getDriver()
							.getTimeToCompletion();
					if (timeToFinish > 0) {
						if (isInforming) {
							/*
							 * Inform elapsed time
							 */
							reportTime(DeviceUI.ReportKind.FINISHING,
									timeToFinish);
						} else {
							/*
							 * We are starting.
							 */
							reportTime(DeviceUI.ReportKind.STARTING,
									timeToFinish);
							isInforming = true;
						}
					} else {
						if (isInforming) {
							/*
							 * The task has just finished
							 */
							reportTime(DeviceUI.ReportKind.FINISHING, 0);
							isInforming = false;
						}
					}
				}
			}
		};

		completionUpdateTimer.scheduleAtFixedRate(timerTask, 0,
				UPDATE_TIMER_REFRESH_RATE);
	}

	/**
	 * Starts the timer and schedules the update task.
	 */
	private void startUpdateTimer() {
		if (completionUpdateTimer == null) {
			completionUpdateTimer = new Timer(UPDATE_TIMER_NAME);

			scheduleUpdateTimer();
		}
	}

	/**
	 * Stops the update timer.
	 */
	private void stopUpdateTimer() {
		if (completionUpdateTimer != null) {
			completionUpdateTimer.cancel();
			completionUpdateTimer = null;
		}
	}

	/**
	 * Finds the first control panel on an area.
	 * 
	 * @param area
	 *            the area with a control panel
	 * @return the first control panel device found or <code>null</code> if no
	 *         control panel exists.
	 */
	private Device findFirstControlPanel(final Area area) {
		for (Device d : area.getDevices()) {
			if (ModelUtils.isControlPanel(d)) {
				return d;
			}
		}

		return null;
	}

	/**
	 * Finds the network interface of a device or area.
	 * 
	 * @param item
	 *            the item
	 * @return the interface in use of <code>null</code> if none is found
	 */
	private IGateway findAreaOrDeviceInterface(final ModelItem item) {
		if (item != null) {
			final Area a = Queries.getAncestorArea(item);
			if (a != null) {
				final IGateway ni = a.getAreaInterface();
				return ni;
			}
		}
		return null;
	}

	/**
	 * Method that returns the controls that can be used to interact with the
	 * device.
	 * 
	 * @param device
	 *            the device to get the controls
	 * @return composite of the devices controls
	 */
	private Composite createDeviceControls(final Device device) {
		if (device == null || device.getDeviceType() == null) {
			return top;
		}

		top.setLayout(new GridLayout());

		Composite mainPartControl = new DevicePanelDecorator(device).getPanel(
				top, SWT.NONE, device);
		if (mainPartControl == null) {
			mainPartControl = new Composite(top, SWT.NONE);
			ViewUtils.createLabelPane(mainPartControl, "No control defined");
		} else {
			final GridData centering = new GridData(SWT.FILL, SWT.FILL, true,
					true);
			mainPartControl.setLayoutData(centering);

			((DeviceUI) mainPartControl).updateControls(device);
			mainPartControl.getShell().layout(false, true);
			((DeviceUI) mainPartControl).finishLayout();
		}

		return mainPartControl;
	}

	private void setDeviceUnderDisplay(final Device d) {
		if (d == null) {
			ViewUtils.disposeExistingControls(top);
			removeWarningPane();
		} else {
			compositeUnderDisplay = createDeviceControls(d);
			doShowWarningPaneIfNeeded(d, null, false);
		}

		deviceUnderDisplay = d;
		isInforming = false;
	}

	/**
	 * Checks is the warning pane must be displayed and if do displays it.
	 * 
	 * @param device
	 *            the current item being displayed
	 * @param scheduler
	 *            the scheduler that sent a notification, can be
	 *            <code>null</code>
	 */
	private void doShowWarningPaneIfNeeded(final Device device,
			final MessageScheduler scheduler, final boolean busy) {
		final IGateway ni = findAreaOrDeviceInterface(device);

		if (ni != null) {
			if (device.getConnectivity().isOffline()) {
				showWarningPane(DeviceConnectivityProblem
						.getOfflineProblemDetails(device.getConnectivity()
								.getTimestamp()));
				return;
			} else if (busy && scheduler != null
					&& scheduler.getInterface() == ni) {
				showWarningPane(BUSY_PANEL_MESSAGE);
				return;
			} else {
				final IGateway.GatewayConnectionStatus ns = ni
						.getConnectionStatus();
				if (IGateway.GatewayConnectionStatus.isNoGatewayConnection(ns)) {
					showWarningPane(WARNING_PANEL_MESSAGE);
					return;
				}
			}
		}

		/*
		 * Remove if show did not apply!
		 */
		// removeWarningPane();
	}

	private void showWarningPane(final String message) {
		if (top != null && !top.isDisposed()) {
			final Display display = top.getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(new Runnable() {
					public void run() {
						// // XXX: This code is like the code
						// InterfaceStatusContribution
						// // we should refactor this
						if (labelWarning == null || labelWarning.isDisposed()
								&& !top.isDisposed()) {
							labelWarning = new CLabel(top, SWT.CENTER);

							labelWarning.setForeground(display
									.getSystemColor(UIConstants.WARNING_PANEL_FOREGROUND_COLOR));

							if (compositeUnderDisplay != null
									&& !compositeUnderDisplay.isDisposed()) {
								labelWarning.moveAbove(compositeUnderDisplay);
							}
						}

						final Image warningImage = lumina.ui.swt.ApplicationImageCache
								.getInstance().getImage(
										UIConstants.WARNING_PANEL_IMAGE_PATH);

						/*
						 * We were having problem with "widget is disposed" when
						 * exiting the application. This fixes the problem (sort
						 * of).
						 */
						if (!labelWarning.isDisposed()) {
							labelWarning.setText(message);
							labelWarning.setImage(warningImage);

							labelWarning.setBackground(display
									.getSystemColor(UIConstants.WARNING_PANEL_BACKGROUND_COLOR));

							final GridData labelGd = new GridData(SWT.FILL,
									SWT.CENTER | SWT.FILL, true, true);

							// FIXME: Temporary fix, what happens if the font
							// changes?
							// CHECKSTYLE:OFF
							labelGd.minimumHeight = 32;
							// CHECKSTYLE:ON

							labelWarning.setLayoutData(labelGd);
							labelWarning.pack();
						}
						if (!top.isDisposed()) {
							top.layout(true, false);
						}
					}
				});
			}
		}
	}

	private void removeWarningPane() {
		if (top != null && !top.isDisposed()) {
			/*
			 * thread protection.
			 */
			final Display display = top.getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(new Runnable() {
					public void run() {
						if (labelWarning != null) {
							labelWarning.dispose();
							labelWarning = null;

							/*
							 * /!\ we're on another thread here, top may have
							 * been disposed.
							 */
							if (!top.isDisposed()) {
								top.setEnabled(true);
								top.layout(true, false);
							}
						}
					}
				});
			}
		}
	}

	/**
	 * Listener than updates the panels according to the network status.
	 */
	private final IGateway.ConnectionStatusListener connectionStatusListener = new IGateway.ConnectionStatusListener() {
		/**
		 * Network message handling for network close. Remove warning pane.
		 * 
		 * @param networkInterface
		 *            network interface
		 */
		public final void gatewayConnectionClosed(IGateway networkInterface) {
			ControlView.this.removeWarningPane();
		}

		/**
		 * Network message handling for port absent. Shows warning pane if
		 * required.
		 * 
		 * @param networkInterface
		 *            network interface
		 */
		public final void transportPortAbsent(IGateway networkInterface) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					null, false);
		}

		/**
		 * Network message handling for connection lost. Shows warning pane if
		 * required.
		 * 
		 * @param networkInterface
		 *            network interface
		 */
		public final void gatewayConnectionLost(IGateway networkInterface) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					null, false);
		}

		/**
		 * Network message handling for network found. Remove warning pane.
		 * 
		 * @param networkInterface
		 *            network interface
		 */
		public final void gatewayFound(IGateway networkInterface) {
			ControlView.this.removeWarningPane();
		}

		public final void gatewaySearching(IGateway networkInterface) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					null, false);
		}
	};

	/**
	 * Listener than updates the panels according to the message dispatcher
	 * status.
	 */
	private final MessageScheduler.DispatcherStatusListener dispatcherListener = new MessageScheduler.DispatcherStatusListener() {
		/**
		 * Dispatcher message handling for busy dispatcher. Shows warning pane
		 * if required.
		 * 
		 * @param scheduler
		 *            message scheduler
		 */
		public final void dispatcherBusy(MessageScheduler scheduler) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					scheduler, true);
		}

		/**
		 * Dispatcher message handling for normal dispatcher. Shows warning pane
		 * if required.
		 * 
		 * @param scheduler
		 *            message scheduler
		 */
		public final void dispatcherNormal(MessageScheduler scheduler) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					scheduler, false);
		}

		/**
		 * Dispatcher message handling for stopped dispatcher. Shows warning
		 * pane if required.
		 * 
		 * @param scheduler
		 *            message scheduler
		 */
		public final void dispatcherStopped(MessageScheduler scheduler) {
			ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
					scheduler, false);
		}
	};

	/**
	 * Updates the pane when the device goes online/offline.
	 */
	private final ProjectModel.DeviceConnectivityChangeListener connectivityChangeListener = new ProjectModel.DeviceConnectivityChangeListener() {
		/**
		 * Message handling for device connectivity change.
		 * <p>
		 * Pane will be shown or hidden upon connectivity status.
		 * 
		 * @param device
		 *            device
		 * @param status
		 *            connectivity status
		 */
		public final void deviceConnectivityChanged(Device device,
				TimestampedConnectivityStatus status) {
			if (status.isOffline()) {
				ControlView.this.doShowWarningPaneIfNeeded(deviceUnderDisplay,
						null, true);
			}

			if (status.isOnline()) {
				ControlView.this.removeWarningPane();
			}
		}
	};

	/**
	 * Call-back that will allow to create the viewer and initialize it.
	 * 
	 * @param parent
	 *            control
	 */
	public final void createPartControl(final Composite parent) {
		top = parent;
		/* Create a label for the view to indicate no item is selected */
		ViewUtils.createLabelPane(top, NO_VALID_SELECTION);

		/* register the property change listeners of the model */
		ProjectModel.getInstance().addPropertyChangeListener(this);

		// ///?????
		startUpdateTimer();
	}

	/**
	 * Terminate.
	 * <p>
	 * Stops the update timer.
	 */
	@Override
	public final void dispose() {
		stopUpdateTimer();
		super.dispose();
	}

	/**
	 * Updates the displayed controls when another device is selected.
	 * 
	 * @param part
	 *            workbench part
	 * @param incoming
	 *            selection
	 */
	public final void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		/*
		 * Try to avoid selectionCache because the user will see the control
		 * view flickering
		 */
		if (incoming instanceof IStructuredSelection
				&& !incoming.equals(selectionCache)) {
			selectionCache = (IStructuredSelection) incoming;
			final boolean isMultipleSelection = selectionCache.size() > 1;

			setDeviceUnderDisplay(null);

			if (isMultipleSelection) {
				ViewUtils.createLabelPane(top, MULTIPLE_ITEMS_SELECTED);
			} else {
				final Object obj = selectionCache.getFirstElement();

				Device selectedDevice = null;
				if (obj != null && ModelUtils.isModelItem(obj)) {
					if (ModelUtils.isDevice(obj)) {
						selectedDevice = (Device) obj;
					} else if (ModelUtils.isArea(obj)) {
						selectedDevice = findFirstControlPanel((Area) obj);
					}
				}

				if (selectedDevice == null) {
					/*
					 * if occurred a selectionCache of a non object area
					 */
					ViewUtils.createLabelPane(top, NO_VALID_SELECTION);
				} else {
					setDeviceUnderDisplay(selectedDevice);
				}
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

	/**
	 * Property change.
	 * <p>
	 * Refreshes the views when a change occurs.
	 * 
	 * @param event
	 *            property change event
	 */
	public final void propertyChange(final PropertyChangeEvent event) {
		final Object subject = event.getSource();
		final String changedProperty = event.getProperty();

		if (PropertyChangeNames.isStatusChange(changedProperty)) {
			assert ModelUtils.isModelItem(subject);

			if (subject == deviceUnderDisplay) {
				// final DeviceStatus status = (DeviceStatus)
				// event.getNewValue();
				if (compositeUnderDisplay instanceof DeviceUI
						&& !compositeUnderDisplay.isDisposed()) {
					((DeviceUI) compositeUnderDisplay)
							.updateControls((Device) subject);
				}
			}
		} else if (PropertyChangeNames.isNodeRemove(changedProperty)) {
			ViewUtils.disposeExistingControls(top);
			ViewUtils.createLabelPane(top, NO_VALID_SELECTION);
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

					/*
					 * Set the dispatcher status listener to show warnings when
					 * messages are not being delivered.
					 */
					manager.addDispatcherListener(dispatcherListener);
				}

				ProjectModel.getInstance().addDeviceConnectivityListener(
						connectivityChangeListener);
			}

			setDeviceUnderDisplay(null);
			ViewUtils.createLabelPane(top, NO_VALID_SELECTION);
		}
	}

}
