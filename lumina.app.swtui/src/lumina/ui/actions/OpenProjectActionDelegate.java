package lumina.ui.actions;

import java.lang.reflect.InvocationTargetException;

import lumina.Constants;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.license.LicenseLimitsExceededException;
import lumina.network.LuminaException;
import lumina.ui.dialogs.ProjectUserActionsDialogs;
import lumina.ui.jface.EnhancedFileDialog;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.perspectives.PerspectiveHelper;
import lumina.ui.swt.SimpleDialogs;

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
 * Open project action delegate.<br/>
 * Action for project open with a progress bar in a monitor window.
 * <p>
 * Useful references:
 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFace Progress Monitor Dialog Example</a>}
 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
 * </p>
 */
public class OpenProjectActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	private boolean success;

	/**
	 * Open project action with progress.
	 * <p>
	 * Useful references:
	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFacesProgressMonitorDialog</a>}
	 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">newProgressMonitorDialogShellshell.htm</a>}
	 * {@link <a href="http://www.cetic.be/internal.php3?id_article=225">Example</a>}
	 * </p>
	 */
	private class OpenProjectActionProgress implements IRunnableWithProgress {

		private String projectFile = "";
		private IWorkbenchWindow workbenchWindow = null;

		/**
		 * Constructor.
		 * 
		 * @param projectFile
		 *            project file to open
		 * @param workbenchWindow
		 *            workbench window
		 *            <p>
		 *            Useful references:
		 *            {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFace Progress Monitor Dialog Example</a>}
		 *            {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
		 *            </p>
		 */
		public OpenProjectActionProgress(final String projectFile,
				final IWorkbenchWindow workbenchWindow) {
			this.projectFile = projectFile;
			this.workbenchWindow = workbenchWindow;
		}

		/**
		 * Open project progress.
		 * 
		 * @param monitor
		 *            progress monitor
		 */
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask(
					Messages.getString("OpenProjectActionProgress.taskName"), //$NON-NLS-1$
					IProgressMonitor.UNKNOWN);
			monitor.subTask(Messages
					.getString("OpenProjectActionProgress.subTaskName1")); //$NON-NLS-1$
			try {
				final Project project = ProjectModel.getInstance().openProject(
						this.projectFile);

				final ISelectionProvider selectionProvider = SelectionUtils
						.getSelectionProvider(this.workbenchWindow);
				if (selectionProvider != null) {
					SelectionUtils.doSelectItems(new Object[] { project },
							selectionProvider);
				}

				// close the welcome screen upon opening a project
				monitor.subTask(Messages
						.getString("OpenProjectActionProgress.subTaskName2")); //$NON-NLS-1$
				PerspectiveHelper.closeWelcomePage();

				success = true;
			} catch (LicenseLimitsExceededException ex) {
				monitor.done();

				Logger.getInstance().log(LogService.LOG_ERROR,
						"License limits exceded while opening project:", ex);
				SimpleDialogs.showInfo(ex.getTitle(), ex.getMessage(), true);
			} catch (Exception ex) {
				throw new LuminaException(
						Messages.getString("OpenProjectActionDelegate.errorLoading"), //$NON-NLS-1$
						Messages.getString("OpenProjectActionDelegate.errorLoadingDetail"), //$NON-NLS-1$
						ex);
			} finally {
				monitor.done();
			}
		}
	}

	/**
	 * Runs action with progress dialog.
	 * 
	 * @param projectFile
	 *            project file
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
					ps.run(false, false, new OpenProjectActionProgress(
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
					// LuminaException by the time they get here.
					if (t instanceof LuminaException)
						Logger.getInstance().log(LogService.LOG_ERROR,
								"ERROR!!", t);
					else
						Logger.getInstance()
								.log(LogService.LOG_ERROR,
										Messages.getString("OpenProjectActionDelegate.unexpectedError"), t); //$NON-NLS-1$
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
		this.workbenchWindow = window;
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
	 * Action execution.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		// check for project changes and ask to save
		if (Capabilities.canDo(Capability.PROJECT_SAVE)) {
			ProjectUserActionsDialogs.SaveResult result = ProjectUserActionsDialogs
					.saveBefore(ProjectUserActionsDialogs.SaveReason.OPEN_PROJECT);
			if (result != ProjectUserActionsDialogs.SaveResult.PROJECT_SAVED_OK)
				return;
		}

		success = false;

		Shell shell = workbenchWindow.getShell();
		EnhancedFileDialog openDialog = new EnhancedFileDialog(shell, SWT.OPEN);
		openDialog.setFilterExtensions(new String[] {
				Constants.PROJECT_FILE_EXTENSION_FILTER,
				Constants.ALL_FILE_EXTENSIONS }); //$NON-NLS-1$
		openDialog.setFilterNames(new String[] {
				Messages.getString("Project.projectExtensions",
						Constants.APPLICATION_NAME_SHORT,
						Constants.PROJECT_FILE_EXTENSION_FILTER),
				Messages.getString("Project.allExtensions") }); //$NON-NLS-1$
		openDialog.setText(Messages
				.getString("OpenProjectActionDelegate.openProject")); //$NON-NLS-1$

		final String name = openDialog.open();

		if (name != null) {
			runProgress(name, this.workbenchWindow);
		}
	}

	/**
	 * Returns the open project action success.
	 * 
	 * @return true if the last action run() was successful, false otherwise
	 * @see OpenProjectActionDelegate#run(IAction)
	 * @see OpenProjectActionDelegate#runProgress(String, IWorkbenchWindow)
	 */
	public boolean getActionSuccess() {
		return success;
	}

}
