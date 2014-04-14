package lumina.ui.actions;

import java.lang.reflect.InvocationTargetException;

import lumina.Constants;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.network.LuminaException;
import lumina.ui.jface.EnhancedFileDialog;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.log.LogService;

/**
 * Save project as action delegate.<br/>
 * Saves the project with a specific given name using progress bar in monitored
 * window.
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
public class SaveProjectAsActionDelegate implements
		IWorkbenchWindowActionDelegate {

	/**
	 * The workbench window. Can be <code>null</code>.
	 */
	private IWorkbenchWindow window;

	/**
	 * Set to <code>true</code> if the save operation was carried out
	 * successfully.
	 */
	private boolean success;

	/**
	 * Save project as action with progress.
	 * 
	 * <p>
	 * Useful references:
	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFaces Progress Monitor Dialog Example</a>}
	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DialogExamples.htm">Dialog Examples</a>}
	 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
	 * </p>
	 */
	private class SaveProjectActionProgress implements IRunnableWithProgress {

		private String projectFile = "";

		private IWorkbenchWindow workbenchWindow;

		/**
		 * Constructor.
		 * 
		 * @param projectFile
		 *            project file to open
		 * @param workbenchWindow
		 *            workbench window
		 */
		public SaveProjectActionProgress(final String projectFile,
				final IWorkbenchWindow workbenchWindow) {
			this.projectFile = projectFile;
			this.workbenchWindow = workbenchWindow;
		}

		/**
		 * Runs action with progress dialog.
		 * 
		 * @param monitor
		 *            progress monitor
		 */
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask(
					Messages.getString("SaveProjectActionProgress.taskName"), //$NON-NLS-1$
					IProgressMonitor.UNKNOWN);
			monitor.subTask(Messages
					.getString("SaveProjectActionProgress.subTaskName1")); //$NON-NLS-1$
			try {
				if (!this.projectFile
						.endsWith(Constants.PROJECT_FILE_EXTENSION)) {
					this.projectFile += Constants.PROJECT_FILE_EXTENSION;
				}

				final Project project = ProjectModel.getInstance()
						.saveProjectAs(this.projectFile);
				final ISelectionProvider selectionProvider = SelectionUtils
						.getSelectionProvider(this.workbenchWindow);
				if (selectionProvider != null) {
					SelectionUtils.doSelectItems(new Object[] { project },
							selectionProvider);
				}

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
	 * @param projectFile
	 *            project file name
	 * @param workbenchWdw
	 *            workbench window
	 */
	private void runProgress(final String projectFile,
			final IWorkbenchWindow workbenchWdw) {
		Runnable runnable = new Runnable() {
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				try {
					ps.run(false, false, new SaveProjectActionProgress(
							projectFile, workbenchWdw));
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
	public void init(final IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Action execution.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		if (!Capabilities.canDo(Capability.PROJECT_SAVE))
			return;

		success = false;

		final Shell shell = Display.getCurrent().getActiveShell();
		if (shell != null) {
			/*
			 * Create the save dialog
			 */
			final EnhancedFileDialog saveDialog = new EnhancedFileDialog(shell,
					SWT.SAVE);
			saveDialog.setText(Messages
					.getString("SaveProjectActionDelegate.saveProjectAs")); //$NON-NLS-1$

			final String defaultName = ProjectModel.getInstance().getProject()
					.getFileName()
					+ Constants.PROJECT_FILE_EXTENSION; //$NON-NLS-1$
			saveDialog.setFileName(defaultName);
			saveDialog.setFilterExtensions(new String[] {
					Constants.PROJECT_FILE_EXTENSION_FILTER,
					Constants.ALL_FILE_EXTENSIONS }); //$NON-NLS-1$
			saveDialog.setFilterNames(new String[] {
					Messages.getString("Project.projectExtensions",
							Constants.APPLICATION_NAME_SHORT,
							Constants.PROJECT_FILE_EXTENSION_FILTER),
					Messages.getString("Project.allExtensions") }); //$NON-NLS-1$

			final String path = saveDialog.open();

			if (path != null && window != null) {
				runProgress(path, this.window);
			}
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
	 * @see SaveProjectAsActionDelegate#run(IAction)
	 * @see SaveProjectAsActionDelegate#runProgress(String, IWorkbenchWindow)
	 */
	public boolean getActionSuccess() {
		return success;
	}
}
