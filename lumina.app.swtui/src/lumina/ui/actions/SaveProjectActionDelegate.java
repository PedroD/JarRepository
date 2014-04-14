package lumina.ui.actions;

import java.lang.reflect.InvocationTargetException;

import lumina.base.model.ProjectModel;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.network.LuminaException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.log.LogService;

/**
 * Save project action delegate.<br/>
 * Saves the project with progress bar in monitored window.
 * 
 * <p>
 * Useful references:
 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFaces Progress Monitor Dialog Example</a>}
 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DialogExamples.htm">Dialog Examples</a>}
 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
 * </p>
 * 
 * @author Fernando Martins
 */
public class SaveProjectActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	private boolean success = false;

	/**
	 * Save project action with progress.
	 * 
	 * <p>
	 * Useful references:
	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFaces Progress Monitor Dialog Example</a>}
	 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
	 * </p>
	 */
	private class SaveProjectActionProgress implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask(
					Messages.getString("SaveProjectActionProgress.taskName"), //$NON-NLS-1$ 
					IProgressMonitor.UNKNOWN);
			monitor.subTask(Messages
					.getString("SaveProjectActionProgress.subTaskName1")); //$NON-NLS-1$
			try {
				ProjectModel.getInstance().saveProject();
				success = true;
			} catch (Exception ex) {
				throw new LuminaException(
						Messages.getString("SaveProjectActionDelegate.errorSaving"), //$NON-NLS-1$
						Messages.getString("SaveProjectActionDelegate.errorSavingDetail"), ex); //$NON-NLS-1$
			} finally {
				monitor.done();
			}
		}
	}

	/**
	 * Runs action with progress dialog.
	 * 
	 * @param workbenchWdw
	 *            workbench window
	 */
	private void runProgress(final IWorkbenchWindow workbenchWdw) {
		Runnable runnable = new Runnable() {
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				try {
					ps.run(false, false, new SaveProjectActionProgress());
				} catch (InvocationTargetException ex) {
					// All exceptions that occur come wrapped in a
					// InvocationTargetException,
					// so unwrap them.
					Throwable t = ex.getCause();

					// Serious errors should be passed upward
					if (t instanceof Error && !(t instanceof AssertionError))
						throw (Error) t;

					// Normal errors should already be turned into a
					// LuminaException by
					// the time they get here.
					if (t instanceof LuminaException)
						Logger.getInstance().log(LogService.LOG_ERROR,
								"ERROR!!", t);
					else
						Logger.getInstance()
								.log(LogService.LOG_ERROR,
										Messages.getString("SaveProjectActionDelegate.unexpectedError"), t); //$NON-NLS-1$
				} catch (InterruptedException ex) {
					// never happens because cancelable=false
				}
			}
		};

		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Initialize.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Action execution.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		success = false;

		if (!Capabilities.canDo(Capability.PROJECT_SAVE))
			return;

		if (ProjectModel.getInstance().getProject().getProjectFilePath() == null) {
			// if the project doesn't already have a file (e.g., when it's new
			// and has never been saved), invoke "Save As" instead
			SaveProjectAsActionDelegate saveAsActionDelegate = new SaveProjectAsActionDelegate();
			saveAsActionDelegate.init(window);
			saveAsActionDelegate.run(action);
			success = saveAsActionDelegate.getActionSuccess();
		} else {
			runProgress(this.window);
		}
	}

	/**
	 * Terminate.
	 */
	public void dispose() {
	}

	/**
	 * Trigger for selection change.
	 * 
	 * @param action
	 *            action
	 * @param selection
	 *            selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Returns the save project action success.
	 * 
	 * @return true if the last action run() was successful, false otherwise
	 * @see SaveProjectActionDelegate#run(IAction)
	 * @see SaveProjectActionDelegate#runProgress(IWorkbenchWindow)
	 */
	public boolean getActionSuccess() {
		return success;
	}

}
