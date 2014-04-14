package lumina.ui.celleditors;

import lumina.base.model.Floor;
import lumina.ui.dialogs.FloorPlanDialog;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that displays the FloorPlanDialog.
 */
public class FloorPlanCellEditor extends DialogCellEditor {

	private final Floor parentFloor;

	/**
	 * Constructor for floor plan dialog.
	 * 
	 * @param parent
	 *            component
	 * @param floor
	 *            floor
	 */
	public FloorPlanCellEditor(final Composite parent, final Floor floor) {
		this(parent, SWT.NONE, floor);
	}

	/**
	 * Constructor for floor plan dialog.
	 * 
	 * @param parent
	 *            component
	 * @param style
	 *            style for this floor plan dialog
	 * @param floor
	 *            floor
	 */
	public FloorPlanCellEditor(Composite parent, int style, final Floor floor) {
		super(parent, style);
		parentFloor = floor;
	}

	/**
	 * Opens the floor plan dialog box.
	 * 
	 * @param cellEditorWindow
	 *            cell editor window
	 * @return floor plan dialog selection or null
	 * @see FloorPlanDialog
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		final FloorPlanDialog dialog = new FloorPlanDialog(
				cellEditorWindow.getShell(), parentFloor);
		dialog.setBlockOnOpen(true);

		final Object value = getValue();
		if (value instanceof String) {
			dialog.setInitialSelection((String) value);
		}

		int result = dialog.open();

		if (result == FloorPlanDialog.OK) {
			return dialog.getSelection();
		} else {
			return null;
		}
	}

}
