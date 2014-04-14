package lumina.ui.views;

import lumina.base.model.validators.ValidatorManager;
import lumina.qp.AggregateResult;
import lumina.qp.TableSink;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract tree view label provider.
 * <p>
 * Provides abstract class for the tree view label provider.
 */
public abstract class AbstractTreeViewLabelProvider extends LabelProvider
		implements ITableLabelProvider,
		ValidatorManager.ValidationEventListener {

	private TableSink<AggregateResult> queryResult;

	/**
	 * Defines the query result.
	 * 
	 * @param result
	 *            the query result, can be <code>null</code>
	 */
	protected void setQueryResult(final TableSink<AggregateResult> result) {
		queryResult = result;
	}

	/**
	 * Returns the query result.
	 * 
	 * @return the query result object; or <code>null</code> if not assigned
	 */
	protected TableSink<AggregateResult> getQueryResult() {
		return queryResult;
	}

	/**
	 * Returns the column image.
	 * <p>
	 * If columnt index is zero the return the element image, otherwise return
	 * <code>null</code>.
	 * 
	 * @param element
	 *            element to get image from
	 * @param columnIndex
	 *            column index
	 * @return column image
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return getImage(element);
		} else {
			return null;
		}
	}

}
