package lumina.ui.actions;

import java.util.Map;

import lumina.base.model.Area;
import lumina.base.model.Floor;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.BaseRedoableOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Handler for area deletion.
 */
public class DeleteAreasAction extends Action {

	/**
	 * Area delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final Area[] deletedAreas;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for area deletion.
	 * 
	 * @param areas
	 *            array with areas for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteAreasAction(final Area[] areas, final IWorkbenchWindow window,
			final String operationName) {
		super(getLabelFor(operationName, areas));

		if (areas == null) {
			throw new IllegalArgumentException("The areas must be assigned");
		}

		deletedAreas = areas;
		workbenchWindow = window;
	}

	/**
	 * Action for area deletion.
	 * 
	 * @param areas
	 *            array with areas for deletion
	 * @param window
	 *            window
	 */
	public DeleteAreasAction(final Area[] areas, final IWorkbenchWindow window) {
		this(areas, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for area action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param areas
	 *            array with areas to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName, Area[] areas) {
		if (areas.length > 1) {
			return operationName + " areas";
		} else {
			return operationName + " area";
		}
	}

	/**
	 * Execute the area deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final Map<Floor, Area[]> originalAreasByFloor = Queries
				.getOriginalAreasByFloor(deletedAreas);

		IUndoableOperation deleteAreas = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().deleteAreas(deletedAreas);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().addAreas(originalAreasByFloor);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		deleteAreas.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(deleteAreas, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}