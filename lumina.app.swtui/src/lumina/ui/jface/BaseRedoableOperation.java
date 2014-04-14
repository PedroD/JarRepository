package lumina.ui.jface;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Base class for simple redoable operations.
 * <p>
 * Simple redoable just call the method
 * {@link #execute(IProgressMonitor, IAdaptable)} whenever the method
 * {@link #redo(IProgressMonitor, IAdaptable)} is issued.
 */
public abstract class BaseRedoableOperation extends AbstractOperation {

	/**
	 * Constructs a new base operation.
	 * 
	 * @param label
	 *            the labeld of the operation.
	 */
	public BaseRedoableOperation(String label) {
		super(label);
	}

	/**
	 * Redo simply re-executes. No clean-up actions are required.
	 * 
	 * @param monitor
	 *            the monitor the progress monitor. Passed down.
	 * @param info
	 *            the context info. Passed down.
	 * @return the result of Calls
	 *         {@link #execute(IProgressMonitor, IAdaptable)}.
	 * @throws ExecutionException
	 *             the execution exception
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public final IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}
}
