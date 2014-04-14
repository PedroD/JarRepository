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
 * Handler for device deletion.
 */
public class DeleteDevicesAction extends Action {

	/**
	 * Device delete action name.
	 */
	static final String DEFAULT_ACTION_NAME = "Delete";

	private final Device[] deletedDevices;
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Action for devices deletion.
	 * 
	 * @param devices
	 *            array with devices for deletion
	 * @param window
	 *            window
	 * @param operationName
	 *            operation name
	 */
	public DeleteDevicesAction(final Device[] devices,
			final IWorkbenchWindow window, final String operationName) {
		super(getLabelFor(operationName, devices));
		setId("Lumina.MoveDeviceAction");

		if (devices == null) {
			throw new IllegalArgumentException("The device must be assigned");
		}

		deletedDevices = devices;
		workbenchWindow = window;
	}

	/**
	 * Action for device deletion.
	 * 
	 * @param devices
	 *            array with devices for deletion
	 * @param window
	 *            window
	 */
	public DeleteDevicesAction(final Device[] devices,
			final IWorkbenchWindow window) {
		this(devices, window, DEFAULT_ACTION_NAME);
	}

	/**
	 * Returns the label for device action.
	 * 
	 * @param operationName
	 *            operation name
	 * @param device
	 *            array with devices to which the operation applies
	 * @return label with operation
	 */
	private static String getLabelFor(final String operationName,
			Device[] device) {
		if (device.length > 1) {
			return operationName + " devices";
		} else {
			return operationName + " device";
		}
	}

	/**
	 * Execute the device deletion.
	 */
	public void run() {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;

		final Map<Area, Device[]> originalDevicesByArea = Queries
				.getOriginalDevicesByArea(deletedDevices);

		IUndoableOperation moveDevice = new BaseRedoableOperation(getText()) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().deleteDevices(deletedDevices);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().addDevices(originalDevicesByArea,
						null);
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
