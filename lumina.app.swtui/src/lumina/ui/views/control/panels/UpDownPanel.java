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
 * A panel with UP, DOWN and STOP buttons.
 * <p>
 * Depending on the information provided by the device driver the <b>stop</b>
 * button may not be rendered.
 * <p>
 * This class requires a device with a {@link BidirectionalStatus}.
 */
// FIXME: The listeners should be inner classes
public class UpDownPanel extends AbstractDevicePanel {

	private static final String UP_HINT = Messages
			.getString("UpDownPanel.upHint"); //$NON-NLS-1$
	private static final String DOWN_HINT = Messages
			.getString("UpDownPanel.downHint"); //$NON-NLS-1$
	private static final String STOP_HINT = Messages
			.getString("UpDownPanel.stopHint"); //$NON-NLS-1$
	private static final String UP_IMAGE_PATH = "/icons/devices/arrow_up.png"; //$NON-NLS-1$
	private static final String DOWN_IMAGE_PATH = "/icons/devices/arrow_down.png"; //$NON-NLS-1$

	private final Device deviceUnderDisplay;

	private final BidirectionalStatus upDownStatus;

	private final Label deviceNameLabel;
	private final Composite centeringComposite;

	private Composite infoHeader;

	private Button upButton;
	private Button stopButton;
	private Button downButton;

	private Composite buttonPanel;

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
		 * Used to handle the change of static information about the device.
		 * 
		 * @param event
		 *            the property change event
		 */
		@Override
		public final void propertyChange(final PropertyChangeEvent event) {
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
	 * @param style
	 */
	// CHECKSTYLE:OFF
	public UpDownPanel(final Composite parent, final int style,
			final Device device) {
		super(parent, style, device);
		setLayout(new GridLayout());

		deviceUnderDisplay = device;

		final DeviceStatus status = device.getStatus();
		if (!(status instanceof BidirectionalStatus)) {
			throw new IllegalArgumentException("Wrong kind of status"); //$NON-NLS-1$
		}
		upDownStatus = (BidirectionalStatus) status;

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
		infoHeader.setFont(SWTResourceManager.getFont("", 8, SWT.NONE)); //$NON-NLS-1$
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

		// up button
		upButton = new Button(buttonPanel, SWT.TOGGLE);
		final GridData gdUpButton = new GridData(SWT.CENTER, SWT.CENTER, true,
				true);
		gdUpButton.minimumHeight = 30;
		gdUpButton.minimumWidth = 60;
		upButton.setLayoutData(gdUpButton);
		upButton.setImage(ApplicationImageCache.getInstance().getImage(
				UP_IMAGE_PATH));
		upButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				upDownStatus.setDirection(true, true);
				upDownStatus.setStopped(false, true);
				updateButtons(upDownStatus);
			}
		});
		upButton.setToolTipText(UP_HINT);

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
				upDownStatus.setStopped(true, true);
				updateButtons(upDownStatus);
			}
		});
		stopButton.setToolTipText(STOP_HINT);

		// down button
		downButton = new Button(buttonPanel, SWT.TOGGLE);
		final GridData gdDownButton = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		gdDownButton.verticalIndent = 5;
		gdDownButton.minimumHeight = 30;
		gdDownButton.minimumWidth = 60;
		downButton.setLayoutData(gdDownButton);
		downButton.setImage(ApplicationImageCache.getInstance().getImage(
				DOWN_IMAGE_PATH));
		downButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				upDownStatus.setDirection(false, true);
				upDownStatus.setStopped(false, true);
				updateButtons(upDownStatus);
			}
		});
		downButton.setToolTipText(DOWN_HINT);

		new Label(centeringComposite, SWT.NONE);

		updateMetadataInfo();

		ProjectModel.getInstance().addPropertyChangeListener(
				propertyChangeListener);
	}

	// CHECKSTYLE:ON

	/**
	 * Removes this panel from beeing a property listener.
	 */
	public final void dispose() {
		// FIXME: Should we dispose all children?
		ProjectModel.getInstance().removePropertyChangeListener(
				propertyChangeListener);

		super.dispose();
	}

	@Override
	public final void finishLayout() {
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
	 * Activates the button corresponding to the state.
	 * 
	 * @param status
	 *            the device status
	 */
	private void updateButtons(final BidirectionalStatus status) {
		/*
		 * deactivate all buttons
		 */
		upButton.setSelection(false);
		if (stopButton != null && !stopButton.isDisposed()) {
			stopButton.setSelection(false);
		}
		downButton.setSelection(false);

		/*
		 * activate the correct button
		 */
		final IDeviceDriver driver = deviceUnderDisplay.getDriver();
		if (driver instanceof DirectionDeviceDriver) {
			if (!hasStop()) {
				if (status.getDirection()) {
					upButton.setSelection(true);
				} else {
					downButton.setSelection(true);
				}
			} else {
				if (status.isStopped()) {
					stopButton.setSelection(true);
				} else {
					if (status.getDirection()) {
						upButton.setSelection(true);
					} else {
						downButton.setSelection(true);
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
				deviceNameLabel.setText(""); //$NON-NLS-1$
			} else {
				deviceNameLabel.setText(deviceName);
			}

			if (infoHeader != null && !infoHeader.isDisposed()) {
				infoHeader.layout();
			}

		}

		updateButtons(upDownStatus);
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
	public final void updateControls(final Device device) {
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
