package lumina.ui.views.control.panels;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.IDeviceDriver;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.devices.status.BidirectionalStatus;
import lumina.bundles.drivers.ilight.DirectionDeviceDriver;
import lumina.ui.swt.ApplicationImageCache;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.swtdesigner.SWTResourceManager;

/**
 * A panel with OPEN, CLOSE and STOP buttons.
 * <p>
 * Depending on the information provided by the device driver the <b>stop</b>
 * button may not be rendered.
 * <p>
 * This class requires a device with a {@link BidirectionalStatus}.
 */
public class OpenClosePanel extends AbstractDevicePanel {

	private static final String EMPTY_STRING = "";

	private static final String OPEN_HINT = Messages
			.getString("OpenClosePanel.openHint"); //$NON-NLS-1$
	private static final String CLOSE_HINT = Messages
			.getString("OpenClosePanel.closeHint"); //$NON-NLS-1$
	private static final String STOP_HINT = Messages
			.getString("OpenClosePanel.stopHint"); //$NON-NLS-1$

	private final Device deviceUnderDisplay;

	private final BidirectionalStatus shutterStatus;

	private final Label deviceNameLabel;
	private final Composite centeringComposite;

	private Composite infoHeader;

	private Button openButton;
	private Button stopButton;
	private Button closeButton;

	private Composite buttonPanel;

	private static final String OPEN_IMAGE_PATH = "/icons/devices/open.png"; //$NON-NLS-1$
	private static final String CLOSE_IMAGE_PATH = "/icons/devices/close.png"; //$NON-NLS-1$

	private boolean hasStop() {
		final IDeviceDriver driver = deviceUnderDisplay.getDriver();
		if (driver instanceof DirectionDeviceDriver) {
			final DirectionDeviceDriver s = (DirectionDeviceDriver) driver;

			return s.getSignalConfig().hasStop();
		}
		return false;
	}

	/**
	 * Listener that receives the Property change events from the model and
	 * updates the widgets appropriately.
	 */
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		/**
		 * Property change listener handler.
		 * <p>
		 * Used to handle the change of the status information and static
		 * information about the device updating the panel and the widgets
		 * accordingly.
		 * 
		 * @param event
		 *            the property change event
		 */
		public void propertyChange(final PropertyChangeEvent event) {
			final Object subject = event.getSource();
			final String changedProperty = event.getProperty();

			if (!PropertyChangeNames.isStatusChange(changedProperty)) {
				assert ModelUtils.isModelItem(subject);

				if (subject == deviceUnderDisplay) {
					updateMetadataInfo();
					updateControlsInternal();
				}
			}
		}
	};

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 *            the parent panel
	 * @param style
	 *            the SWT style
	 */
	// CHECKSTYLE:OFF
	public OpenClosePanel(final Composite parent, final int style,
			final Device device) {
		super(parent, style, device);
		setLayout(new GridLayout());

		deviceUnderDisplay = device;

		final DeviceStatus status = device.getStatus();
		if (!(status instanceof BidirectionalStatus)) {
			throw new IllegalArgumentException("Wrong kind of status"); //$NON-NLS-1$
		}
		shutterStatus = (BidirectionalStatus) status;

		centeringComposite = new Composite(this, SWT.NONE);
		final GridData gdCenteringComposite = new GridData(SWT.FILL,
				SWT.CENTER, true, true);
		centeringComposite.setLayoutData(gdCenteringComposite);
		final GridLayout gridLayout1 = new GridLayout();
		gridLayout1.horizontalSpacing = 10;
		gridLayout1.verticalSpacing = 0;
		centeringComposite.setLayout(gridLayout1);

		infoHeader = new Composite(centeringComposite, SWT.NONE);
		infoHeader.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
		infoHeader.setFont(SWTResourceManager
				.getFont(EMPTY_STRING, 8, SWT.NONE)); //$NON-NLS-1$
		final GridLayout gridLayout = new GridLayout();
		infoHeader.setLayout(gridLayout);

		deviceNameLabel = new Label(infoHeader, SWT.NONE);
		final GridData gdDeviceNameLabel = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		deviceNameLabel.setLayoutData(gdDeviceNameLabel);
		deviceNameLabel.setText(Messages.getString("LampPanel.deviceName")); //$NON-NLS-1$
		deviceNameLabel.setFont(UIConstants.getTitleFont());
		deviceNameLabel.setForeground(UIConstants.getTitleColor());

		buttonPanel = new Composite(centeringComposite, SWT.NONE);
		final GridData gdComposite = new GridData(SWT.CENTER, SWT.CENTER, true,
				true);
		gdComposite.verticalIndent = 15;
		buttonPanel.setLayoutData(gdComposite);
		final GridLayout gridLayout2 = new GridLayout();
		buttonPanel.setLayout(gridLayout2);

		// open button
		openButton = new Button(buttonPanel, SWT.TOGGLE);
		final GridData gdUpButton = new GridData(SWT.CENTER, SWT.CENTER, true,
				true);
		gdUpButton.minimumHeight = 30;
		gdUpButton.minimumWidth = 60;
		openButton.setLayoutData(gdUpButton);
		openButton.setImage(ApplicationImageCache.getInstance().getImage(
				OPEN_IMAGE_PATH));
		openButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shutterStatus.setDirection(true, true);
				shutterStatus.setStopped(false, true);
				updateButtons(shutterStatus);
			}
		});
		openButton.setToolTipText(OPEN_HINT);

		// stop button
		stopButton = new Button(buttonPanel, SWT.TOGGLE);
		final GridData gdStopButton = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		gdStopButton.verticalIndent = 5;
		gdStopButton.minimumHeight = 30;
		gdStopButton.minimumWidth = 60;
		stopButton.setLayoutData(gdStopButton);
		stopButton.setText("STOP");
		stopButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shutterStatus.setStopped(true, true);
				updateButtons(shutterStatus);
			}
		});
		stopButton.setToolTipText(STOP_HINT);

		// close button
		closeButton = new Button(buttonPanel, SWT.TOGGLE);
		final GridData gdDownButton = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		gdDownButton.verticalIndent = 5;
		gdDownButton.minimumHeight = 30;
		gdDownButton.minimumWidth = 60;
		closeButton.setLayoutData(gdDownButton);
		closeButton.setImage(ApplicationImageCache.getInstance().getImage(
				CLOSE_IMAGE_PATH));
		closeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shutterStatus.setDirection(false, true);
				shutterStatus.setStopped(false, true);
				updateButtons(shutterStatus);
			}
		});
		closeButton.setToolTipText(CLOSE_HINT);

		new Label(centeringComposite, SWT.NONE);

		updateMetadataInfo();

		ProjectModel.getInstance().addPropertyChangeListener(
				propertyChangeListener);
	}

	// CHECKSTYLE:ON

	public void dispose() {
		ProjectModel.getInstance().removePropertyChangeListener(
				propertyChangeListener);

		super.dispose();
	}

	@Override
	public void finishLayout() {
		super.finishLayout();
		// putImage(CLOSE_BLIND, intensityDownImage);
		// putImage(OPEN_BLIND, intensityUpImage);
		// putImage(CLOSE_BLIND, intensityDownImage);
	}

	private void updateMetadataInfo() {
		if (deviceNameLabel != null && !deviceNameLabel.isDisposed()) {
			deviceNameLabel.setText(deviceUnderDisplay.getName());

			if (centeringComposite != null && !centeringComposite.isDisposed()) {
				infoHeader.pack();
				centeringComposite.layout();
			}
		}

		if (stopButton != null && !stopButton.isDisposed()) {
			final boolean canShowStop = hasStop();
			stopButton.setVisible(canShowStop);
		}
	}

	/**
	 * Activates the button corresponding to the status.
	 * 
	 * @param status
	 *            the device status
	 */
	private void updateButtons(final BidirectionalStatus status) {
		/*
		 * deactivate all buttons
		 */
		openButton.setSelection(false);
		if (stopButton != null && !stopButton.isDisposed()) {
			stopButton.setSelection(false);
		}
		closeButton.setSelection(false);

		/*
		 * activate the correct button
		 */
		final IDeviceDriver driver = deviceUnderDisplay.getDriver();
		if (driver instanceof DirectionDeviceDriver) {
			if (!hasStop()) {
				if (status.getDirection()) {
					openButton.setSelection(true);
				} else {
					closeButton.setSelection(true);
				}
			} else {
				if (status.isStopped()) {
					stopButton.setSelection(true);
				} else {
					if (status.getDirection()) {
						openButton.setSelection(true);
					} else {
						closeButton.setSelection(true);
					}
				}
			}
		}

	}

	/**
	 * Updates the controls of the control panel.
	 */
	private void updateControlsInternal() {
		if (deviceNameLabel != null && !deviceNameLabel.isDisposed()) {
			final String deviceName = deviceUnderDisplay.getName();
			if (deviceName == null) {
				deviceNameLabel.setText(EMPTY_STRING); //$NON-NLS-1$
			} else {
				deviceNameLabel.setText(deviceName);
			}

			if (infoHeader != null && !infoHeader.isDisposed()) {
				infoHeader.layout();
			}

		}

		updateButtons(shutterStatus);
	}

	/**
	 * Updates the controls according to the given status.
	 * <p>
	 * Called by the {@link lumina.ui.views.control.ControlView} to respond to
	 * property changes.
	 * 
	 * @param device
	 *            the new supplied status
	 */
	public void updateControls(final Device device) {
		final DeviceStatus status = device.getStatus();
		if (status instanceof BidirectionalStatus) {
			final BidirectionalStatus s = (BidirectionalStatus) status;
			updateButtons(s);
		}
	}

	@Override
	protected void updateFade(final boolean visible, final int duration) {
	}
}
