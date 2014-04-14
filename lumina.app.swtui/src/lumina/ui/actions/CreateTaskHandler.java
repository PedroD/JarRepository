package lumina.ui.actions;

import lumina.base.model.ModelUtils;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.network.LuminaException;
import lumina.ui.jface.BaseRedoableOperation;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.log.LogService;

/**
 * Handler for task creation.
 */
public class CreateTaskHandler extends AbstractHandler {

	/**
	 * Creates a task.
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
		if (!Capabilities.canDo(Capability.TIMER_EDIT_TREE))
			return null;

		final ISelection currentSelection = HandlerUtil
				.getCurrentSelection(execEvent);
		if (currentSelection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;

			final Object selectedElement = structuredSelection
					.getFirstElement();
			final Schedule enclosingSchedule = Queries
					.getAncestorSchedule(selectedElement);

			assert enclosingSchedule != null;

			final ISelectionProvider selectionProvider = HandlerUtil
					.getActivePart(execEvent).getSite().getSelectionProvider();

			IUndoableOperation createTask = new BaseRedoableOperation("") {
				private Task createdTask;

				public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
					final Task dropTarget = ModelUtils
							.getDropTargetForTask(selectedElement);
					createdTask = ProjectModel.getInstance().createTask(
							enclosingSchedule, dropTarget);

					SelectionUtils.doSelectItems(new Object[] { createdTask },
							selectionProvider);

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
					if (createdTask != null) {
						ProjectModel.getInstance().deleteTasks(
								new Task[] { createdTask });

						SelectionUtils.doSelectItems(currentSelection,
								selectionProvider);

						return org.eclipse.core.runtime.Status.OK_STATUS;
					}
					return org.eclipse.core.runtime.Status.CANCEL_STATUS;
				}
			};

			createTask.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);

			final IWorkbench workbench = PlatformUI.getWorkbench();

			final IOperationHistory undoHistory = workbench
					.getOperationSupport().getOperationHistory();

			try {
				undoHistory.execute(createTask, null, null);
			} catch (ExecutionException ex) {
				// All exceptions that occur come wrapped in a
				// ExecutionException,
				// so unwrap them.
				Throwable t = ex.getCause();

				// Serious errors should be passed upward
				if (t instanceof Error && !(t instanceof AssertionError))
					throw (Error) t;

				// Normal errors should already be turned into a LuminaException
				// by
				// the time they get here.
				if (t instanceof LuminaException)
					Logger.getInstance()
							.log(LogService.LOG_ERROR, "ERROR!!", t);
				else
					Logger.getInstance()
							.log(LogService.LOG_ERROR,
									Messages.getString("CreateTaskActionHandler.errorCreatingTask"), t); //$NON-NLS-1$
			}
		}

		return null;
	}
}
