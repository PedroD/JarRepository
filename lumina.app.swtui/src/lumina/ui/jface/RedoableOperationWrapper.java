package lumina.ui.jface;

import lumina.kernel.Logger;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.service.log.LogService;

/**
 * Base class for redoable/undoable operations.
 */
public abstract class RedoableOperationWrapper {

	/**
	 * The operation name.
	 */
	private final String operationName;

	/**
	 * The workbench.
	 */
	private final IWorkbench workbench;

	/**
	 * Instantiates a new redoable operation wrapper.
	 * 
	 * @param name
	 *            the user-readable short name of the operation
	 * @param wb
	 *            the workbench reference
	 */
	public RedoableOperationWrapper(final String name, final IWorkbench wb) {
		operationName = name;
		this.workbench = wb;

	}

	/**
	 * Instantiates a new redoable operation wrapper.
	 * 
	 * @param name
	 *            the name
	 * @param window
	 *            the window
	 */
	public RedoableOperationWrapper(final String name,
			final IWorkbenchWindow window) {
		operationName = name;
		this.workbench = window.getWorkbench();

	}

	/**
	 * Executes the operation.
	 * <p>
	 * Descending classes are expected to implement this method.
	 * 
	 * @param monitor
	 *            the monitor a progress monitor
	 * @param info
	 *            the context
	 * @return the status information
	 */
	public abstract IStatus execute(IProgressMonitor monitor, IAdaptable info);

	/**
	 * Undoes the operation.
	 * <p>
	 * Descending classes are expected to implement this method.
	 * 
	 * @param monitor
	 *            the monitor a progress monitor
	 * @param info
	 *            the context
	 * @return the status information
	 */
	public abstract IStatus undo(IProgressMonitor monitor, IAdaptable info);

	/**
	 * Executes the operation.
	 * <p>
	 * The operation is actually executed and put in the undo history.
	 */
	public final void run() {
		final IUndoableOperation theOperation = new BaseRedoableOperation(
				operationName) {
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
				return RedoableOperationWrapper.this.execute(monitor, info);
			}

			public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
				return RedoableOperationWrapper.this.undo(monitor, info);
			}
		};

		theOperation.addContext(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		final IOperationHistory undoHistory = workbench.getOperationSupport()
				.getOperationHistory();

		try {
			/*
			 * This actually executes the operation and stacks in the undo
			 * history.
			 */
			undoHistory.execute(theOperation, null, null);
		} catch (ExecutionException e) {
			// What should we do here?
			Logger.getInstance().log(LogService.LOG_ERROR,
					"An error occurred while undoing", e); // $NON-NLS-1$
		}
	}
}
