package lumina.ui.celleditors;

import lumina.api.properties.IProperty;

import org.eclipse.jface.viewers.CellEditor;

public final class SimpleCellEditorWrapper extends AbstractCellEditorWrapper {

	public SimpleCellEditorWrapper(CellEditor e, IProperty p) {
		super(e, p);
	}

	@Override
	public Object getValueForWidget(IProperty property) {
		return property.toString();
	}

	@Override
	public Object getValueForProperty(IProperty property) {
		return cellEditor.getValue();
	}
}
