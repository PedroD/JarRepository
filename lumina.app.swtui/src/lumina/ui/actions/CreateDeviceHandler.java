package lumina.ui.actions;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.devices.FluorescentLampDevice;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.license.LicenseLimitsExceededException;
import lumina.ui.jface.RedoableOperationWrapper;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.SimpleDialogs;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.log.LogService;

/**
 * Abstract base class that provides the enabled state, where changing the state
 * fires the HandlerEvent.
 * 
 * @since 3.3
 */
public class CreateDeviceHandler extends AbstractHandler {

	/**
	 * ID of the command we are handling.
	 */
	static final String NEW_DEVICE_COMMAND_ID = "lumina.commands.creation.newDevice";

	/**
	 * ID of the parameter.
	 */
	private static final String DEVICE_TYPE_PARAMETER = "lumina.commands.creation.newDevice.deviceType";

	/**
	 * The type of the default device type.
	 */
	private static final String DEFAULT_DEVICE_TYPE_CLASS_NAME = FluorescentLampDevice.class
			.getName();

	private String lastDeviceTypeCreatedClassName = DEFAULT_DEVICE_TYPE_CLASS_NAME;

	/**
	 * Constructor.
	 */
	public CreateDeviceHandler() {
		super();
	}

	/**
	 * Create device action execution.
	 * 
	 * @param event
	 *            new device action event
	 * @return null
	 * @throws ExecutionException
	 *             if an exception occurs during the command execution
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String deviceTypeName = event.getParameter(DEVICE_TYPE_PARAMETER);

		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			final ISelectionProvider provider = HandlerUtil
					.getActivePart(event).getSite().getSelectionProvider();

			try {
				if (deviceTypeName != null) {
					lastDeviceTypeCreatedClassName = deviceTypeName;
					createDevice(deviceTypeName, structuredSelection, provider);
				} else if (lastDeviceTypeCreatedClassName != null) {
					createDevice(lastDeviceTypeCreatedClassName,
							structuredSelection, provider);
				}
			} catch (LicenseLimitsExceededException ex) {
				Logger.getInstance().log(LogService.LOG_ERROR,
						"License limits exceded while creating device:", ex);
				SimpleDialogs.showInfo(ex.getTitle(), ex.getMessage(), true);
			}
		}

		return null;
	}

	private void createDevice(final String deviceTypeClassName,
			final IStructuredSelection currentSelection,
			final ISelectionProvider selectionProvider)
			throws LicenseLimitsExceededException {

		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return;
		if (currentSelection == null)
			return;

		ProjectModel.getInstance().checkCreateDeviceAllowed();

		final Object selectedElement = currentSelection.getFirstElement();
		final Area enclosingArea = Queries.getAncestorArea(selectedElement);

		assert enclosingArea != null;

		if (deviceTypeClassName != null) {
			final RedoableOperationWrapper createDevice = new RedoableOperationWrapper(
					"", PlatformUI.getWorkbench()) {
				private Device createdDevice;

				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					final Device dropDevice = ModelUtils
							.getDropTargetForDevice(selectedElement);
					createdDevice = ProjectModel.getInstance().createDevice(
							enclosingArea, deviceTypeClassName, dropDevice);

					SelectionUtils.doSelectItems(
							new Object[] { createdDevice }, selectionProvider);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					if (createdDevice != null) {
						ProjectModel.getInstance().deleteDevices(
								new Device[] { createdDevice });

						SelectionUtils.doSelectItems(currentSelection,
								selectionProvider);

						return org.eclipse.core.runtime.Status.OK_STATUS;
					}
					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			};

			createDevice.run();
		}
	}

}
