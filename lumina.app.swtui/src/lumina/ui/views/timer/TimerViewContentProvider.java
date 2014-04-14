package lumina.ui.views.timer;

import lumina.base.model.DeviceTimer;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Schedule;
import lumina.base.model.Task;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Unwinds the model and provides the content for the {@link TimerView}.
 */
public class TimerViewContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private static ModelItem[] getChildrenOf(final ModelItem item) {
		if (item instanceof Task) {
			return null;
		} else if (item instanceof Schedule) {
			return ModelUtils.toModelItems(((Schedule) item).getTasks());
		} else if (item instanceof DeviceTimer) {
			return ModelUtils.toModelItems(((DeviceTimer) item).getSchedules());
		} else if (item instanceof Project) {
			return ModelUtils.toModelItems(((Project) item).getTimers());
		} else {
			return null;
		}
	}

	private static ModelItem getParentOf(final ModelItem item) {
		if (item instanceof Project) {
			return null;
		} else if (item instanceof DeviceTimer) {
			return ((DeviceTimer) item).getParentProject();
		} else if (item instanceof Schedule) {
			return ((Schedule) item).getParentTimer();
		} else if (item instanceof Task) {
			return ((Task) item).getParentSchedule();
		} else {
			return null;
		}
	}

	/**
	 * Input changed.
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
	 * Returns the child elements of the given parent object.
	 * 
	 * @param parent
	 *            parent object
	 * @return array of objects containing the parent children
	 */
	public final Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/**
	 * Returns the parent of a given child object.
	 * 
	 * @param child
	 *            child object
	 * @return parent object
	 */
	public final Object getParent(final Object child) {
		if (ModelUtils.isModelItem(child)) {
			return getParentOf((ModelItem) child);
		} else {
			return null;
		}
	}

	/**
	 * Returns the child elements of the given parent object or a new project
	 * object if the parent is not an item in the current model.
	 * 
	 * @param parent
	 *            parent object
	 * @return array of objects containing the parent children
	 * @see #getElements(Object)
	 */
	public final Object[] getChildren(final Object parent) {
		if (ModelUtils.isModelItem(parent)) {
			return getChildrenOf((ModelItem) parent);
		} else {
			return new Object[] { ProjectModel.getInstance().getProject() };
		}
	}

	/**
	 * Checks if a parent object has children.
	 * 
	 * @param parent
	 *            parent object
	 * @return true if the parent object has children, false othewise
	 */
	public final boolean hasChildren(final Object parent) {
		if (parent instanceof Project) {
			return ((Project) parent).hasTimers();
		} else if (parent instanceof Schedule) {
			return ((Schedule) parent).hasTasks();
		} else if (parent instanceof DeviceTimer) {
			return ((DeviceTimer) parent).hasSchedules();
		} else {
			return false;
		}
	}

}
