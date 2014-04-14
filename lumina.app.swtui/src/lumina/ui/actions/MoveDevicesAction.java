package lumina.ui.actions;

import java.util.Map;

import lumina.base.model.Area;
import lumina.base.model.Device;
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
 * Action for device move.
 */
public class MoveDevicesAction extends Action {

	private final Device[] movedDevices;
	private final Area destinationArea;
	private final Device targetDevice;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for moving devices into a new location.
	 * 
	 * @param devices
	 *            array with devices to be moved
	 * @param to
	 *            destination area
	 * @param target
	 *            new location
	 * @param window
	 *            workbench window
	 */
	public MoveDevicesAction(final Device[] devices, final Area to,
			final Device target, final IWorkbenchWindow window) {
		super(getLabelFor(devices, to));

		if (devices == null) {
			throw new IllegalArgumentException("The device must be assigned");
		}

		movedDevices = devices;

		if (to == null) {
			throw new IllegalArgumentException(
					"The destination area must be assigned");
		}
		destinationArea = to;
		targetDevice = target;

		workbenchWindow = window;
	}

	/**
	 * Returns the label for device action.
	 * 
	 * @param devices
	 *            array with devices to which the operation applies
	 * @param to
	 *            destination area
	 * @return label with operation
	 */
	private static String getLabelFor(final Device[] devices, final Area to) {
		if (devices.length > 1) {
			return "Move devices " + to.getName();
		} else {
			return "Move device to " + to.getName();
		}
	}

	/**
	 * Execute the area move.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final Map<Area, Device[]> originalDeviceAreas = Queries
				.getOriginalDevicesByArea(movedDevices);

		IUndoableOperation moveDevice = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveDevices(movedDevices,
						destinationArea, targetDevice);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().moveDevices(originalDeviceAreas,
						targetDevice);
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		moveDevice.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			undoHistory.execute(moveDevice, null, null);
		} catch (ExecutionException e) {
			// XXX What should I do here?
		}
	}
}
