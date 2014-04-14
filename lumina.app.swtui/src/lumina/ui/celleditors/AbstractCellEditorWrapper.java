package lumina.ui.celleditors;

import lumina.api.properties.IProperty;

import org.eclipse.jface.viewers.CellEditor;

public abstract class AbstractCellEditorWrapper {

	protected final CellEditor cellEditor;
	protected final IProperty property;

	public AbstractCellEditorWrapper(CellEditor editor, IProperty p) {
		cellEditor = editor;
		property = p;
	}

	public Object getWidget() {
		return cellEditor;
	}

	public IProperty getProperty() {
		return property;
	}

	public abstract Object getValueForWidget(IProperty property);

	public abstract Object getValueForProperty(IProperty property);
}
