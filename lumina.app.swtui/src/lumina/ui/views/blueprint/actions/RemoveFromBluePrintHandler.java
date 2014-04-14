package lumina.ui.views.blueprint.actions;

import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.RedoableOperationWrapper;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler for removing devices to from plan view.
 */
public class RemoveFromBluePrintHandler extends AbstractHandler {

	public static final void run(final IWorkbenchWindow window,
			final Device[] devices,
			final org.eclipse.swt.graphics.Point eventPoint,
			final ISelectionProvider selectionProvider) {
		final String operationName = "";

		final ISelection selection = selectionProvider.getSelection();
		final RedoableOperationWrapper addToBlueprint = new RedoableOperationWrapper(
				operationName, window) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().removeFromPlan(devices);

				/*
				 * reselect the items to force the re-evaluation of the property
				 * testers
				 */
				if (selectionProvider != null) {
					selectionProvider.setSelection(selection);
				}

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().addToPlan(devices);

				/*
				 * reselect the items to force the re-evaluation of the property
				 * testers
				 */
				if (selectionProvider != null) {
					selectionProvider.setSelection(selection);
				}

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};

		addToBlueprint.run();
	}

	@Override
	public final Object execute(ExecutionEvent execEvent)
			throws ExecutionException {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_PROPERTIES)) {
			return null;
		}

		final Object[] selections = SelectionUtils.getSelection(HandlerUtil
				.getCurrentSelection(execEvent));

		if (selections != null) {
			final ModelItem[] items = ModelUtils.toModelItems(selections);
			final Device[] devicesToAdd = ModelUtils.getAllDevices(items);

			final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();

			final ISelectionProvider provider = HandlerUtil
					.getActivePart(execEvent).getSite().getSelectionProvider();

			run(workbenchWindow, devicesToAdd, null, provider);
		}

		return null;
	}
}
