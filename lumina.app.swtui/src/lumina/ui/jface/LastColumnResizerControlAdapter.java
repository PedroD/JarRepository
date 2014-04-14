package lumina.ui.jface;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A {@link ControlAdapter control adapter} that resizes the last visibile
 * column.
 * <p>
 * This is very important workarround that prevents the last column to be get
 * ugly when resizing of a table takes place.
 */
public class LastColumnResizerControlAdapter extends ControlAdapter {

	/**
	 * A reference to the parent.
	 */
	private final Composite parent;

	/**
	 * The table.
	 */
	private final Table table;

	/**
	 * Gets the last column.
	 * 
	 * @param table
	 *            the table object
	 * @return the last column or <code>null</code> if the table does not
	 *         contain at least one column.
	 */
	private static TableColumn getLastColumn(final Table table) {
		final int colCount = table.getColumnCount();
		if (colCount >= 1) {
			return table.getColumn(colCount - 1);
		} else {
			return null;
		}
	}

	/**
	 * Gets the column before the last.
	 * 
	 * @param table
	 *            the table object
	 * @return the penultimate column or <code>null</code> if the table does not
	 *         have at least 2 columns.
	 */
	private static TableColumn getPenultimateColumn(final Table table) {
		final int colCount = table.getColumnCount();
		if (colCount >= 2) {
			return table.getColumn(colCount - 2);
		} else {
			return null;
		}
	}

	/**
	 * Instantiates a new last column resizer control adapter.
	 * 
	 * @param top
	 *            the parent composite object
	 * @param subject
	 *            the table object being controlled subject
	 */
	public LastColumnResizerControlAdapter(final Composite top,
			final Table subject) {
		parent = top;
		table = subject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.
	 * events. ControlEvent)
	 */
	public final void controlResized(final ControlEvent e) {
		final Rectangle area = parent.getParent().getClientArea();

		int width = area.width + table.getBorderWidth();

		if (table.getVerticalBar().isVisible()) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			final Point vBarSize = table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		final Point oldSize = table.getSize();

		final TableColumn lastColumn = getLastColumn(table);
		if (lastColumn != null) {
			final TableColumn penultimate = getPenultimateColumn(table);
			final boolean onlyOneColumn = penultimate == null;

			final int columnInset = 2;

			if (onlyOneColumn) {
				table.setSize(area.width, area.height);
				lastColumn.setWidth(width + columnInset);
			} else {
				if (oldSize.x > area.width) {
					// table is getting smaller
					lastColumn.setWidth((width + columnInset)
							- penultimate.getWidth());
					table.setSize(area.width, area.height);
				} else {
					// table is getting bigger
					table.setSize(area.width, area.height);
					lastColumn.setWidth((width + columnInset)
							- penultimate.getWidth());
				}
			}
		}
	}
}
