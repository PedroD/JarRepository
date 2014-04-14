package lumina.ui.celleditors;

import lumina.api.properties.IProperty;
import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.viewers.ComboBoxCellEditor;

public class ChoiceCellEditorWrapper extends AbstractCellEditorWrapper {

	public static Object getValueForCellEditor(String[] choices, String value) {
		if (choices == null || choices.length == 0) {
			return 0;
		}

		final int choice = SWTUtils.findSelectionIndex(value, choices);
		if (choice > 0) {
			return choice;
		}

		return 0;
	}

	private IProperty property;
	private ComboBoxCellEditor editor;

	public ChoiceCellEditorWrapper(ComboBoxCellEditor e, IProperty p) {
		super(e, p);
		editor = e;
		property = p;
	}

	@Override
	public Object getWidget() {
		editor.setItems((String[]) property.getChoices());
		return super.getWidget();
	}

	@Override
	public Object getValueForWidget(IProperty property) {
		String value = property.getPropertyType().format(property.getValue());
		return getValueForCellEditor((String[]) property.getChoices(), value);
	}

	@Override
	public Object getValueForProperty(IProperty property) {
		return cellEditor.getValue();
		//
		// if (choices != null && choices.length > 0) {
		// final int comboIndex = (Integer) cellEditor.getValue();
		// final boolean validSelection = comboIndex >= 0 && comboIndex <
		// choices.length;
		// if (validSelection) {
		// return choices[comboIndex];
		// }
		// }
		//
		// return property.getValue();
	}
}
