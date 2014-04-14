package lumina.ui.views.control.panels;

import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.devices.status.OnOffIntensityStatus;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import com.swtdesigner.SWTResourceManager;

/**
 * A panel with intensity scale, ON and OFF buttons.
 * <p>
 * This class requires a device with a {@link OnOffIntensityStatus}.
 */
// FIXME: The listeners should be inner classes
public class OnOffIntensityPanel extends AbstractDevicePanel {

	// FIXME: Pannel graphics should be improved
	//    private static final String POWERON_IMAGE = "/icons/devices/power_on.png"; //$NON-NLS-1$
	//    private static final String POWEROFF_IMAGE = "/icons/devices/power_off.png"; //$NON-NLS-1$
	//    private static final String INCREASE_BRIGHTNESS = "/icons/devices/brightness_increase.png"; //$NON-NLS-1$
	//    private static final String DECREASE_BRIGHTNESS = "/icons/devices/brightness_decrease.png"; //$NON-NLS-1$

	/**
	 * Largest message of the intensity display label. Used to compute the
	 * appropriate widget size.
	 */
	private static final String INTENSITY_DISPLAY_LARGEST = Messages
			.getString("LampPanel.100Percent"); //$NON-NLS-1$

	/**
	 * Tooltip message of the on button.
	 */
	private static final String ON_HINT = Messages
			.getString("LampPanel.onHint"); //$NON-NLS-1$

	/**
	 * Tooltip message of the off button.
	 */
	private static final String OFF_HINT = Messages
			.getString("LampPanel.offHint"); //$NON-NLS-1$

	/**
	 * Tooltip message of the intensity gauge.
	 */
	private static final String INTENSITY_HINT = Messages
			.getString("LampPanel.intensityHint"); //$NON-NLS-1$

	/**
	 * Tooltip message of the intensity display label.
	 */
	private static final String CURRENT_INTENSITY_HINT = Messages
			.getString("LampPanel.currentIntensityHint"); //$NON-NLS-1$

	/**
	 * Label for the ON button.
	 */
	private static final String BUTTON_ON_LABEL = Messages
			.getString("LampPanel.buttonOn"); //$NON-NLS-1$

	/**
	 * Label for the OFF button.
	 */
	private static final String BUTTON_OFF_LABEL = Messages
			.getString("LampPanel.buttonOff"); //$NON-NLS-1$

	private static final String AUTO_OFF_MESSAGE = Messages
			.getString("LampPanel.fade"); //$NON-NLS-1$

	private final OnOffIntensityStatus lampStatus;

	private final Composite centeringComposite;

	private final Scale intensityScale;

	private final Label intensityLevel;

	private final Label deviceNameLabel;

	private final FadeTimerLabel timeLeft;

	private final Button onButton;

	private final Button offButton;

	private Canvas intensityDownImage;

	private Canvas intensityUpImage;

	private MouseListener intensityStatusMouseListener;

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
		 *            the change event
		 */
		public void propertyChange(final PropertyChangeEvent event) {
			final Object subject = event.getSource();
			final String changedProperty = event.getProperty();

			if (!PropertyChangeNames.isStatusChange(changedProperty)) {
				assert ModelUtils.isModelItem(subject);

				if (subject == deviceUnderDisplay) {
					updateMetadataInfo();
				}
			}
		}
	};

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the SWT style
	 * @param device
	 *            the device the device to be controlled
	 */
	// CHECKSTYLE:OFF
	public OnOffIntensityPanel(final Composite parent, final int style,
			final Device device) {
		super(parent, style, device);
		setLayout(new GridLayout());

		if (!(device.getStatus() instanceof OnOffIntensityStatus)) {
			throw new IllegalArgumentException(
					"Wrong kind of status for lamp UI"); //$NON-NLS-1$
		}
		lampStatus = (OnOffIntensityStatus) device.getStatus();

		centeringComposite = new Composite(this, SWT.NONE);
		final GridData gdCenteringComposite = new GridData(SWT.CENTER,
				SWT.CENTER, true, true);
		centeringComposite.setLayoutData(gdCenteringComposite);
		final GridLayout gridLayout1 = new GridLayout();
		gridLayout1.horizontalSpacing = 10;
		gridLayout1.verticalSpacing = 0;
		gridLayout1.numColumns = 2;
		centeringComposite.setLayout(gridLayout1);

		final Composite infoHeader = new Composite(centeringComposite, SWT.NONE);
		infoHeader.setFont(SWTResourceManager.getFont("", 8, SWT.NONE)); //$NON-NLS-1$
		final GridLayout gridLayout = new GridLayout();
		infoHeader.setLayout(gridLayout);
		final GridData gdInfoHeader = new GridData(SWT.CENTER, SWT.CENTER,
				true, false, 2, 1);
		infoHeader.setLayoutData(gdInfoHeader);
		new Label(centeringComposite, SWT.NONE);

		deviceNameLabel = new Label(infoHeader, SWT.NONE);
		final GridData gdDeviceNameLabel = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		deviceNameLabel.setLayoutData(gdDeviceNameLabel);
		deviceNameLabel.setFont(UIConstants.getTitleFont());
		deviceNameLabel.setForeground(UIConstants.getTitleColor());
		deviceNameLabel.setText("Device Name"); //$NON-NLS-1$

		intensityUpImage = new Canvas(centeringComposite, SWT.NONE);
		intensityUpImage.setDragDetect(false);
		final GridData gdIntensityUpImage = new GridData(SWT.CENTER,
				SWT.BOTTOM, false, false);
		gdIntensityUpImage.widthHint = 22;
		gdIntensityUpImage.heightHint = 22;
		intensityUpImage.setLayoutData(gdIntensityUpImage);

		final Composite composite = new Composite(centeringComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		composite.setLayout(new GridLayout());

		onButton = new Button(composite, SWT.PUSH);
		final GridData gdOnButton = new GridData(SWT.FILL, SWT.CENTER, true,
				true);
		gdOnButton.verticalIndent = 1;
		gdOnButton.minimumHeight = 38;
		onButton.setLayoutData(gdOnButton);
		onButton.setText(BUTTON_ON_LABEL);
		onButton.addListener(SWT.Selection, new Listener() {

			/**
			 * Handle pressing the <tt>on</tt> button.
			 * <p>
			 * Turns the circuit on.
			 * 
			 * @param event
			 *            the event.
			 */
			public void handleEvent(Event event) {
				lampStatus.switchOn(true, true);
			}
		});
		onButton.setToolTipText(ON_HINT);

		offButton = new Button(composite, SWT.PUSH);
		final GridData gdOffButton = new GridData(SWT.FILL, SWT.CENTER, true,
				true);
		gdOffButton.minimumWidth = 60;
		gdOffButton.verticalIndent = 4;
		gdOffButton.minimumHeight = 38;
		offButton.setLayoutData(gdOffButton);
		offButton.setText(BUTTON_OFF_LABEL);
		offButton.addListener(SWT.Selection, new Listener() {
			/**
			 * Handle pressing the <tt>off</tt> button.
			 * <p>
			 * Turns the circuit off.
			 * 
			 * @param event
			 *            the event.
			 */
			public void handleEvent(final Event event) {
				lampStatus.switchOn(false, true);
			}

		});
		offButton.setToolTipText(OFF_HINT);

		// Intensity Scale
		intensityScale = new Scale(centeringComposite, SWT.VERTICAL);
		final GridData gdIntensityScale = new GridData(SWT.CENTER, SWT.CENTER,
				false, false);
		gdIntensityScale.heightHint = 100;
		gdIntensityScale.minimumHeight = 100;
		intensityScale.setLayoutData(gdIntensityScale);
		intensityScale.addListener(SWT.Selection, new Listener() {
			/**
			 * Updates the intensity status of the circuit.
			 * 
			 * @param event
			 *            the widget event
			 */
			public void handleEvent(final Event event) {
				/*
				 * We are conservative here.
				 * 
				 * We do not update lampStatus.setIntensity(intensity) here in
				 * order to to flood the the network.
				 */
				setIntensityInternal(getIntensityInternal());

				if (lampStatus != null) {
					lampStatus.setIntensity(getIntensityInternal(), true);
				}
			}
		});
		intensityScale.setSelection(100);
		intensityScale.setToolTipText(INTENSITY_HINT);

		/*
		 * AUTO OFF stuff:
		 */
		if (true) {
			final Label autoOffLabel = new Label(centeringComposite, SWT.CENTER);
			autoOffLabel.setFont(SWTResourceManager.getFont(
					"Arial", 7, SWT.NONE)); //$NON-NLS-1$
			final GridData gdAutoOffLabel = new GridData(SWT.CENTER,
					SWT.BOTTOM, false, false);
			gdAutoOffLabel.verticalIndent = -5;
			autoOffLabel.setLayoutData(gdAutoOffLabel);
			autoOffLabel.setText(AUTO_OFF_MESSAGE); //$NON-NLS-1$
			autoOffLabel.setVisible(false);
		}

		intensityDownImage = new Canvas(centeringComposite, SWT.NONE);
		intensityDownImage.setBackgroundMode(SWT.INHERIT_FORCE);
		intensityDownImage.setForeground(SWTResourceManager.getColor(255, 255,
				255));

		final GridData gdIntensityDownImage = new GridData(SWT.CENTER, SWT.TOP,
				false, false);
		gdIntensityDownImage.widthHint = 22;
		gdIntensityDownImage.heightHint = 22;
		intensityDownImage.setLayoutData(gdIntensityDownImage);

		if (true) {
			timeLeft = new FadeTimerLabel(centeringComposite, SWT.CENTER);
			final GridData gdTimeLeft = new GridData(SWT.CENTER, SWT.CENTER,
					false, false);
			timeLeft.setLayoutData(gdTimeLeft);
			timeLeft.setText("--:--:--"); // $NON-NLS-1$
			timeLeft.setVisible(false);
		}

		intensityLevel = new Label(centeringComposite, SWT.CENTER);
		final GridData gdIntensityLevel = new GridData(SWT.CENTER, SWT.CENTER,
				true, false);
		gdIntensityLevel.minimumWidth = 50;
		intensityLevel.setLayoutData(gdIntensityLevel);
		intensityLevel.setText(INTENSITY_DISPLAY_LARGEST);
		intensityLevel.setToolTipText(CURRENT_INTENSITY_HINT);

		updateMetadataInfo();

		intensityStatusMouseListener = new MouseListener() {
			private boolean mouseDown;

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				mouseDown = true;
			}

			public void mouseUp(MouseEvent e) {
				if (mouseDown) {
					mouseDown = false;
					if (lampStatus != null) {
						lampStatus.setIntensity(getIntensityInternal(), true);
					}
				}
			}
		};

		intensityScale.addMouseListener(intensityStatusMouseListener);
		ProjectModel.getInstance().addPropertyChangeListener(
				propertyChangeListener);
	}

	// CHECKSTYLE:ON

	public void dispose() {
		intensityScale.removeMouseListener(intensityStatusMouseListener);
		ProjectModel.getInstance().removePropertyChangeListener(
				propertyChangeListener);
		super.dispose();
	}

	public void finishLayout() {
		super.finishLayout();
		/*
		 * FIXME: These images are not in use yet!
		 */
		// putImage(DECREASE_BRIGHTNESS, intensityDownImage);
		// putImage(INCREASE_BRIGHTNESS, intensityUpImage);
		// putImage(DECREASE_BRIGHTNESS, intensityDownImage);
	}

	private void updateMetadataInfo() {
		if (deviceNameLabel != null && !deviceNameLabel.isDisposed()) {
			deviceNameLabel.setText(deviceUnderDisplay.getName());

			if (centeringComposite != null && !centeringComposite.isDisposed()) {
				centeringComposite.pack();
				centeringComposite.layout();
			}
		}
	}

	// FIXME: Pannel graphics should be improved
	// private void putImage(final String path, final Canvas canvas) {
	// final Image image = ApplicationImageCache.getInstance().getImage(path);
	//
	// final GC gc = new GC(intensityDownImage);
	// final int width = image.getImageData().width;
	// final int height = image.getImageData().height;
	// canvas.setSize(width, height);
	// gc.drawImage(image, 0, 0);
	// }

	private void setIntensityInternal(final int intensity) {
		final int intensityValue = intensityScale.getMaximum() - intensity
				+ intensityScale.getMinimum();
		intensityScale.setSelection(intensityValue);
		intensityLevel.setText(Messages.getString(
				"LampPanel.intensityPercent", Integer.toString(intensity))); //$NON-NLS-1$
	}

	private int getIntensityInternal() {
		final int intensityValue = intensityScale.getMaximum()
				- intensityScale.getSelection();
		return intensityValue;
	}

	/**
	 * Updates the controls according to the given status.
	 * <p>
	 * The ON and OFF buttons are not never updated. These buttons are push-only
	 * buttons that do not reflect the state-
	 * 
	 * @param device
	 *            the new supplied status
	 */
	public void updateControls(final Device device) {
		final DeviceStatus status = device.getStatus();
		if (status instanceof OnOffIntensityStatus) {
			final OnOffIntensityStatus newLampStatus = (OnOffIntensityStatus) status;

			setIntensityInternal(newLampStatus.getIntensityPercent());

			/*
			 * FIXME: Image not in used for the moment
			 */
			// if (onButton.getSelection()) {
			// final Image imageOn = ApplicationImageCache.getInstance()
			// .getImage(POWERON_IMAGE);
			// onButton.setImage(imageOn);
			// } else {
			// final Image imageOff = ApplicationImageCache.getInstance()
			// .getImage(POWEROFF_IMAGE);
			// onButton.setImage(imageOff);
			// }
		}
	}

	@Override
	protected void updateFade(final boolean visible, final int duration) {
		if (!timeLeft.isDisposed()) {
			timeLeft.setVisible(visible);
			timeLeft.setTime(duration);
		}
	}
}
