package lumina.ui.celleditors;

import lumina.base.model.SchedulerPeriodicity;
import lumina.ui.dialogs.SchedulePeriodicityDialog;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A cell editor that displays the scheduler periodicity dialog.<br/>
 * Periodicity has a specific dialog window where options are shown to the user.
 */
public class SchedulePeriodicityCellEditor extends DialogCellEditor {

	/**
	 * Constructor for schedule periodicity cell editor.
	 * 
	 * @param parent
	 *            parent component
	 */
	public SchedulePeriodicityCellEditor(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Constructor for schedule periodicity cell editor.
	 * 
	 * @param parent
	 *            parent component
	 * @param style
	 *            style for the schedule periodicity cell editor
	 */
	public SchedulePeriodicityCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Opens the dialog box and returns user selection.
	 * 
	 * @param cellEditorWindow
	 *            component
	 * @return selected periodicities or null if nothing selected
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		SchedulePeriodicityDialog dialog = new SchedulePeriodicityDialog(
				cellEditorWindow.getShell());
		dialog.setBlockOnOpen(true);

		dialog.setSchedulerPeriodicities((SchedulerPeriodicity) super
				.doGetValue());

		int result = dialog.open();

		if (result == Window.OK)
			return dialog.getSchedulerPeriodicities();
		else
			return null;
	}

	/**
	 * The label that gets reused by <code>updateLabel</code>.
	 */
	private Label defaultLabel;

	/**
	 * Creates the dialog contents.<br/>
	 * Creates the default label.
	 * 
	 * @param cell
	 *            component
	 * @return default label
	 */
	@Override
	protected Control createContents(Composite cell) {
		defaultLabel = new Label(cell, SWT.LEFT);
		defaultLabel.setFont(cell.getFont());
		defaultLabel.setBackground(cell.getBackground());
		return defaultLabel;
	}

	/**
	 * Update contents.<br/>
	 * Update the default label text.
	 * 
	 * @param value
	 *            object
	 */
	@Override
	protected void updateContents(Object value) {
		defaultLabel.setText(Messages
				.getString("SchedulePeriodicity.cellEditor.edit"));
	}

}
