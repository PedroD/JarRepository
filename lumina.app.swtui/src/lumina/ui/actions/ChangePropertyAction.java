package lumina.ui.actions;

import lumina.api.properties.IProperty;
import lumina.base.model.ModelItem;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.ui.jface.RedoableOperationWrapper;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.part.ViewPart;

/**
 * Changes the value of a property.
 * <p>
 * This action is only executed is the new value is not equal to the existing
 * value for that property. This is important to prevent the generation of dummy
 * undo/redo action that would confuse the user.
 * 
 * @author Paulo Carreira
 */
public class ChangePropertyAction extends Action {
	private static final String ID = "Lumina.ChangePropertyAction";

	private final ModelItem changedObject;

	private final IProperty changeProperty;

	private final Object newValue;

	private final ViewPart viewPart;

	/**
	 * Constructor for property action change.
	 * 
	 * @param changed
	 *            model item
	 * @param property
	 *            property
	 * @param value
	 *            property value
	 * @param view
	 *            view part
	 */
	public ChangePropertyAction(final ModelItem changed,
			final IProperty property, final Object value, final ViewPart view) {
		super(getLabelFor(changed, property));
		setId(ID);
		changedObject = changed;
		changeProperty = property;
		newValue = value;
		viewPart = view;
	}

	private static String getLabelFor(final Object changed,
			final IProperty property) {
		if (changed != null) {
			final String msg = "Change "
					+ Queries.getObjectName(changed).toLowerCase() + " "
					+ property.getName().toLowerCase();
			return msg;
		} else {
			return "Property change";
		}
	}

	/**
	 * Action execution.
	 */
	public void run() {
		final Object oldValue = changeProperty.getValue();

		if (true) { // !changeProperty.represents(newValue)) {
			RedoableOperationWrapper alterProperty = new RedoableOperationWrapper(
					getText(), viewPart.getSite().getWorkbenchWindow()
							.getWorkbench()) {
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().changeProperty(changedObject,
							changeProperty, newValue);
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					ProjectModel.getInstance().changeProperty(changedObject,
							changeProperty, oldValue);
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};

			alterProperty.run();
		}
	}
}
