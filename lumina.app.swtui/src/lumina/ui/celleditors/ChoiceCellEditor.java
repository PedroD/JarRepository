package lumina.ui.celleditors;

import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that presents a list of items in a combo box.
 * <p>
 * Used to edit properties over a set of values.
 */
// FIXME: We should use composition instead of inheritance
// see the documentation of the ComboBoxCellEditor
public class ChoiceCellEditor extends ComboBoxCellEditor {

	public ChoiceCellEditor() {
		super();
	}

	public ChoiceCellEditor(Composite parent, String[] items, int style) {
		super(parent, new String[] {}, style);
	}

	public ChoiceCellEditor(Composite parent, String[] items) {
		super(parent, items);
	}

	@Override
	protected Object doGetValue() {
		final Object value = super.doGetValue();
		if (value instanceof Integer) {
			final int index = (Integer) value;
			final String[] items = getItems();
			if (items != null && 0 <= index && index < items.length) {
				return items[index];
			}
		}

		final Control control = getControl();
		if (control instanceof Combo) {
			return ((Combo) control).getText();
		} else if (control instanceof CCombo) {
			return ((CCombo) control).getText();
		}

		return null;
	}

	@Override
	protected void doSetValue(final Object value) {
		if (value instanceof String) {
			final String[] items = getItems();
			final int choice = SWTUtils.findSelectionIndex((String) value,
					items);
			if (choice >= 0) {
				super.doSetValue(choice);
			} else {
				super.doSetValue(value);
			}
		} else {
			super.doSetValue(value);
		}
	}

	@Override
	public void setItems(String[] items) {
		super.setItems(items);
	}
}
