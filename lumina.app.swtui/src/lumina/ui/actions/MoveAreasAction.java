package lumina.ui.actions;

import java.util.Map;

import lumina.base.model.Area;
import lumina.base.model.Floor;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.RedoableOperationWrapper;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action for area move.
 */
public class MoveAreasAction extends Action {
	private final Area[] movedAreas;
	private final Floor destinationFloor;
	private final Area targetArea;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving areas into a new location.
	 * 
	 * @param areas
	 *            array with areas to be moved
	 * @param to
	 *            destination floor
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 * 
	 */
	public MoveAreasAction(final Area[] areas, final Floor to,
			final Area target, final IWorkbenchWindow window) {
		super(getLabelFor(areas, to));

		if (areas == null) {
			throw new IllegalArgumentException("The area must be assigned");
		}
		movedAreas = areas;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination floor must be assigned");
		}

		destinationFloor = to;
		targetArea = target;
		workbenchWindow = window;
	}

	/**
	 * Returns the label for area action.
	 * 
	 * @param areas
	 *            array with areas to which the operation applies
	 * @param to
	 *            destination floor
	 * @return label with operation
	 */
	private static String getLabelFor(final Area[] areas, final Floor to) {
		if (areas.length > 1) {
			return "Move areas to " + to.getName();
		} else {
			return "Move area " + to.getName();
		}
	}

	/**
	 * Execute the area move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final Map<Floor, Area[]> originalAreaFloors = Queries
				.getOriginalAreasByFloor(movedAreas);

		final RedoableOperationWrapper moveDevice = new RedoableOperationWrapper(
				getText(), workbenchWindow) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveAreas(movedAreas,
						destinationFloor, targetArea);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveAreas(originalAreaFloors,
						targetArea);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		moveDevice.run();
	}
}
