package lumina.ui.actions;

import lumina.base.model.Area;
import lumina.base.model.Floor;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for area creation.
 */
public class CreateAreaHandler extends AbstractHandler {

	/**
	 * Creates an area.
	 * 
	 * @param execEvent
	 *            event
	 * @return null
	 * @throws ExecutionException
	 *             not thrown
	 * @see org.eclipse.core.commands.AbstractHandler#execute(ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		if (!Capabilities.canDo(Capability.DEVICE_EDIT_TREE))
			return null;

		final ISelection currentSelection = HandlerUtil
				.getCurrentSelection(execEvent);
		if (currentSelection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;

			final Object selectedElement = structuredSelection
					.getFirstElement();
			final Project enclosingProject = Queries
					.getAncestorProject(selectedElement);

			assert enclosingProject != null;

			// TODO i18n get text in BaseRedoableOperation(getText())
			final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();

			final ISelectionProvider selectionProvider = HandlerUtil
					.getActivePart(execEvent).getSite().getSelectionProvider();

			final Floor enclosingFloor = Queries
					.getAncestorFloor(selectedElement);

			assert enclosingFloor != null;

			RedoableOperationWrapper createArea = new RedoableOperationWrapper(
					"", workbenchWindow) {
				private Area createdArea;
				private ISelection currentSelection;

				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					final Area dropTarget = ModelUtils
							.getDropTargetForArea(selectedElement);
					createdArea = ProjectModel.getInstance().createArea(
							enclosingFloor, dropTarget);
					currentSelection = workbenchWindow.getActivePage()
							.getSelection();

					SelectionUtils.doSelectItems(new Object[] { createdArea },
							selectionProvider);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					if (createdArea != null) {
						ProjectModel.getInstance().deleteAreas(
								new Area[] { createdArea });

						SelectionUtils.doSelectItems(currentSelection,
								selectionProvider);

						return org.eclipse.core.runtime.Status.OK_STATUS;
					}
					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			};

			createArea.run();
		}
		return null;
	}
}
