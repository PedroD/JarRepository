package lumina.ui.views.navigation;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Unwinds the model and provides the content for the TreeViewer.
 */
public class NavigationViewContentProvider implements ITreeContentProvider {

	/**
	 * Handler for input change.
	 * 
	 * @param v
	 *            viewer
	 * @param oldInput
	 *            old input
	 * @param newInput
	 *            new input
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	/**
	 * Terminates.
	 */
	public void dispose() {
	}

	/**
	 * Returns the elements of the navigation view.
	 * <p>
	 * The returned array will only hold a single element, the current project.
	 * 
	 * @param parent
	 *            object
	 * @return object array containing the current project
	 */
	public Object[] getElements(Object parent) {
		return new Object[] { ProjectModel.getInstance().getProject() };
	}

	/**
	 * Returns the parent of a specified child.
	 * 
	 * @param child
	 *            object to get the parent from
	 * @return the object parent or null if the object is not a model item
	 */
	public Object getParent(final Object child) {
		if (ModelUtils.isModelItem(child)) {
			return ModelUtils.getParentOf((ModelItem) child);
		} else {
			return null;
		}
	}

	/**
	 * Returns the children of a specified parent.
	 * 
	 * @param parent
	 *            parent object to get the children from
	 * @return array with the parent children if the parent is a model item, or
	 *         empty array otherwise
	 */
	public Object[] getChildren(final Object parent) {
		if (ModelUtils.isModelItem(parent)) {
			return ModelUtils.getChildrenOf((ModelItem) parent);
		} else {
			return new Object[0];
		}
	}

	/**
	 * Checks if a specified parent has children.
	 * 
	 * @param parent
	 *            parent to check if it has children
	 * @return true if it has children, false otherwise
	 */
	public boolean hasChildren(final Object parent) {
		return ModelUtils.hasChildren(parent);
	}
}
