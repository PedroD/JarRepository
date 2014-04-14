package lumina.ui.dialogs;

import lumina.Activator;
import lumina.api.properties.IProperty;
import lumina.base.model.ProjectModel;
import lumina.network.AbstractGateway;
import lumina.network.gateways.api.IGateway;
import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.swtdesigner.ResourceManager;

/**
 * Advanced settings configuration dialog window.
 * <p>
 * This is the dialog that is used to configure the advanced network settings
 * like refresh speed, collision avoidance and timeout.
 */
public class AdvancedNetworkSettingsDialog extends Dialog {

	/**
	 * The dialog title message.
	 */
	private static final String DIALOG_TITLE = Messages
			.getString("AdvancedNetworkSettingsDialog.title"); // $NON-NLS-1$

	/**
	 * The path for the icon of the dialog.
	 */
	private static final String DIALOG_ICON_PATH = "icons/model/interface.png"; //$NON-NLS-1$

	// widgets
	private Combo messageIntervalCombo;
	private Combo hysteresisTimeCombo;
	private Combo refreshSpeedCombo;
	private Spinner spinnerTimeout;
	private Button radioContinuous;
	private Button radioOnRequest;

	private AbstractGateway selectedInterface;

	/**
	 * Creates the dialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param ni
	 *            the network interface
	 */
	public AdvancedNetworkSettingsDialog(Shell parentShell,
			AbstractGateway ni) {
		super(parentShell);
		super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE
				| SWT.APPLICATION_MODAL);
		selectedInterface = ni;
	}

	// CHECKSTYLE:OFF
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		Group grpInterface = new Group(container, SWT.NONE);
		grpInterface.setText(Messages
				.getString("AdvancedNetworkSettingsDialog.grpInterface.text"));
		grpInterface.setLayout(new GridLayout(1, false));

		final Composite mainComposite = new Composite(grpInterface, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));

		// refresh time
		final Label refreshTimeLabel = new Label(mainComposite, SWT.NONE);
		refreshTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		refreshTimeLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.messageInterval.label"));

		final Composite refreshTimeComposite = new Composite(mainComposite,
				SWT.NONE);
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 2;
		refreshTimeComposite.setLayout(gridLayout_4);

		final Label sendMessageTimeLabel = new Label(refreshTimeComposite,
				SWT.NONE);
		sendMessageTimeLabel.setText(Messages
				.getString("AdvancedNetworkSettingsDialog.sendMessage.label"));

		messageIntervalCombo = new Combo(refreshTimeComposite, SWT.READ_ONLY);
		messageIntervalCombo.select(0);

		messageIntervalCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				/*
				 * the available refresh speeds are dependent on the message
				 * interval
				 */
				if (selectedInterface != null) {
					updateScanSpeedCombo(selectedInterface);
				}
			}
		});

		// Collision avoidance
		final Label collisionAvoidanceLabel = new Label(mainComposite, SWT.NONE);
		collisionAvoidanceLabel.setLayoutData(new GridData(SWT.RIGHT,
				SWT.CENTER, false, false, 1, 1));
		collisionAvoidanceLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.collisionAvoidance.label"));

		final Composite collisionAvoidanceComposite = new Composite(
				mainComposite, SWT.NONE);
		final GridLayout gridLayout_ca = new GridLayout();
		gridLayout_ca.numColumns = 3;
		collisionAvoidanceComposite.setLayout(gridLayout_ca);

		final Label collisionAvoidanceMessageTimeLabel = new Label(
				collisionAvoidanceComposite, SWT.NONE);
		collisionAvoidanceMessageTimeLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.collisionAvoidanceMessage.label")); //$NON-NLS-1$

		hysteresisTimeCombo = new Combo(collisionAvoidanceComposite,
				SWT.READ_ONLY);
		hysteresisTimeCombo.select(0);

		final Label collisionAvoidanceFinalLabel = new Label(
				collisionAvoidanceComposite, SWT.NONE);
		collisionAvoidanceFinalLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.collisionAvoidanceFinal.label"));

		// Network timeout
		final Label labelTimeout = new Label(mainComposite, SWT.NONE);
		labelTimeout.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		labelTimeout.setText(Messages
				.getString("AdvancedNetworkSettingsDialog.timeout.label"));

		final Composite compositeTimeout = new Composite(mainComposite,
				SWT.NONE);
		final GridLayout gridLayout_5 = new GridLayout();
		gridLayout_5.numColumns = 2;
		compositeTimeout.setLayout(gridLayout_5);

		spinnerTimeout = new Spinner(compositeTimeout, SWT.BORDER);
		spinnerTimeout.setMinimum(AbstractGateway.MIN_TIMEOUT);
		spinnerTimeout.setMaximum(AbstractGateway.MAX_TIMEOUT);

		final Label sLabel = new Label(compositeTimeout, SWT.NONE);
		sLabel.setText(Messages
				.getString("AdvancedNetworkSettingsDialog.timeoutWait.label"));
		updatePropertyCombo(selectedInterface,
				AbstractGateway.MESSAGE_INTERVAL_PROPERTY_NAME,
				messageIntervalCombo);

		updatePropertyCombo(selectedInterface,
				AbstractGateway.HYSTERESIS_PROPERTY_NAME,
				hysteresisTimeCombo);

		// Scanner group
		final Group scannerGroup = new Group(container, SWT.NONE);
		scannerGroup.setText(Messages
				.getString("AdvancedNetworkSettingsDialog.grpScanner.text"));
		GridData gd_scannerGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_scannerGroup.heightHint = 102;
		gd_scannerGroup.widthHint = 263;
		scannerGroup.setLayoutData(gd_scannerGroup);
		scannerGroup.setLayout(new GridLayout());

		// scanner composite
		final Composite scannerComposite = new Composite(scannerGroup, SWT.NONE);
		scannerComposite.setLocation(0, 84);
		scannerComposite.setSize(189, -52);
		GridData gd_scannerComposite = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		gd_scannerComposite.heightHint = 88;
		scannerComposite.setLayoutData(gd_scannerComposite);
		scannerComposite.setLayout(new GridLayout(2, false));

		// Device scan mode
		final Label labelDeviceRefreshMode = new Label(scannerComposite,
				SWT.NONE);
		labelDeviceRefreshMode
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.deviceStatusUpdateMode.label")); //$NON-NLS-1$

		final Composite networkDeviceUpdateModeSelection = new Composite(
				scannerComposite, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		networkDeviceUpdateModeSelection.setLayout(gridLayout_3);

		radioContinuous = new Button(networkDeviceUpdateModeSelection,
				SWT.RADIO);
		radioContinuous
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.deviceStatusUpdateAutomatic.label")); //$NON-NLS-1$

		radioOnRequest = new Button(networkDeviceUpdateModeSelection, SWT.RADIO);
		radioOnRequest
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.deviceStatusUpdateManual.label")); //$NON-NLS-1$

		// refresh time
		final Label refreshIntervalLabel = new Label(scannerComposite, SWT.NONE);
		refreshIntervalLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		refreshIntervalLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.refreshInterval.label")); //$NON-NLS-1$

		final Composite refreshIntervalComposite = new Composite(
				scannerComposite, SWT.NONE);
		final GridLayout gridLayout_speed = new GridLayout();
		gridLayout_speed.numColumns = 2;
		refreshIntervalComposite.setLayout(gridLayout_speed);

		final Label speedMessageTimeLabel = new Label(refreshIntervalComposite,
				SWT.NONE);
		speedMessageTimeLabel
				.setText(Messages
						.getString("AdvancedNetworkSettingsDialog.refreshRequest.label")); //$NON-NLS-1$

		refreshSpeedCombo = new Combo(refreshIntervalComposite, SWT.READ_ONLY);
		refreshSpeedCombo.select(0);

		setSelectedRefreshMode(selectedInterface.getRefreshMode());

		updatePropertyCombo(selectedInterface,
				AbstractGateway.REFRESH_SPEED_PROPERTY_NAME,
				refreshSpeedCombo);

		updatePropertyCombo(selectedInterface,
				AbstractGateway.REFRESH_SPEED_PROPERTY_NAME,
				refreshSpeedCombo);

		setSelectedTimeout(selectedInterface.getTimeout());

		parent.pack();

		return container;
	}

	/**
	 * Creates the buttons for the button bar.
	 * 
	 * @param parent
	 *            parent component
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button close = createButton(
				parent,
				IDialogConstants.CLOSE_ID,
				Messages.getString("AdvancedNetworkSettingsDialog.close.text"), true); //$NON-NLS-1$

		close.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_closeButton_widgetSelected(e);
			}
		});
	}

	/**
	 * Configures the shell.
	 * 
	 * @param newShell
	 *            new shell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DIALOG_TITLE);
		newShell.setImage(ResourceManager.getPluginImage(
				Activator.getDefault(), DIALOG_ICON_PATH));
	}

	/**
	 * Updates the scanner refresh rate based of the inter-message interval.
	 * 
	 * @param networkInterface
	 *            current interface
	 */
	private void updateScanSpeedCombo(final IGateway networkInterface) {
		if (networkInterface != null) {
			final String selection = messageIntervalCombo
					.getItem(messageIntervalCombo.getSelectionIndex());
			final int msgInterval = AbstractGateway
					.parseTimeString(selection);
			final int[] speeds = networkInterface
					.getSupportedRefreshSpeeds(msgInterval);
			final String[] options = AbstractGateway
					.getTimeChoices(speeds);

			final int selIndex = refreshSpeedCombo.getSelectionIndex();
			refreshSpeedCombo.setItems(options);
			if (options.length > 0 && selIndex < 0) {
				refreshSpeedCombo.select(0);
			} else {
				refreshSpeedCombo.select(selIndex);
			}
		}
	}

	/**
	 * Sets the interface selected timeout value.
	 * 
	 * @param timeout
	 *            timeout value
	 */
	private void setSelectedTimeout(final int timeout) {
		spinnerTimeout.setSelection(timeout);
	}

	/**
	 * Sets the interface selected refresh mode.
	 * 
	 * @param refreshMode
	 *            refresh mode
	 */
	private void setSelectedRefreshMode(
			final IGateway.DeviceStatusPollMode refreshMode) {
		if (IGateway.DeviceStatusPollMode.CONTINUOUS == refreshMode) {
			radioContinuous.setSelection(true);
			radioOnRequest.setSelection(false);
		} else {
			radioContinuous.setSelection(false);
			radioOnRequest.setSelection(true);
		}
	}

	/**
	 * Returns the user selected refresh mode.
	 * 
	 * @return user selected refresh mode
	 */
	private IGateway.DeviceStatusPollMode getSelectedRefreshMode() {
		if (radioContinuous.getSelection()) {
			return IGateway.DeviceStatusPollMode.CONTINUOUS;
		} else {
			return IGateway.DeviceStatusPollMode.ON_REQUEST;
		}
	}

	/**
	 * Updates a combo box with the value of a property.
	 * 
	 * @param networkInterface
	 *            current interface
	 * @param propertyName
	 *            name of the property to queried
	 * @param combo
	 *            a combo box to be updated
	 */
	private void updatePropertyCombo(
			final AbstractGateway networkInterface,
			final String propertyName, final Combo combo) {
		final IProperty property = networkInterface.getPropertyManager()
				.findPropertyByName(propertyName);
		if (property != null) {
			final String[] options = (String[]) property.getChoices();
			combo.setItems(options);

			final String value = (String) property.getValue();
			final int selection = SWTUtils.findSelectionIndex(value, options);
			combo.select(selection);
		}
	}

	/**
	 * Returns the selection of a combo box.
	 * 
	 * @param combo
	 *            combo box
	 * @return combo box selection value or null, if nothing is selected
	 */
	private String getComboSelection(final Combo combo) {
		final int index = combo.getSelectionIndex();
		if (index >= 0) {
			return combo.getItem(index);
		}
		return null;
	}

	/**
	 * Closes the configuration dialog window.
	 * 
	 * @param e
	 *            event
	 */
	private void do_closeButton_widgetSelected(SelectionEvent e) {
		final int timeoutValue = spinnerTimeout.getSelection();
		final String messageInterval = getComboSelection(messageIntervalCombo);
		final String hysteresisTime = getComboSelection(hysteresisTimeCombo);
		final String refreshTime = getComboSelection(refreshSpeedCombo);
		final IGateway.DeviceStatusPollMode resfreshMode = getSelectedRefreshMode();

		// set the network data
		ProjectModel.getInstance().setInterfaceSettings(selectedInterface,
				messageInterval, hysteresisTime, timeoutValue, resfreshMode,
				refreshTime);
		this.close();
	}
}
