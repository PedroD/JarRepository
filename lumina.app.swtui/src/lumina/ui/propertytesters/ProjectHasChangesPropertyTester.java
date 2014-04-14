package lumina.ui.propertytesters;

import lumina.base.model.ProjectModel;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Checks if the project has been changes since the last save operation.
 */
public class ProjectHasChangesPropertyTester extends PropertyTester {

	/**
	 * Tests if a project has changes.
	 * 
	 * @param receiver
	 *            object
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return true if project has changes, false otherwise
	 */
	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		return ProjectModel.getInstance().isProjectDirty();
	}

}
