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
 * Handler for adding devices to the plan view.
 */
public class AddToBluePrintHandler extends AbstractHandler {

	public static final void run(final IWorkbenchWindow window,
			final Device[] devices,
			final org.eclipse.swt.graphics.Point[] dropLocations,
			final ISelectionProvider selectionProvider) {
		final String operationName = "";

		final RedoableOperationWrapper addToBlueprint = new RedoableOperationWrapper(
				operationName, window) {
			private ISelection currentSelection;

			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				currentSelection = window.getActivePage().getSelection();

				ProjectModel.getInstance().addToPlan(devices, dropLocations);

				/*
				 * /!\ Because of some strange reason selecting the items is not
				 * sufficient for forcing the re-evaluation of the actions when
				 * addToPlan is triggered by a drop action.
				 */
				SelectionUtils.doSelectItems(currentSelection,
						selectionProvider);

				/*
				 * Force the re-evaluation to ensure that if we right-click on
				 * the newly add item the item 'Remove from plan' option will be
				 * available. See issue #286.
				 */
				SelectionUtils.doUpdateSelectionSourceProvider(window,
						currentSelection);

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				ProjectModel.getInstance().removeFromPlan(devices);

				SelectionUtils.doSelectItems(currentSelection,
						selectionProvider);

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
