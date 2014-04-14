package lumina.ui.views.control.panels;

import java.util.ArrayList;
import java.util.List;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.IDeviceDriver;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.base.model.commands.Action;
import lumina.base.model.commands.DeviceCommand;
import lumina.base.model.devices.ControlPanelDevice;
import lumina.base.model.devices.status.ScenarioStatus;
import lumina.base.model.devices.status.SetScenarioCommand;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.network.drivers.IScenarioDeviceDriver;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.swt.MouseWheelEventListener;
import lumina.ui.views.ViewUtils;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * A panel with buttons to choose the scenario.
 * <p>
 * The buttons will be rendered, one for each scene specified in the
 * {@link lumina.base.model.commands.PanelActions} of the {@link ControlPanelDevice}.
 * <p>
 * A control panel requires a device with a {@link ScenarioStatus}.
 */
// TODO: Save scene should only apply to scenarios

// FIXME: Listeners should be inner classes
public class ScenarioPanel extends AbstractDevicePanel {

	private static final int SCENARIO_BUTTON_DEFAULT_INDENT = 4;

	/**
	 * Indicates the position in the control panel window where the button
	 * should be moved to.
	 */
	private static final double AUTO_SCROLL_RELATIVE_POS = 1.0 / 2.0;

	private static final int SCENARIO_BUTTON_WIDTH = 100;

	private static final int SCENARIO_BUTTON_HEIGHT = 30;

	private static final int SAVE_BUTTON_HEIGHT = 30;

	private static final int SAVE_BUTTON_WIDTH = 30;

	private static final String EDIT_LOCK = "/icons/actions/edit.png"; //$NON-NLS-1$

	private static final String OFF_BUTTON_TOOLTIP = Messages
			.getString("ControlPanelUIStandard.offTooltip");

	private static final String SAVE_SCENARIO_BUTTON_LABEL = Messages
			.getString("ControlPanelUIStandard.saveScenario");

	/**
	 * The scene status.
	 */
	private final ScenarioStatus sceneStatus;

	/**
	 * The control panel under display.
	 */
	private final ControlPanelDevice controlPanelUnderDisplay;

	/**
	 * The area label composite.
	 */
	private final Composite areaLabelComposite;

	/**
	 * The button holder.
	 */
	private final Composite buttonHolder;

	/**
	 * The off button.
	 */
	private Button offButton;

	/**
	 * The edit lock button.
	 */
	private final Button editLockButton;

	/**
	 * The area name label.
	 */
	private final Label areaNameLabel;

	/**
	 * The fade label.
	 */
	private final FadeTimerLabel fadeLabel;

	/**
	 * The mouse wheel event listener.
	 */
	private final MouseWheelEventListener mouseWheelEventListener;

	/**
	 * Scrolled composite for the scenarios group.
	 */
	private ScrolledComposite scrolledComposite;

	/**
	 * The button order label list.
	 */
	private List<Label> buttonOrderLabelList = new ArrayList<Label>();

	/**
	 * The scenario button list. When the user click one of these buttons a new
	 * scenario is selected.
	 */
	private List<Button> actionButtonList = new ArrayList<Button>();

	/**
	 * The save scenario button list. When the user selects one of these buttons
	 * the current levels for the circuits are saved. Currently on the network.
	 */
	private List<Button> saveActionButtonList = new ArrayList<Button>();

	/**
	 * Gets the parent area name of the given device.
	 * 
	 * @param device
	 *            the device to get the parent area
	 * @return the parent area name
	 */
	private static String getAreaName(final Device device) {
		final Area parentArea = Queries.getAncestorArea(device);
		if (parentArea != null) {
			return parentArea.getName();
		} else {
			return Messages.getString("UI.notAssigned"); //$NON-NLS-1$
		}
	}

	/**
	 * Checks if an action should have a corresponding "Save" button.
	 * 
	 * @param action
	 *            the action to be checked
	 * @return <code>true</code> if the action command is a save scenario and
	 *         <code>false</code> otherwise.
	 */
	private static boolean shouldHaveSaveButton(final Action action) {
		final DeviceCommand command = action.getCommand();
		return command.getCommandType().equals(
				SetScenarioCommand.SET_SCENARIO_COMMAND_TYPE);
	}

	/**
	 * Creates the action and save buttons.
	 * 
	 * @param parent
	 *            the parent to add the buttons to
	 * @param panelActions
	 *            the actions to create the buttons for
	 */
	private void createButtons(final Composite parent, Action[] panelActions) {
		final int numberOfActions = panelActions.length;

		final GridData gdLabel = new GridData();
		gdLabel.horizontalIndent = SCENARIO_BUTTON_DEFAULT_INDENT;
		gdLabel.verticalIndent = SCENARIO_BUTTON_DEFAULT_INDENT;

		final GridData gdScenarioButton = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		gdScenarioButton.minimumHeight = SCENARIO_BUTTON_HEIGHT;
		gdScenarioButton.minimumWidth = SCENARIO_BUTTON_WIDTH;

		/*
		 * Flag used to determine if the editlock button should be enabled or
		 * not;
		 */
		boolean hasAtLeastOneSaveButton = false;

		/*
		 * Create the buttons
		 */
		for (int buttonNumber = 0; buttonNumber < numberOfActions; buttonNumber++) {
			final Action action = panelActions[buttonNumber];

			/*
			 * Creates the scenario number button
			 */
			final Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(gdLabel);
			if (shouldHaveSaveButton(action)) {
				SetScenarioCommand c = (SetScenarioCommand) action.getCommand();
				label.setText(String.valueOf(c.getScenarioNumber()));
				label.setForeground(UIConstants.getReadOnlyColor());
				buttonOrderLabelList.add(label);

				hasAtLeastOneSaveButton = true;
			}

			/*
			 * Creates the "command/scenario" button
			 */
			final Button actionButton = new Button(parent, SWT.TOGGLE
					| SWT.CENTER);
			actionButton.setLayoutData(gdScenarioButton);
			actionButton.setText(action.getLabel());

			if (panelActions[buttonNumber].canEditCommand()) {
				actionButton.setData(action);
				actionButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						handleSelectActionEvent(actionButton, event);
					}
				});
			} else {
				actionButton.setData(Integer
						.valueOf(ScenarioStatus.OFF_SCENARIO_NUMBER));
				actionButton.setToolTipText(OFF_BUTTON_TOOLTIP);
				actionButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						deactivateActionButtons();
						sceneStatus.setCurrentScene(
								ScenarioStatus.OFF_SCENARIO_NUMBER, true);
					}
				});
			}

			actionButton.setToolTipText(Messages.getString(
					"ControlPanelUIStandard.selectScenarioTooltip", // $NON-NLS-1$
					action.getLabel()));
			actionButtonList.add(actionButton);

			/*
			 * Creates the "Save" button
			 */
			final Button saveActionButton = new Button(parent, SWT.NONE);
			final GridData gdEditLockButton = new GridData(SWT.FILL,
					SWT.BOTTOM, false, true);
			gdEditLockButton.minimumHeight = SAVE_BUTTON_HEIGHT;
			gdEditLockButton.minimumWidth = SAVE_BUTTON_WIDTH;
			saveActionButton.setLayoutData(gdEditLockButton);
			saveActionButton.setData(action);

			if (shouldHaveSaveButton(action)) {
				saveActionButton.setText(SAVE_SCENARIO_BUTTON_LABEL);
				saveActionButton.setToolTipText(Messages.getString(
						"ControlPanelUIStandard.saveScenarioTooltip",
						action.getLabel())); // $NON-NLS-1$
				saveActionButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						handleSaveActionEvent(saveActionButton, event);
					}
				});
			}
			saveActionButtonList.add(saveActionButton);
		}

		editLockButton.setEnabled(hasAtLeastOneSaveButton);
		parent.pack();
	}

	/**
	 * Activates selection for a given scenario button.
	 * 
	 * @param scenarioNumber
	 *            the number of the scenario to activate
	 */
	private void activateScenarioButton(final int scenarioNumber) {
		/*
		 * Do nothing. It is not clear how to give feed-back regarding the
		 * currently selected scenario using a regular button.
		 * 
		 * If the button is rendered pressed, how should the user know that the
		 * button can be pressed again to select the scenario.
		 */
		deactivateActionButtons();
	}

	/**
	 * Deactivates selection from all scenario buttons.
	 * <p>
	 * Does not include the OFF button.
	 */
	private void deactivateActionButtons() {
		for (Button scenarioButton : actionButtonList) {
			scenarioButton.setSelection(false);
		}
	}

	/**
	 * Shows/hides the save buttons.
	 * 
	 * @param show
	 *            flag indicating if buttons should be shown
	 */
	private void updateSaveButtons(boolean show) {
		/*
		 * update labels
		 */
		for (Label label : buttonOrderLabelList) {
			if (!label.isDisposed())
				label.setVisible(show);
		}

		/*
		 * update the save buttons
		 */
		for (Button saveButton : saveActionButtonList) {
			final Action a = (Action) saveButton.getData();

			if (shouldHaveSaveButton(a)) {
				saveButton.setEnabled(show);
				saveButton.setVisible(show);
			} else {
				saveButton.setEnabled(false);
				saveButton.setVisible(false);
			}
		}

	}

	/**
	 * Changes the scenario edition mode.
	 */
	private void updateActionEditionMode() {
		final boolean inEditMode = controlPanelUnderDisplay.isEditMode();

		/*
		 * update the button
		 */
		editLockButton.setSelection(inEditMode);

		/*
		 * Can be changed to show an unlock image, if needed
		 */
		editLockButton.setImage(ApplicationImageCache.getInstance().getImage(
				EDIT_LOCK));

		updateSaveButtons(inEditMode);
	}

	/**
	 * Updates the controls of the control panel.
	 */
	private void updateControlsInternal() {
		if (areaNameLabel != null && !areaNameLabel.isDisposed()) {
			final String areaName = getAreaName(controlPanelUnderDisplay);
			if (areaName == null) {
				areaNameLabel.setText(""); //$NON-NLS-1$
			} else {
				areaNameLabel.setText(areaName);
			}

			if (!areaLabelComposite.isDisposed()) {
				areaLabelComposite.pack();
				areaLabelComposite.layout();
			}
		}

		if (actionButtonList != null) {
			actionButtonList.clear();
			saveActionButtonList.clear();
			ViewUtils.disposeExistingControls(buttonHolder);

			createButtons(buttonHolder, controlPanelUnderDisplay
					.getPanelActions().elements());
			updateActionEditionMode();

			activateScenarioButton(sceneStatus.getCurrentScene());
		}
		// createActionButtons(buttonHolder,
		// controlPanelUnderDisplay.getPanelActions().elements());
		editLockButton.setVisible(Capabilities
				.canDo(Capability.DEVICE_EDIT_PROP_SCENARIO));
	}

	/**
	 * Handles the save scenario event from the GUI.
	 * 
	 * @param saveButton
	 *            the button where the save occurs
	 * @param event
	 *            the event
	 */
	private void handleSaveActionEvent(final Button saveButton,
			final Event event) {
		saveButton.setSelection(false);

		final Action a = (Action) saveButton.getData();
		final SetScenarioCommand c = (SetScenarioCommand) a.getCommand();
		final int n = c.getScenarioNumber();

		/*
		 * try saving the scenario
		 */
		final IDeviceDriver deviceDriver = controlPanelUnderDisplay.getDriver();
		if (deviceDriver != null) {
			((IScenarioDeviceDriver) deviceDriver).saveScenario(n);
		}

		scrollTo(saveButton);
	}

	/**
	 * Handles the scenario selection event from the GUI.
	 * 
	 * @param actionButton
	 *            the panel action button
	 * @param event
	 *            the event
	 */
	private void handleSelectActionEvent(final Button actionButton,
			final Event event) {
		actionButton.setSelection(true);

		final Action a = (Action) actionButton.getData();
		a.execute(this.getDeviceUnderDisplay());

		scrollTo(actionButton);
	}

	/**
	 * Method that is called when an event occurs on the GUI.
	 * 
	 * @param event
	 *            the event object
	 */
	private void handleEvent(final Event event) {
		assert actionButtonList.size() == saveActionButtonList.size();

		if (event.widget == offButton) {
			deactivateActionButtons();
			sceneStatus.setCurrentScene(ScenarioStatus.OFF_SCENARIO_NUMBER,
					true);
		} else if (event.widget == editLockButton) {
			controlPanelUnderDisplay.setEditMode(!controlPanelUnderDisplay
					.isEditMode());
			updateActionEditionMode();
		}
	}

	/**
	 * Scrolls panel to show specified button.
	 * 
	 * @param button
	 *            button to show
	 */
	private void scrollTo(final Control button) {
		final Rectangle bounds = button.getBounds();
		final int buttonCenterY = bounds.y;
		final int parentPlacementY = scrolledComposite.getBounds().height;
		final int y = buttonCenterY
				- (int) (AUTO_SCROLL_RELATIVE_POS * parentPlacementY);

		final Point p = scrolledComposite.getOrigin();
		p.y = y;

		scrolledComposite.setOrigin(p);
	}

	/**
	 * Listener that receives the Property change events from the model and
	 * updates the widgets appropriately.
	 */
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		/**
		 * Handles status change events.
		 * <p>
		 * When the scenario number is changed, another button is selected.
		 * 
		 * @param event
		 *            the event object
		 */
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			final Object subject = event.getSource();
			final String changedProperty = event.getProperty();

			if (!PropertyChangeNames.isStatusChange(changedProperty)) {
				assert ModelUtils.isModelItem(subject);
				final Area areaUnderDisplay = controlPanelUnderDisplay
						.getParentArea();

				if (subject == controlPanelUnderDisplay
						|| subject == areaUnderDisplay) {
					updateControlsInternal();
				}
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.base.model.devices.ui.AbstractDevicePanel#updateFade(boolean, int)
	 */
	@Override
	protected void updateFade(final boolean visible, final int duration) {
		if (!fadeLabel.isDisposed()) {
			fadeLabel.setVisible(visible);
			fadeLabel.setTime(duration);
		}
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 *            the panel parent
	 * @param style
	 *            the SWT widget style
	 * @param device
	 *            the device
	 */
	// CHECKSTYLE:OFF
	public ScenarioPanel(final Composite parent, final int style,
			final Device device) {
		super(parent, style, device);
		setLayout(new GridLayout());

		if (!(device.getStatus() instanceof ScenarioStatus)) {
			throw new IllegalArgumentException(
					"Wrong kind of status for control panel UI"); //$NON-NLS-1$
		}
		sceneStatus = (ScenarioStatus) device.getStatus();

		if (!(device instanceof ControlPanelDevice)) {
			throw new IllegalArgumentException(
					"Wrong kind of device found for the control panel UI"); //$NON-NLS-1$
		}

		controlPanelUnderDisplay = (ControlPanelDevice) device;

		final String deviceParentAreaName = getAreaName(controlPanelUnderDisplay);

		final GridLayout mainGridLayout = new GridLayout();
		mainGridLayout.marginTop = 0;
		mainGridLayout.marginWidth = 0;
		// setLayout(mainGridLayout);

		// Area label composite
		areaLabelComposite = new Composite(this, SWT.NONE);
		areaLabelComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM,
				true, false));
		final GridLayout gridLayoutAreaLabelComposite = new GridLayout();
		gridLayoutAreaLabelComposite.verticalSpacing = 0;
		areaLabelComposite.setLayout(gridLayoutAreaLabelComposite);

		// Parent area name
		areaNameLabel = new Label(areaLabelComposite, SWT.NONE);
		areaNameLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
				true));
		areaNameLabel.setText(deviceParentAreaName);
		areaNameLabel.setFont(UIConstants.getTitleFont());
		areaNameLabel.setForeground(UIConstants.getTitleColor());

		// Main composite
		final Composite mainComposite = new Composite(this, SWT.NONE);
		mainComposite
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayoutMainComposite = new GridLayout();
		gridLayoutMainComposite.verticalSpacing = 0;
		gridLayoutMainComposite.numColumns = 2;
		mainComposite.setLayout(gridLayoutMainComposite);

		// Group
		// Scenarios Group
		final Group scenariosGroup = new Group(mainComposite, SWT.NONE);
		scenariosGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		final GridLayout gridLayoutScenariosGroup = new GridLayout();
		gridLayoutScenariosGroup.marginHeight = 0;
		gridLayoutScenariosGroup.verticalSpacing = 0;
		gridLayoutScenariosGroup.marginWidth = 0;
		gridLayoutScenariosGroup.marginLeft = 10;
		scenariosGroup.setLayout(gridLayoutScenariosGroup);

		scrolledComposite = new ScrolledComposite(scenariosGroup, SWT.V_SCROLL);
		scrolledComposite.setDragDetect(false);
		scrolledComposite.setExpandHorizontal(true);
		final GridData gdScrolledComposite = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		gdScrolledComposite.widthHint = 149;
		scrolledComposite.setLayoutData(gdScrolledComposite);
		scrolledComposite.setEnabled(true);

		// Scenario buttons composite
		buttonHolder = new Composite(scrolledComposite, SWT.NONE);
		final GridLayout buttonHolderLayout = new GridLayout();
		buttonHolderLayout.numColumns = 3;
		buttonHolder.setLayout(buttonHolderLayout);
		scrolledComposite.setContent(buttonHolder);
		buttonHolder.setBounds(0, 0, 82, 65);

		/*
		 * Adds the mouse wheel event listener
		 */
		mouseWheelEventListener = new MouseWheelEventListener(buttonHolder) {
			@Override
			public void scrollVertically(int direction) {
				final int increment = direction * SCENARIO_BUTTON_HEIGHT
						* scrolledComposite.getVerticalBar().getIncrement();
				scrolledComposite.getVerticalBar().setSelection(
						scrolledComposite.getVerticalBar().getSelection()
								+ increment);

				final Point p = scrolledComposite.getOrigin();
				p.y = scrolledComposite.getVerticalBar().getSelection();
				scrolledComposite.setOrigin(p);
			}
		};

		// Intensity composite
		final Composite intensityComposite = new Composite(mainComposite,
				SWT.NONE);
		final GridData gdIntensityComposite = new GridData(SWT.CENTER,
				SWT.FILL, false, true);
		gdIntensityComposite.heightHint = 361;
		gdIntensityComposite.verticalIndent = 5;
		gdIntensityComposite.horizontalIndent = 4;
		intensityComposite.setLayoutData(gdIntensityComposite);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		intensityComposite.setLayout(gridLayout);

		editLockButton = new Button(intensityComposite, SWT.TOGGLE);
		editLockButton.setToolTipText(Messages
				.getString("ControlPanelUIStandard.editLock")); //$NON-NLS-1$
		final GridData gdEditLockButton = new GridData(SWT.FILL, SWT.BOTTOM,
				true, true);
		gdEditLockButton.minimumHeight = 30;
		gdEditLockButton.minimumWidth = 40;
		editLockButton.setLayoutData(gdEditLockButton);
		updateActionEditionMode();
		editLockButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ScenarioPanel.this.handleEvent(event);
			}
		});

		// The fade time label
		fadeLabel = new FadeTimerLabel(mainComposite, SWT.CENTER);
		fadeLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fadeLabel.setText("--:--");

		// Request to update the controls
		updateControlsInternal();

		// register the property change listeners of the model
		ProjectModel.getInstance().addPropertyChangeListener(
				propertyChangeListener);
	}

	// CHECKSTYLE:ON

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		mouseWheelEventListener.dispose();
		ProjectModel.getInstance().removePropertyChangeListener(
				propertyChangeListener);
		super.dispose();
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
		if (status instanceof ScenarioStatus) {
			final ScenarioStatus scenarioStatus = (ScenarioStatus) status;
			final int newScenario = scenarioStatus.getCurrentScene();
			activateScenarioButton(newScenario);
		}
	}

}
