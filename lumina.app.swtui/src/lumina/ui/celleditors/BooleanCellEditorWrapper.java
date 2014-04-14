package lumina.ui.celleditors;

import lumina.api.properties.IProperty;


public class BooleanCellEditorWrapper extends AbstractCellEditorWrapper {

	public BooleanCellEditorWrapper(BooleanCellEditor e, IProperty p) {
		super(e, p);
	}

	@Override
	public Object getValueForWidget(IProperty property) {
		final Object value = property.getValue();
		final String[] choices = (String[]) property.getChoices();

		if (choices != null && choices.length >= 2) {
			if (value.equals(choices[0])) {
				return Boolean.TRUE;
			}

			if (value.equals(choices[1])) {
				return Boolean.FALSE;
			}
		}

		return Boolean.FALSE;
	}

	@Override
	public Object getValueForProperty(IProperty property) {
		return cellEditor.getValue();
	}
}
