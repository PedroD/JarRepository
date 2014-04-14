package lumina.ui.celleditors;

import lumina.base.model.commands.PanelActions;
import lumina.ui.dialogs.ControlPanelActionEditorDialog;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A cell editor that displays the control panel labels dialog.
 */
public class ControlPanelLabelsCellEditor extends DialogCellEditor {

	/**
	 * The label that gets reused by <code>updateLabel</code>.
	 */
	private Label defaultLabel;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent control
	 */
	public ControlPanelLabelsCellEditor(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent control
	 * @param style
	 *            cell editor style
	 */
	public ControlPanelLabelsCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Opens the control panel labels dialog box.
	 * 
	 * @param cellEditorWindow
	 *            cell editor to which the window will belong to
	 * @return the dialog object
	 */
	protected Object openDialogBox(Control cellEditorWindow) {

		ControlPanelActionEditorDialog dialog = new ControlPanelActionEditorDialog(
				cellEditorWindow.getShell());

		dialog.setPanelActions((PanelActions) this.doGetValue());

		dialog.setBlockOnOpen(true);

		int result = dialog.open();

		if (result == Window.OK)
			return dialog.getPanelActions();

		else
			return null;
	}

	/**
	 * Creates the cell content.
	 * 
	 * @param cell
	 *            cell to which the content belongs to
	 * @return the cell editor control
	 */
	@Override
	protected Control createContents(Composite cell) {
		defaultLabel = new Label(cell, SWT.LEFT);
		defaultLabel.setFont(cell.getFont());
		defaultLabel.setBackground(cell.getBackground());
		return defaultLabel;
	}

	/**
	 * Updates the cell content.
	 * 
	 * @param value
	 *            cell value
	 */
	@Override
	protected void updateContents(Object value) {
		defaultLabel.setText(Messages
				.getString("ControlPanelActionEditorDialog.cellEditor.edit"));
	}

}
