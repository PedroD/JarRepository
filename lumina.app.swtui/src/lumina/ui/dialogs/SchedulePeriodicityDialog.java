package lumina.ui.dialogs;

import java.util.Calendar;

import lumina.Activator;
import lumina.base.model.SchedulerPeriodicity;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.ResourceManager;

/**
 * Schedule periodicity selection dialog window.
 * 
 * @author Fernando Martins
 */
public class SchedulePeriodicityDialog extends Dialog {
	private static final int DIALOG_DEFAULT_HEIGHT = 220;

	private static final int DIALOG_DEFAULT_WIDTH = 200;

	/**
	 * Weekdays initial array position.
	 */
	private static final int FIRST_WEEK_DAY = 0;

	/**
	 * Weekends array position.
	 */
	private static final int WORKING_DAYS_COUNT = 5;

	/**
	 * Number of day in a week.
	 */
	private static final int WEEK_DAYS_COUNT = 7;

	// button indexes
	private static final int MON_BUTTON_INDEX = 0;
	private static final int TUE_BUTTON_INDEX = 1;
	private static final int WED_BUTTON_INDEX = 2;
	private static final int THU_BUTTON_INDEX = 3;
	private static final int FRI_BUTTON_INDEX = 4;
	private static final int SAT_BUTTON_INDEX = 5;
	private static final int SUN_BUTTON_INDEX = 6;

	// widgets
	private Button chkMonday;
	private Button chkTuesday;
	private Button chkWednesday;
	private Button chkThursday;
	private Button chkFriday;
	private Button chkSaturday;
	private Button chkSunday;
	private Button btnAll;
	private Button btnWeekends;
	private Button btnWeekdays;
	private Button btnNone;
	private Label labelWarning;
	private Button[] checks = new Button[WEEK_DAYS_COUNT];

	/** Holds the list of labels being edited. */
	private SchedulerPeriodicity schedulersPeriodicity;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            parent shell
	 */
	public SchedulePeriodicityDialog(Shell parentShell) {
		super(parentShell);

		this.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL
				| SWT.BORDER);
	}

	/**
	 * Returns scheduler periodicity.
	 * 
	 * @return scheduler periodicity
	 */
	public SchedulerPeriodicity getSchedulerPeriodicities() {
		return this.schedulersPeriodicity;
	}

	/**
	 * Sets scheduler periodicity.
	 * 
	 * @param periodicities
	 *            scheduler periodicity
	 */
	public void setSchedulerPeriodicities(SchedulerPeriodicity periodicities) {
		this.schedulersPeriodicity = new SchedulerPeriodicity(periodicities);
	}

	/**
	 * Configure shell.
	 * 
	 * @param newShell
	 *            new shell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setMinimumSize(new Point(DIALOG_DEFAULT_WIDTH,
				DIALOG_DEFAULT_HEIGHT));
		newShell.setText(Messages.getString("SchedulePeriodicity.title"));
		newShell.setImage(ResourceManager.getPluginImage(
				Activator.getDefault(), "icons/model/schedule.gif"));
	}

	/**
	 * Removes all selections.
	 */
	private void selectNone() {
		chkMonday.setSelection(false);
		chkTuesday.setSelection(false);
		chkWednesday.setSelection(false);
		chkThursday.setSelection(false);
		chkFriday.setSelection(false);
		chkSaturday.setSelection(false);
		chkSunday.setSelection(false);
	}

	/**
	 * Applies weekdays selection.
	 */
	private void selectWeekdays() {
		boolean option = true;
		int countOn = 0;

		for (int i = FIRST_WEEK_DAY; i < FIRST_WEEK_DAY + WORKING_DAYS_COUNT; i++) {
			if (checks[i].getSelection()) {
				countOn++;
			}
		}
		if (WORKING_DAYS_COUNT == countOn) {
			option = false;
		} else {
			option = true;
		}
		chkMonday.setSelection(option);
		chkTuesday.setSelection(option);
		chkWednesday.setSelection(option);
		chkThursday.setSelection(option);
		chkFriday.setSelection(option);
	}

	/**
	 * Applies weekends selection.
	 */
	private void selectWeekends() {
		boolean option = true;
		int countOn = 0;

		for (int f = WORKING_DAYS_COUNT; f < WORKING_DAYS_COUNT + 2; f++) {
			if (checks[f].getSelection()) {
				countOn++;
			}
		}
		if (2 == countOn) {
			option = false;
		} else {
			option = true;
		}
		chkSaturday.setSelection(option);
		chkSunday.setSelection(option);
	}

	/**
	 * Applies all selections.
	 */
	private void selectAll() {
		chkMonday.setSelection(true);
		chkTuesday.setSelection(true);
		chkWednesday.setSelection(true);
		chkThursday.setSelection(true);
		chkFriday.setSelection(true);
		chkSaturday.setSelection(true);
		chkSunday.setSelection(true);
	}

	/**
	 * Defines the new user selection.
	 */
	private void defineUserSelection() {
		this.schedulersPeriodicity = new SchedulerPeriodicity();
		if (checks[MON_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.MONDAY);
		if (checks[TUE_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.TUESDAY);
		if (checks[WED_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.WEDNESDAY);
		if (checks[THU_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.THURSDAY);
		if (checks[FRI_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.FRIDAY);
		if (checks[SAT_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.SATURDAY);
		if (checks[SUN_BUTTON_INDEX].getSelection())
			this.schedulersPeriodicity.add(Calendar.SUNDAY);
	}

	/**
	 * Checks the previous user selection.
	 */
	private void checkUserSelection() {
		selectNone();
		checks[MON_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.MONDAY));
		checks[TUE_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.TUESDAY));
		checks[WED_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.WEDNESDAY));
		checks[THU_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.THURSDAY));
		checks[FRI_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.FRIDAY));
		checks[SAT_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.SATURDAY));
		checks[SUN_BUTTON_INDEX].setSelection(this.schedulersPeriodicity
				.contains(Calendar.SUNDAY));
	}

	/**
	 * Method that creates the widgets for this editor.
	 * 
	 * @param parent
	 *            parent component
	 * @return dialog area
	 */
	// CHECKSTYLE:OFF - Automatically generated code for the UI
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);
		container.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));

		final Group executionDaysGroup = new Group(container, SWT.NONE);
		final GridData gd_executionDaysGroup = new GridData(SWT.RIGHT,
				SWT.CENTER, false, false);
		executionDaysGroup.setLayoutData(gd_executionDaysGroup);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.marginHeight = 0;
		gridLayout_1.numColumns = 2;
		executionDaysGroup.setLayout(gridLayout_1);

		final Composite composite_1 = new Composite(executionDaysGroup,
				SWT.NONE);
		composite_1
				.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.makeColumnsEqualWidth = true;
		gridLayout_2.numColumns = 2;
		composite_1.setLayout(gridLayout_2);

		chkMonday = new Button(composite_1, SWT.CHECK);
		chkMonday.setText(Messages.getString("SchedulePeriodicity.monday"));
		checks[0] = chkMonday;

		chkSaturday = new Button(composite_1, SWT.CHECK);
		chkSaturday.setText(Messages.getString("SchedulePeriodicity.saturday"));
		checks[5] = chkSaturday;

		chkTuesday = new Button(composite_1, SWT.CHECK);
		chkTuesday.setText(Messages.getString("SchedulePeriodicity.tuesday"));
		checks[1] = chkTuesday;

		chkSunday = new Button(composite_1, SWT.CHECK);
		chkSunday.setText(Messages.getString("SchedulePeriodicity.sunday"));
		checks[6] = chkSunday;

		chkWednesday = new Button(composite_1, SWT.CHECK);
		chkWednesday.setText(Messages
				.getString("SchedulePeriodicity.wednesday"));
		checks[2] = chkWednesday;
		new Label(composite_1, SWT.NONE);

		chkThursday = new Button(composite_1, SWT.CHECK);
		chkThursday.setText(Messages.getString("SchedulePeriodicity.thursday"));
		checks[3] = chkThursday;
		new Label(composite_1, SWT.NONE);

		chkFriday = new Button(composite_1, SWT.CHECK);
		chkFriday.setText(Messages.getString("SchedulePeriodicity.friday"));
		checks[4] = chkFriday;
		new Label(composite_1, SWT.NONE);

		final Composite composite = new Composite(executionDaysGroup, SWT.NONE);
		composite.setLayoutData(new GridData());
		composite.setLayout(new GridLayout());

		btnAll = new Button(composite, SWT.NONE);
		btnAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				selectAll();
			}
		});
		btnAll.setText(Messages.getString("SchedulePeriodicity.everyDay"));

		btnWeekdays = new Button(composite, SWT.NONE);
		btnWeekdays.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		btnWeekdays.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				selectWeekdays();
			}
		});
		btnWeekdays.setText(Messages.getString("SchedulePeriodicity.weekdays"));

		btnWeekends = new Button(composite, SWT.NONE);
		btnWeekends.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		btnWeekends.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				selectWeekends();
			}
		});
		btnWeekends.setText(Messages.getString("SchedulePeriodicity.weekends"));

		btnNone = new Button(composite, SWT.NONE);
		btnNone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnNone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				selectNone();
			}
		});
		btnNone.setText(Messages.getString("SchedulePeriodicity.none"));

		labelWarning = new Label(container, SWT.NONE);
		labelWarning.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true,
				false));
		labelWarning.setAlignment(SWT.CENTER);
		labelWarning.setText(Messages
				.getString("SchedulePeriodicity.pleaseSelectDays"));
		labelWarning.setVisible(false);

		checkUserSelection();
		return container;
	}

	// CHECKSTYLE:ON

	/**
	 * Create buttons of the button bar.
	 * 
	 * @param parent
	 *            parent component
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Button pressed event handler.
	 * 
	 * @param buttonId
	 *            identifier of the pressed button
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			defineUserSelection();

			if (this.schedulersPeriodicity.size() == 0) {
				labelWarning.setVisible(true);
				return;
			}
		}
		super.buttonPressed(buttonId);
	}

}
