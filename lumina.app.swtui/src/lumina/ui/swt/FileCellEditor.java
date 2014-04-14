package lumina.ui.swt;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;

/**
 * File cell editor.
 */
public class FileCellEditor extends DialogCellEditor {

	private String value;

	/**
	 * Creates the file cell editor.
	 * 
	 * @param parent
	 *            component
	 */
	public FileCellEditor(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Creates the file cell editor.
	 * 
	 * @param parent
	 *            parent component
	 * @param style
	 *            file cell editor style
	 */
	public FileCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the image file dialog box.
	 * 
	 * @param cellEditorWindow
	 *            cell editor
	 * @return image selected
	 * @see org.eclipse.jface.viewers.DialogCellEditor
	 */
	protected String openDialogBox(Control cellEditorWindow) {

		FileDialog dialog = new FileDialog(cellEditorWindow.getShell(),
				SWT.OPEN);

		dialog.setText("Choose Image");

		value = dialog.open();

		return value;
	}

	/**
	 * Disposes the file cell editor.
	 */
	public void dispose() {
		super.dispose();
	}

}
