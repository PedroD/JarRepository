package lumina.ui.dialogs;

import gnu.io.SerialPort;
import lumina.Activator;
import lumina.api.properties.IPropertySet;
import lumina.base.model.Area;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.bundles.drivers.ilight.PCNodeProtocol;
import lumina.bundles.drivers.ilight.SI2Protocol;
import lumina.extensions.drivers.rs232rxtx.transport.RS232Driver;
import lumina.network.AbstractGateway;
import lumina.network.NetworkInterfaceManager;
import lumina.network.gateways.api.IGateway;
import lumina.network.osgi.registries.DriverRegistry;
import lumina.network.transport.api.ITransportDriver;
//import lumina.energymanager.network.modbus.gateways.SEnergyProtocol;
//import lumina.energymanager.network.openweathermap.OpenWeatherProtocol;
import lumina.ui.jface.EnhancedTableViewer;
import lumina.ui.swt.SimpleDialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.swtdesigner.ResourceManager;

/**
 * Interface configuration dialog window.
 * <p>
 * This is the dialog that is used to configure the connections to the bus
 * network.
 */
public class InterfaceConfigurationDialog extends Dialog implements
		DriverRegistry.ServiceRegistryListener {

	/**
	 * The dialog title message.
	 */
	private static final String DIALOG_TITLE = Messages
			.getString("InterfaceConfigurationDialog.interfaces.text"); // $NON-NLS-1$

	/**
	 * The path for the icon of the dialog.
	 */
	private static final String DIALOG_ICON_PATH = "icons/model/interface.png"; //$NON-NLS-1$

	private static boolean isChanged = false;

	private static final int ILIGHT_PCNODE = 0;
	private static final int ILIGHT_SI2 = 1;
	private static final int X10 = 2;

	// widgets
	private TableViewer tableViewer;
	private Combo protocolCombo;
	private Combo driverCombo;
	private ControlDecoration driverComboDecoration;
	private Combo portCombo;
	private ControlDecoration portComboDecoration;
	private Combo baudRateCombo;
	private Combo parityCombo;
	private Combo dataBitsCombo;
	private Combo stopBitsCombo;
	private Combo flowControlCombo;

	private Button enableDTRCheckBox;
	private Button enableRTSCheckBox;
	private Button applyButton;

	private Button removeButton;
	private Button advancedSettingsButton;

	private AbstractGateway selectedInterface;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public InterfaceConfigurationDialog(Shell parentShell) {
		super(parentShell);
		super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE
				| SWT.APPLICATION_MODAL);
	}

	/**
	 * Creates the dialog.
	 */
	@Override
	public void create() {
		super.create();

		// Pre-select the first item in the list if not already selected (varies
		// by OS).
		// Call the selectionChanged by hand in those cases where it will not be
		// called automatically.
		final Table table = tableViewer.getTable();
		if (table.getItemCount() > 0) {
			if (table.getSelectionCount() == 0) {
				table.select(0);
				do_tableViewer_selectionChanged(null);
			}
		} else {
			do_tableViewer_selectionChanged(null);
		}

		DriverRegistry.getInstance().addServiceListener(this);
		enableAllControls();
	}

	/**
	 * Creates the dialog area for the interface configuration.
	 * 
	 * @param parent
	 *            parent component
	 * @return dialog area
	 */
	// CHECKSTYLE:OFF
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Group interfacesGroup = new Group(container, SWT.NONE);
		interfacesGroup.setText(Messages
				.getString("InterfaceConfigurationDialog.interfaces.text"));
		GridData gd_interfacesGroup = new GridData(SWT.LEFT, SWT.FILL, true,
				true);
		gd_interfacesGroup.heightHint = 217;
		gd_interfacesGroup.widthHint = 243;
		interfacesGroup.setLayoutData(gd_interfacesGroup);
		interfacesGroup.setLayout(new GridLayout());

		tableViewer = new EnhancedTableViewer(interfacesGroup,
				SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						do_tableViewer_selectionChanged(event);
					}
				});
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setInput(new Object());

		Table interfacesTable = tableViewer.getTable();
		interfacesTable.setHeaderVisible(false);
		interfacesTable.setDragDetect(false);
		final GridData gd_interfacesTable = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		gd_interfacesTable.heightHint = 245;
		gd_interfacesTable.widthHint = 204;
		gd_interfacesTable.minimumWidth = 180;
		interfacesTable.setLayoutData(gd_interfacesTable);

		final Composite buttonComposite = new Composite(interfacesGroup,
				SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.makeColumnsEqualWidth = true;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.numColumns = 2;
		buttonComposite.setLayout(gridLayout_1);
		buttonComposite.setDragDetect(false);

		final Button addButton = new Button(buttonComposite, SWT.NONE);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_addButton_widgetSelected(e);
			}
		});
		addButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addButton.setText(Messages
				.getString("InterfaceConfigurationDialog.add.text")); //$NON-NLS-1$
		addButton.setToolTipText(Messages
				.getString("InterfaceConfigurationDialog.add.tooltip")); //$NON-NLS-1$

		removeButton = new Button(buttonComposite, SWT.NONE);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_removeButton_widgetSelected(e);
			}
		});

		final GridData gd_removeButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_removeButton.minimumWidth = 60;
		removeButton.setLayoutData(gd_removeButton);
		removeButton.setText(Messages
				.getString("InterfaceConfigurationDialog.remove.text")); //$NON-NLS-1$
		removeButton.setToolTipText(Messages
				.getString("InterfaceConfigurationDialog.remove.tooltip")); //$NON-NLS-1$

		final Group interfaceDetailsGroup = new Group(container, SWT.NONE);
		interfaceDetailsGroup.setLayout(new GridLayout());
		GridData gd_interfaceDetailsGroup = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		gd_interfaceDetailsGroup.heightHint = 398;
		gd_interfaceDetailsGroup.widthHint = 282;
		interfaceDetailsGroup.setLayoutData(gd_interfaceDetailsGroup);
		interfaceDetailsGroup
				.setText(Messages
						.getString("InterfaceConfigurationDialog.configurationDetails.text")); //$NON-NLS-1$

		// protocol configuration composite
		final Composite protocolConfigurationComposite = new Composite(
				interfaceDetailsGroup, SWT.NONE);
		GridData gd_protocolConfigurationComposite = new GridData(SWT.FILL,
				SWT.FILL, true, true);
		gd_protocolConfigurationComposite.widthHint = 299;
		gd_protocolConfigurationComposite.heightHint = 277;
		protocolConfigurationComposite
				.setLayoutData(gd_protocolConfigurationComposite);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.horizontalSpacing = 8;
		gridLayout_2.numColumns = 3;
		protocolConfigurationComposite.setLayout(gridLayout_2);

		/*
		 * populate the protocol composite
		 */
		final Label protocolLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		protocolLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.protocol.label")); //$NON-NLS-1$
		protocolLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));

		protocolCombo = new Combo(protocolConfigurationComposite, SWT.READ_ONLY
				| SWT.DROP_DOWN);
		final GridData gd_protocolCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		gd_protocolCombo.widthHint = 107;
		gd_protocolCombo.minimumWidth = 80;
		protocolCombo.setLayoutData(gd_protocolCombo);
		protocolCombo.setEnabled(true);
		protocolCombo.setItems(DriverRegistry.getInstance()
				.getRegisteredServicesNames());
		protocolCombo.select(0);
		protocolCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (tableViewer.getTable().getItemCount() > 0)
					do_enableApplyButton(e);
			}
		});

		advancedSettingsButton = new Button(protocolConfigurationComposite,
				SWT.NONE);
		advancedSettingsButton.setLayoutData(new GridData(SWT.RIGHT,
				SWT.CENTER, false, false, 1, 1));
		advancedSettingsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openDialogBox(selectedInterface);
			}
		});
		advancedSettingsButton.setText(Messages
				.getString("InterfaceConfigurationDialog.advanced.text")); //$NON-NLS-1$
		advancedSettingsButton.setToolTipText(Messages
				.getString("InterfaceConfigurationDialog.advanced.tooltip"));

		// Physical layer network access details
		final Label configurationDetailsSeparator = new Label(
				protocolConfigurationComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gd_configurationDetailsSeparator = new GridData(
				SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd_configurationDetailsSeparator.verticalIndent = 6;
		configurationDetailsSeparator
				.setLayoutData(gd_configurationDetailsSeparator);

		final Label driverLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		driverLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		driverLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.networkDriver.label"));

		driverCombo = new Combo(protocolConfigurationComposite, SWT.NONE);
		final GridData gd_driverCombo = new GridData(120, SWT.DEFAULT);
		gd_driverCombo.horizontalSpan = 2;
		gd_driverCombo.grabExcessHorizontalSpace = true;
		gd_driverCombo.minimumWidth = 80;
		driverCombo.setLayoutData(gd_driverCombo);
		driverCombo.setEnabled(false);
		driverCombo.setItems(new String[] { "RS232" });

		driverComboDecoration = new ControlDecoration(driverCombo, SWT.LEFT
				| SWT.TOP);
		driverComboDecoration.setMarginWidth(1);
		driverComboDecoration.setImage(ResourceManager.getPluginImage("Lumina",
				"icons/overlays/overlay_error.png"));
		driverComboDecoration.setDescriptionText("");
		driverComboDecoration.hide();
		driverCombo.select(0);

		final Label portLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		final GridData gd_portLabel = new GridData(SWT.RIGHT, SWT.CENTER,
				false, false);
		gd_portLabel.verticalIndent = 6;
		portLabel.setLayoutData(gd_portLabel);
		portLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.port.label"));

		portCombo = new Combo(protocolConfigurationComposite, SWT.NONE);
		final GridData gd_portCombo = new GridData(SWT.LEFT, SWT.CENTER, true,
				false);
		gd_portCombo.horizontalSpan = 2;
		gd_portCombo.widthHint = 120;
		gd_portCombo.minimumWidth = 80;
		gd_portCombo.verticalIndent = 6;
		portCombo.setLayoutData(gd_portCombo);
		portCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_enableApplyButton(e);
			}
		});

		final String[] portNames = (new RS232Driver()).getExistingPortNames();
		final boolean portNamesAssigned = portNames != null;
		portCombo.setEnabled(portNamesAssigned);
		portCombo.setEnabled(true);

		if (portNames != null) {
			for (int i = 0; i < portNames.length; i++) {
				portCombo.add(portNames[i]);
			}
		}
		portCombo.select(0);

		portComboDecoration = new ControlDecoration(portCombo, SWT.LEFT
				| SWT.TOP);
		portComboDecoration.setMarginWidth(1);
		portComboDecoration.setImage(ResourceManager.getPluginImage("Lumina",
				"icons/overlays/overlay_warning.gif"));
		portComboDecoration.setDescriptionText("");
		portComboDecoration.hide();

		final Label baudRateLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		baudRateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		baudRateLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.baudRate.label"));

		baudRateCombo = new Combo(protocolConfigurationComposite, SWT.READ_ONLY);
		baudRateCombo.setEnabled(false);
		baudRateCombo.setItems(new String[] { "1200", "2400", "4800", "9600",
				"19200", "38400", "57600", "115200" });

		final GridData gd_baudRateCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		gd_baudRateCombo.horizontalSpan = 2;
		gd_baudRateCombo.minimumWidth = 80;
		gd_baudRateCombo.widthHint = 80;
		baudRateCombo.setLayoutData(gd_baudRateCombo);
		baudRateCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_enableApplyButton(e);
			}
		});

		final Label parityLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		parityLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		parityLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.parity.label"));

		parityCombo = new Combo(protocolConfigurationComposite, SWT.READ_ONLY);
		parityCombo.setEnabled(false);
		parityCombo.setItems(new String[] {
				translateParityToString(SerialPort.PARITY_NONE),
				translateParityToString(SerialPort.PARITY_EVEN),
				translateParityToString(SerialPort.PARITY_ODD),
				translateParityToString(SerialPort.PARITY_MARK),
				translateParityToString(SerialPort.PARITY_SPACE) });

		final GridData gd_parityCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		gd_parityCombo.horizontalSpan = 2;
		gd_parityCombo.minimumWidth = 80;
		gd_parityCombo.widthHint = 80;
		parityCombo.setLayoutData(gd_parityCombo);
		parityCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_enableApplyButton(e);
			}
		});

		final Label dataBitsLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		dataBitsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		dataBitsLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.dataBits.label"));

		dataBitsCombo = new Combo(protocolConfigurationComposite, SWT.READ_ONLY);
		dataBitsCombo.setEnabled(false);
		dataBitsCombo.setItems(new String[] { "5", "6", "7", "8" });

		final GridData gd_dataBitsCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		gd_dataBitsCombo.horizontalSpan = 2;
		gd_dataBitsCombo.minimumWidth = 80;
		gd_dataBitsCombo.widthHint = 80;
		dataBitsCombo.setLayoutData(gd_dataBitsCombo);
		dataBitsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_enableApplyButton(e);
			}
		});

		final Label stopBitsLabel = new Label(protocolConfigurationComposite,
				SWT.NONE);
		stopBitsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		stopBitsLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.stopBits.label"));

		stopBitsCombo = new Combo(protocolConfigurationComposite, SWT.READ_ONLY);
		stopBitsCombo.setEnabled(false);
		stopBitsCombo
				.setItems(new String[] { translateStopBitsToString(1.0),
						translateStopBitsToString(1.5),
						translateStopBitsToString(2.0) });

		final GridData gd_stopBitsCombo = new GridData(SWT.LEFT, SWT.CENTER,
				true, false);
		gd_stopBitsCombo.horizontalSpan = 2;
		gd_stopBitsCombo.minimumWidth = 80;
		gd_stopBitsCombo.widthHint = 80;
		stopBitsCombo.setLayoutData(gd_stopBitsCombo);
		stopBitsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_enableApplyButton(e);
			}
		});

		final Label flowControlLabel = new Label(
				protocolConfigurationComposite, SWT.NONE);
		flowControlLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false));
		flowControlLabel.setText(Messages
				.getString("InterfaceConfigurationDialog.flowControl.label"));

		flowControlCombo = new Combo(protocolConfigurationComposite,
				SWT.READ_ONLY);
		flowControlCombo
				.setItems(new String[] {
						translateFlowControlToString(SerialPort.FLOWCONTROL_NONE),
						translateFlowControlToString(SerialPort.FLOWCONTROL_RTSCTS_IN),
						translateFlowControlToString(SerialPort.FLOWCONTROL_RTSCTS_OUT),
						translateFlowControlToString(SerialPort.FLOWCONTROL_XONXOFF_IN),
						translateFlowControlToString(SerialPort.FLOWCONTROL_XONXOFF_OUT) });
		flowControlCombo.setEnabled(false);

		final GridData gd_flowCombo = new GridData(SWT.LEFT, SWT.CENTER, true,
				false);
		gd_flowCombo.horizontalSpan = 2;
		gd_flowCombo.minimumWidth = 80;
		gd_flowCombo.widthHint = 80;
		flowControlCombo.setLayoutData(gd_flowCombo);

		new Label(protocolConfigurationComposite, SWT.NONE);

		enableDTRCheckBox = new Button(protocolConfigurationComposite,
				SWT.CHECK);
		enableDTRCheckBox.setText(Messages
				.getString("InterfaceConfigurationDialog.btnEnableDtr.text")); //$NON-NLS-1$
		enableDTRCheckBox.setEnabled(false);
		new Label(protocolConfigurationComposite, SWT.NONE);
		new Label(protocolConfigurationComposite, SWT.NONE);

		enableRTSCheckBox = new Button(protocolConfigurationComposite,
				SWT.CHECK);
		enableRTSCheckBox.setText("Enable RTS");
		enableRTSCheckBox.setEnabled(false);
		new Label(protocolConfigurationComposite, SWT.NONE);

		Composite composite = new Composite(interfaceDetailsGroup, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		final GridLayout gridLayout2 = new GridLayout();
		gridLayout2.makeColumnsEqualWidth = true;
		composite.setLayout(gridLayout2);

		applyButton = new Button(composite, SWT.NONE);
		final GridData gd_applyButton = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		gd_applyButton.minimumWidth = 60;
		applyButton.setLayoutData(gd_applyButton);
		applyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				do_applyButton_widgetSelected(e);
			}
		});

		applyButton.setEnabled(false);
		applyButton.setText(Messages
				.getString("InterfaceConfigurationDialog.apply.text")); //$NON-NLS-1$
		applyButton.setToolTipText(Messages
				.getString("InterfaceConfigurationDialog.apply.tooltip"));

		final Label separationLabel = new Label(container, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		final GridData gd_separationLabel = new GridData(SWT.FILL, SWT.BOTTOM,
				true, false, 2, 1);
		gd_separationLabel.verticalIndent = 6;
		separationLabel.setLayoutData(gd_separationLabel);

		Project proj = ProjectModel.getInstance().getProject();
		NetworkInterfaceManager pm = proj.getNetworkInterfaceManager();
		IGateway[] niArray = pm.getInterfaces();

		if (niArray.length > 0)
			selectedInterface = (AbstractGateway) pm.getInterfaces()[0];

		if (selectedInterface == null) {
			baudRateCombo.select(0);
			parityCombo.select(0);
			stopBitsCombo.select(0);
			dataBitsCombo.select(0);
			flowControlCombo.select(0);
			advancedSettingsButton.setEnabled(false);
		} else {
			setConfigForSelectedInterface(selectedInterface);
			advancedSettingsButton.setEnabled(true);
		}

		parent.pack();
		refreshListAndSelect(0);
		return container;
	}

	// CHECKTYLE:ON

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
				Messages.getString("InterfaceConfigurationDialog.close.text"), true); //$NON-NLS-1$

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
	 * Content provider for the interfaces table.
	 */
	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(final Object inputElement) {
			final Project proj = ProjectModel.getInstance().getProject();
			final NetworkInterfaceManager pm = proj
					.getNetworkInterfaceManager();
			return pm.getInterfaces();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Opens the advanced network settings configuration dialog box.
	 * 
	 * @param ni
	 *            the network interface
	 */
	private void openDialogBox(AbstractGateway ni) {
		AdvancedNetworkSettingsDialog dialog = new AdvancedNetworkSettingsDialog(
				this.getShell(), ni);
		dialog.setBlockOnOpen(true);
		dialog.open();
	}

	/**
	 * Builds the interface name.
	 * 
	 * @param protocolName
	 *            protocol name
	 * @param driverName
	 *            driver name
	 * @param portName
	 *            port name
	 * @return the name of the interface
	 */
	private String buildInterfaceName(final String protocolName,
			final String driverName, final String portName) {

		return protocolName + " " + driverName + " " + portName;
	}

	/**
	 * Check if an interface is free.
	 * 
	 * @param oldName
	 *            interface old name
	 * @param newName
	 *            interface new name
	 * @return true if interface does not exist
	 */
	private boolean isInterfaceFree(final String oldName, final String newName) {
		boolean result = oldName.equals(newName);

		if (!result) {
			final Project proj = ProjectModel.getInstance().getProject();
			final NetworkInterfaceManager pm = proj
					.getNetworkInterfaceManager();
			result = true;
			for (String protoName : pm.getAllInterfaceNames(proj)) {
				if (protoName.equals(newName) && !protoName.equals(oldName)) {
					result = false;
					break;
				}
			}
		}

		if (!result) {
			final String errTitle = Messages
					.getString("InterfaceConfigurationDialog.interfaceExist.title"); //$NON-NLS-1$
			final String errMessage = Messages
					.getString(
							"InterfaceConfigurationDialog.interfaceExist.message", newName); //$NON-NLS-1$
			SimpleDialogs.showError(errTitle, errMessage, true);
		}

		return result;
	}

	/**
	 * Check if an interface is free.
	 * 
	 * @param oldName
	 *            interface old name
	 * @return true if interface does not exist
	 */
	private boolean isInterfaceFree(final String newName) {
		return isInterfaceFree("", newName);
	}

	private static void setComboValue(final Combo combo, final String value) {
		final String[] options = combo.getItems();
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(value)) {
				combo.select(i);
				break;
			}
		}
	}

	private static String getProtocolString(AbstractGateway ni) {
		return ni.getName();
	};

	/**
	 * Fills the combo boxes with the selected network interface serial port
	 * settings.
	 * 
	 * @param ni
	 *            the network interface object
	 */
	private void setConfigForSelectedInterface(AbstractGateway ni) {
		if (!ni.getGatewayConnectionName().contains("KNX")) {
			final RS232Driver transportDriver = (RS232Driver) ni
					.getTransportDriver();

			if (transportDriver != null) {
				final IPropertySet settings = transportDriver.getPropertySet();

				setComboValue(protocolCombo, getProtocolString(ni));
				setComboValue(
						baudRateCombo,
						String.valueOf(settings.findPropertyByName(
								RS232Driver.BAUDRATE_PROP_NAME).getValue()));
				setComboValue(
						stopBitsCombo,
						translateStopBitsToString((Double) settings
								.findPropertyByName(
										RS232Driver.STOPBITS_PROP_NAME)
								.getValue()));
				setComboValue(
						dataBitsCombo,
						String.valueOf(settings.findPropertyByName(
								RS232Driver.DATABITS_PROP_NAME).getValue()));
				setComboValue(
						parityCombo,
						translateParityToString((Integer) settings
								.findPropertyByName(
										RS232Driver.PARITY_PROP_NAME)
								.getValue()));
				// TODO: Criar estas props
				// setComboValue(flowControlCombo,
				// translateFlowControlToString(settings.getFlowControl()));
				//
				// enableDTRCheckBox.setSelection(settings.isDTR());
				// enableRTSCheckBox.setSelection(settings.isRTS());
			}
		}
	}

	/**
	 * Fills the port combo-box with the available ports, and pre-selects the
	 * given port. If the given port does not exist it is added to the list.
	 * 
	 * @param drive
	 *            the communication driver object
	 */
	private void updateDriverAndPortCombos(final ITransportDriver driver) {
		// portCombo.clearSelection();
		// portCombo.removeAll();

		if (driver instanceof RS232Driver) {
			final ITransportDriver rs232driver = (RS232Driver) driver;

			// if (!driver.isDriverLibPresent()) {
			// driverComboDecoration.setDescriptionText(driver.getErrorMessage()
			// + "\n"
			// + driver.getErrorResolution());
			// driverComboDecoration.show();
			// } else {
			// driverComboDecoration.hide();
			// }

			final String[] portNames = rs232driver.getExistingPortNames();
			final String selectedPort = (String) rs232driver.getPropertySet()
					.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
					.getValue();

			final boolean portNamesAssigned = portNames != null;
			portCombo.setEnabled(portNamesAssigned);
			portCombo.setEnabled(true);

			if (portNames != null) {
				if (selectedPort != null) {
					for (int i = 0; i < portNames.length; i++) {
						portCombo.add(portNames[i]);
						if (portNames[i].equals(selectedPort)) {
							portCombo.select(i);
						}
					}
				}
			}

			// if (driver.isDriverLibPresent()) {
			// portCombo.setEnabled(true);
			//
			// /*
			// * If the port does not exist on the system we complain.
			// */
			// if (!driver.isPortPresent()) {
			// portComboDecoration.show();
			// portComboDecoration.setDescriptionText(driver.getErrorMessage() +
			// "\n"
			// + driver.getErrorResolution());
			// } else {
			// portComboDecoration.hide();
			// }
			// } else {
			// portCombo.setEnabled(false);
			// portComboDecoration.hide();
			// }

			/*
			 * Add the port to the list even when the port was not found in this
			 * machine's port list.
			 */
			if (selectedPort != null && portCombo.getSelectionIndex() == -1) {
				portCombo.add(selectedPort);
				portCombo.select(portCombo.getItemCount() - 1);
			}

		}
	}

	/**
	 * Refreshes the interfaces list
	 * 
	 * @param index
	 *            List index to select
	 */
	private void refreshListAndSelect(int index) {
		tableViewer.refresh();
		tableViewer.getTable().select(index);

		/*
		 * this isn't called automatically
		 */
		do_tableViewer_selectionChanged(null);

		/*
		 * enable/disable the remove button
		 */
		final TableItem[] protcolLines = tableViewer.getTable().getItems();
		final boolean hasProtocols = protcolLines != null
				&& protcolLines.length > 0;
		removeButton.setEnabled(hasProtocols);
		advancedSettingsButton.setEnabled(hasProtocols);
	}

	/**
	 * Enables or disables all the interface configuration controls of a given
	 * interface.
	 * 
	 * @param enabled
	 *            enables or disables the controls
	 * @param protocol
	 *            the interface
	 */
	private void setConfig(final boolean enabled, final int protocol) {
		// disableAllControls();

		/*
		 * if (!enabled) { portCombo.removeAll(); }
		 */
		// general
		portCombo.setEnabled(enabled);
		applyButton.setEnabled(enabled);

		switch (protocol) {
		case ILIGHT_PCNODE:
			break;
		case ILIGHT_SI2:
		case X10:
			baudRateCombo.setEnabled(enabled);
			parityCombo.setEnabled(enabled);
			dataBitsCombo.setEnabled(enabled);
			stopBitsCombo.setEnabled(enabled);
			flowControlCombo.setEnabled(enabled);
			enableDTRCheckBox.setEnabled(enabled);
			enableRTSCheckBox.setEnabled(enabled);
			break;
		default:
			break;
		}
		enableAllControls();
	}

	/**
	 * Disables all controls in the interface configuration dialog.
	 */
	private void disableAllControls() {
		// general
		portCombo.setEnabled(false);
		applyButton.setEnabled(false);

		baudRateCombo.setEnabled(false);
		parityCombo.setEnabled(false);
		dataBitsCombo.setEnabled(false);
		stopBitsCombo.setEnabled(false);
		flowControlCombo.setEnabled(false);
		enableDTRCheckBox.setEnabled(false);
		enableRTSCheckBox.setEnabled(false);
	}

	/**
	 * Enables all controls in the interface configuration dialog.
	 */
	private void enableAllControls() {
		// general
		portCombo.setEnabled(true);
		applyButton.setEnabled(true);

		baudRateCombo.setEnabled(true);
		parityCombo.setEnabled(true);
		dataBitsCombo.setEnabled(true);
		stopBitsCombo.setEnabled(true);
		flowControlCombo.setEnabled(true);
		enableDTRCheckBox.setEnabled(true);
		enableRTSCheckBox.setEnabled(true);
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
	 * Maps the parity description to its corresponding value.
	 * 
	 * @param parity
	 *            the string to translate
	 * @return an integer corresponding to serial port parity value or
	 *         <code>-1</code> in case it doesn't have a match.
	 */
	private int translateStringToParity(String parity) {
		if (parity.equals("None"))
			return SerialPort.PARITY_NONE;
		if (parity.equals("Even"))
			return SerialPort.PARITY_EVEN;
		if (parity.equals("Odd"))
			return SerialPort.PARITY_ODD;
		if (parity.equals("Mark"))
			return SerialPort.PARITY_MARK;
		if (parity.equals("Space"))
			return SerialPort.PARITY_SPACE;
		return -1;
	}

	private String translateFlowControlToString(int flowControl) {
		if (flowControl == SerialPort.FLOWCONTROL_NONE)
			return "None";
		if (flowControl == SerialPort.FLOWCONTROL_RTSCTS_IN)
			return "RTS/CTS (in)";
		if (flowControl == SerialPort.FLOWCONTROL_RTSCTS_OUT)
			return "RTS/CTS (out)";
		if (flowControl == SerialPort.FLOWCONTROL_XONXOFF_IN)
			return "XON/XOFF (in)";
		if (flowControl == SerialPort.FLOWCONTROL_XONXOFF_OUT)
			return "XON/XOFF (out)";
		return "";
	}

	private int translateStringToFlowControl(String flowControl) {
		if (flowControl.equals("None"))
			return SerialPort.FLOWCONTROL_NONE;
		if (flowControl.equals("RTS/CTS (in)"))
			return SerialPort.FLOWCONTROL_RTSCTS_IN;
		if (flowControl.equals("RTS/CTS (out)"))
			return SerialPort.FLOWCONTROL_RTSCTS_OUT;
		if (flowControl.equals("XON/XOFF (in)"))
			return SerialPort.FLOWCONTROL_XONXOFF_IN;
		if (flowControl.equals("XON/XOFF (out)"))
			return SerialPort.FLOWCONTROL_XONXOFF_OUT;
		return -1;
	}

	private String translateParityToString(int parity) {
		if (parity == SerialPort.PARITY_NONE)
			return "None";
		if (parity == SerialPort.PARITY_EVEN)
			return "Even";
		if (parity == SerialPort.PARITY_ODD)
			return "Odd";
		if (parity == SerialPort.PARITY_MARK)
			return "Mark";
		if (parity == SerialPort.PARITY_SPACE)
			return "Space";
		return "";
	}

	private Double translateStringToStopBits(String dataBits) {
		if (dataBits.equals("1"))
			return 1.0;
		if (dataBits.equals("1.5"))
			return 1.5;
		if (dataBits.equals("2"))
			return 2.0;
		return -1.0;
	}

	private String translateStopBitsToString(Double dataBits) {
		if (dataBits == 1.0)
			return "1";
		if (dataBits == 1.5)
			return "1.5";
		if (dataBits == 2.0)
			return "2";
		return "";
	}

	/**
	 * Interface table selection.
	 * 
	 * @param event
	 *            event
	 */
	private void do_tableViewer_selectionChanged(SelectionChangedEvent event) {
		final Table table = tableViewer.getTable();
		if (table.getSelectionCount() != 1 || table.getSelectionIndex() == -1) {
			setConfig(false, ILIGHT_SI2);
			selectedInterface = null;
		} else {
			final String protoNameSel = ((TableItem) table.getSelection()[0])
					.getText();
			setInterfaceConfig(protoNameSel);

			final Project proj = ProjectModel.getInstance().getProject();
			final NetworkInterfaceManager pm = proj
					.getNetworkInterfaceManager();

			selectedInterface = (AbstractGateway) pm.getInterfaces()[table
					.getSelectionIndex()];

			setConfigForSelectedInterface(selectedInterface);

			updateDriverAndPortCombos(selectedInterface.getTransportDriver());
		}
	}

	/**
	 * Unlocks configuration parameters based on the given protocol.
	 * 
	 * @param protoNameSel
	 *            the protocol
	 */
	// TODO: INTERFACES?
	private void setInterfaceConfig(String protoNameSel) {
		if (protoNameSel.contains("X10")) {
			protocolCombo.select(1);
			setConfig(true, X10);
		}
		if (protoNameSel.contains("iLight/SI2")) {
			protocolCombo.select(1);
			setConfig(true, ILIGHT_SI2);
		} else if (protoNameSel.contains("iLight/PCNode")) {
			protocolCombo.select(0);
			setConfig(true, ILIGHT_PCNODE);
		}
	}

	/**
	 * Sets the value of the variable.
	 * <p>
	 * Used to verify that a change occurred.
	 * 
	 * @param isChanged
	 *            the new boolean value.
	 */
	public static void setChanged(boolean isChanged) {
		InterfaceConfigurationDialog.isChanged = isChanged;
	}

	/**
	 * Gets the value.
	 * 
	 * @return a boolean value
	 */
	public static boolean getIsChanged() {
		return isChanged;
	}

	/**
	 * Add button event.<br/>
	 * Adds a new interface to the list.
	 * 
	 * @param e
	 *            event
	 */
	// TODO: Este método é chamado no botão add, e é aqui que são criadas as
	// novas instancias de cada protocolo... (e não devia de ser...!)
	protected void do_addButton_widgetSelected(SelectionEvent e) {
		// Start with default values
		String protoName = protocolCombo.getText();
		String driverName = driverCombo.getText();
		String portName = portCombo.getText();

		// When possible, get the user selected values (???)
		/*
		 * if (tableViewer.getTable().getItemCount() > 0) { portName =
		 * portCombo.getText(); }
		 */
		final String newName = buildInterfaceName(protoName, driverName,
				portName);

		if (isInterfaceFree(newName)) {
			ITransportDriver driver = null;
			IGateway protocol = null;

			if (protoName.contains("SI2")) { // Hammer time!!
				// TODO: FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				/*
				 * protocol = new SI2Protocol(); protocol.setName(newName);
				 */
				protocol = DriverRegistry.getInstance().getService(
						"iLight (SI2)");
				driver = new RS232Driver();
				protocol.setDefaultTransportDriverSettings(driver
						.getPropertySet());
				driver.getPropertySet()
						.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
						.setValue(portName);
				protocol.setTransportDriver(driver);
			} else if (protoName.contains("PCNode")) {
				protocol = new PCNodeProtocol();
				protocol.setName(newName);

				driver = new RS232Driver();
				protocol.setDefaultTransportDriverSettings(driver
						.getPropertySet());
				driver.getPropertySet()
						.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
						.setValue(portName);
				protocol.setTransportDriver(driver);
			} else if (protoName.contains("CM17A")) { // Hammer time!!
				// TODO: FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				/*
				 * protocol = new X10Protocol(); protocol.setName(newName);
				 */
				protocol = DriverRegistry.getInstance().getService(
						"CM17A (X10)");
				driver = new RS232Driver();
				protocol.setDefaultTransportDriverSettings(driver
						.getPropertySet());
				driver.getPropertySet()
						.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
						.setValue(portName);
				protocol.setTransportDriver(driver);
			} else if (protoName.contains("CM11A")) { // Hammer time!!
				// TODO: FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				/*
				 * protocol = new X10Protocol(); protocol.setName(newName);
				 */
				protocol = DriverRegistry.getInstance().getService(
						"CM11A (X10)");
				driver = new RS232Driver();
				protocol.setDefaultTransportDriverSettings(driver
						.getPropertySet());
				driver.getPropertySet()
						.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
						.setValue(portName);
				protocol.setTransportDriver(driver);
				// TODO: KNX REGISTRY
				// } else if (protoName.contains("KNX")) {
				// protocol = new KNXProtocol(newName);
				// driver = new KNXDriver();
				// protocol.setTransportDriver(driver);

				// TODO: Energy Manager
				// } else if (protoName.contains("Open Weather")) {
				// protocol = new OpenWeatherProtocol();
				// // driver = new KNXDriver();
				// } else if (protoName.contains("S. Energy")) {
				// protocol = new SEnergyProtocol("SEnergy");
			}
			ProjectModel.getInstance().addInterface(
					ProjectModel.getInstance().getProject(), protocol);

			// select the new interface
			refreshListAndSelect(tableViewer.getTable().getItemCount());
			setConfigForSelectedInterface(selectedInterface);
		}
	}

	/**
	 * Removes the selected interface from the list.
	 * 
	 * @param e
	 *            event
	 */
	protected void do_removeButton_widgetSelected(SelectionEvent e) {
		final Project proj = ProjectModel.getInstance().getProject();
		final NetworkInterfaceManager pm = proj.getNetworkInterfaceManager();
		final int useCount = Queries.getAreasUsingInterface(proj,
				selectedInterface).length;
		boolean shouldRemove = false;

		if (useCount > 0) {
			if (proj.getDefaultInterface() == selectedInterface) {
				final Area[] areas = Queries.getAllAreas(proj);
				final boolean hasAreas = areas != null && areas.length > 0;
				if (hasAreas) {
					shouldRemove = SimpleDialogs
							.showQuestion(
									Messages.getString("InterfaceConfigurationDialog.interfaceProjectDelete.title"),
									Messages.getString(
											"InterfaceConfigurationDialog.interfaceProjectDelete.text",
											proj.getName(),
											selectedInterface.getName()), true);
				}
			} else {
				shouldRemove = SimpleDialogs
						.showQuestion(
								Messages.getString("InterfaceConfigurationDialog.interfaceInUse.title"),
								Messages.getString(
										"InterfaceConfigurationDialog.interfaceInUse.text",
										selectedInterface.getName()), true);
			}
		} else {
			shouldRemove = true;
		}

		if (shouldRemove) {
			ProjectModel.getInstance().deleteInterface(pm, selectedInterface);
			refreshListAndSelect(0);
		}
	}

	/**
	 * Enables the apply button when some setting is changed.
	 * 
	 * @param e
	 *            event
	 */
	protected void do_enableApplyButton(SelectionEvent e) {
		setChanged(true);
		applyButton.setEnabled(true);
	}

	/**
	 * Apply interface settings.
	 * 
	 * @param e
	 *            event
	 */
	protected void do_applyButton_widgetSelected(SelectionEvent e) {
		final Project proj = ProjectModel.getInstance().getProject();
		final NetworkInterfaceManager pm = proj.getNetworkInterfaceManager();

		final String protoName = getComboSelection(protocolCombo);
		final String driverName = getComboSelection(driverCombo);
		final String portName = getComboSelection(portCombo);

		final int parityValue = translateStringToParity(getComboSelection(parityCombo));
		final int baudRate = Integer.valueOf(getComboSelection(baudRateCombo));
		final int dataBits = Integer.valueOf(getComboSelection(dataBitsCombo));
		final Double stopBits = translateStringToStopBits(getComboSelection(stopBitsCombo));
		final int flowControl = translateStringToFlowControl(getComboSelection(flowControlCombo));
		final boolean enableDTR = enableDTRCheckBox.getSelection();
		final boolean enableRTS = enableRTSCheckBox.getSelection();

		final int timeoutValue = selectedInterface.getTimeout();
		final int messageInterval = selectedInterface.getMessageInterval();
		final int hysteresisTime = selectedInterface.getHysteresisPeriod();
		final int refreshTime = selectedInterface.getRefreshMsgInterval();
		final IGateway.DeviceStatusPollMode resfreshMode = selectedInterface
				.getRefreshMode();

		final String newName = buildInterfaceName(protoName, driverName,
				portName);

		// This is needed since there is no functional method to change a port
		// on a working interface
		ProjectModel.getInstance().deleteInterface(pm, selectedInterface);

		RS232Driver driver = null;
		AbstractGateway protocol = null;

		// construct a new driver and protocol
		if (protoName.contains("SI2")) {
			protocol = new SI2Protocol();
			protocol.setName(newName);

			// sets the driver
			driver = new RS232Driver();
			IPropertySet properties = driver.getPropertySet();
			properties.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
					.setValue(portName);
			properties.findPropertyByName(RS232Driver.BAUDRATE_PROP_NAME)
					.setValue(baudRate);
			properties.findPropertyByName(RS232Driver.PARITY_PROP_NAME)
					.setValue(parityValue);
			properties.findPropertyByName(RS232Driver.STOPBITS_PROP_NAME)
					.setValue(stopBits);
			properties.findPropertyByName(RS232Driver.DATABITS_PROP_NAME)
					.setValue(dataBits);
			// TODO: VersionService.getApplicationName() as a driver property
			// TODO: 2000

			protocol.setTransportDriver(driver);

			// set network data
			protocol.setRefreshMode(resfreshMode);
			protocol.setHysteresys(hysteresisTime);
			protocol.setMessageInterval(messageInterval);
			protocol.setRefreshSpeed(refreshTime);
			protocol.setTimeout(timeoutValue);

		} else if (protoName.contains("PCNode")) {
			protocol = new PCNodeProtocol();
			protocol.setName(newName);

			// sets the driver
			driver = new RS232Driver();
			IPropertySet properties = driver.getPropertySet();
			properties.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
					.setValue(portName);
			properties.findPropertyByName(RS232Driver.BAUDRATE_PROP_NAME)
					.setValue(baudRate);
			properties.findPropertyByName(RS232Driver.PARITY_PROP_NAME)
					.setValue(parityValue);
			properties.findPropertyByName(RS232Driver.STOPBITS_PROP_NAME)
					.setValue(stopBits);
			properties.findPropertyByName(RS232Driver.DATABITS_PROP_NAME)
					.setValue(dataBits);
			// TODO: VersionService.getApplicationName() as a driver property
			// TODO: 2000
			protocol.setTransportDriver(driver);

			// set network data
			protocol.setRefreshMode(resfreshMode);
			protocol.setHysteresys(hysteresisTime);
			protocol.setMessageInterval(messageInterval);
			protocol.setRefreshSpeed(refreshTime);
			protocol.setTimeout(timeoutValue);
		} else if (protoName.contains("X10")) {
			// TODO: FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			/*
			 * protocol = new X10Protocol(); protocol.setName(newName);
			 */
			// sets the driver
			driver = new RS232Driver();
			IPropertySet properties = driver.getPropertySet();
			properties.findPropertyByName(RS232Driver.PORT_NAME_PROP_NAME)
					.setValue(portName);
			properties.findPropertyByName(RS232Driver.BAUDRATE_PROP_NAME)
					.setValue(baudRate);
			properties.findPropertyByName(RS232Driver.PARITY_PROP_NAME)
					.setValue(parityValue);
			properties.findPropertyByName(RS232Driver.STOPBITS_PROP_NAME)
					.setValue(stopBits);
			properties.findPropertyByName(RS232Driver.DATABITS_PROP_NAME)
					.setValue(dataBits);
			// TODO: VersionService.getApplicationName() as a driver property
			// TODO: 2000
			protocol.setTransportDriver(driver);

			// set network data
			protocol.setRefreshMode(resfreshMode);
			protocol.setHysteresys(hysteresisTime);
			protocol.setMessageInterval(messageInterval);
			protocol.setRefreshSpeed(refreshTime);
			protocol.setTimeout(timeoutValue);
			// TODO: Energy Manager
			// } else if (protoName.contains("Open Weather")) {
			// protocol = new OpenWeatherProtocol();
			// // driver = new KNXDriver();
			// } else if (protoName.contains("S. Energy")) {
			// protocol = new SEnergyProtocol("SEnergy");
		}

		// add the new interface to the project
		ProjectModel.getInstance().addInterface(proj, protocol);

		// FIXME: use this function instead
		// set the network data
		// ProjectModel.getInstance().setInterfaceSettings(protocol,
		// messageInterval, hysteresisTime,
		// timeoutValue, resfreshMode, refreshTime);

		refreshListAndSelect(tableViewer.getTable().getSelectionIndex());

		setChanged(false);
		applyButton.setEnabled(false);
	}

	/**
	 * Close interface configuration dialog window.
	 * 
	 * @param e
	 *            event
	 */
	private void do_closeButton_widgetSelected(SelectionEvent e) {
		DriverRegistry.getInstance().removeServiceListener(this);
		this.close();
	}

	/**
	 * Updates the interface if a new driver is added or removed.
	 */
	@Override
	public void serviceAdded(String driverName) {
		this.serviceModified(driverName);
	}

	@Override
	public void serviceModified(String serviceName) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				protocolCombo.setItems(DriverRegistry.getInstance()
						.getRegisteredServicesNames());
				if (protocolCombo.getSelectionIndex() == -1)
					protocolCombo.select(0);
				protocolCombo.redraw();
			}
		});
	}

	@Override
	public void serviceRemoved(String driverName) {
		this.serviceModified(driverName);
	}

	protected Point getInitialSize() {
		return new Point(568, 523);
	}
}
