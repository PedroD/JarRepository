package lumina.ui.jface;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * A table viewer that reacts to column resizing using a
 * {@link LastColumnResizerControlAdapter}.
 */
public class EnhancedTableViewer extends TableViewer {

	public EnhancedTableViewer(final Composite parent, final int style) {
		super(parent, style);
		hookLastColumnResizer(parent);
	}

	public EnhancedTableViewer(final Composite parent) {
		super(parent);
		hookLastColumnResizer(parent);
	}

	private void hookLastColumnResizer(final Composite parent) {
		parent.addControlListener(new LastColumnResizerControlAdapter(parent,
				getTable()));
	}
}
