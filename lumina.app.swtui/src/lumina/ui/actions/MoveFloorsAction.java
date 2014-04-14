package lumina.ui.actions;

import lumina.base.model.Floor;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.RedoableOperationWrapper;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action for floor move.
 */
public class MoveFloorsAction extends Action {

	private final Floor[] movedFloors;
	private final Project destinationProject;
	private final Floor targetFloor;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving areas into a new location.
	 * 
	 * @param areas
	 *            array with areas to be moved
	 * @param to
	 *            destination project
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 */
	public MoveFloorsAction(final Floor[] areas, final Project to,
			final Floor target, final IWorkbenchWindow window) {
		super(getLabelFor(areas));

		if (areas == null) {
			throw new IllegalArgumentException("The area must be assigned");
		}
		movedFloors = areas;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination floor must be assigned");
		}

		destinationProject = to;
		targetFloor = target;
		workbenchWindow = window;
	}

	/**
	 * Returns the label for areas action.
	 * 
	 * @param floor
	 *            array with floors to which the operation applies
	 * 
	 * @return label with operation
	 */
	private static String getLabelFor(final Floor[] floor) {
		if (floor.length > 1) {
			return "Move floors";
		} else {
			return "Move floor";
		}
	}

	/**
	 * Execute the floor move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final RedoableOperationWrapper moveDevice = new RedoableOperationWrapper(
				getText(), workbenchWindow) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveFloors(movedFloors,
						destinationProject, targetFloor);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveFloors(movedFloors,
						destinationProject, null);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};
		moveDevice.run();
	}
}
