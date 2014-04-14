package lumina;

import lumina.base.model.ProjectModel;
import lumina.kernel.Logger;
import lumina.license.License;
import lumina.license.LicenseValidationException;
import lumina.license.UserMode;
//import lumina.energymanager.db.Database;
import lumina.ui.celleditors.SWTPropertyEditorFactories;
import lumina.ui.dialogs.LicenseWizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.log.LogService;

/**
 * This class controls all aspects of the application's execution.
 */
public class Application implements IApplication {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(final IApplicationContext context) throws Exception {
		/*
		 * /!\ Eclipse bug workaround: This corrects the Toolkit.loadLibraries()
		 * crash problem in MacOS X (used by java.awt.Dimension).
		 */
		java.awt.Toolkit.getDefaultToolkit();

		/*
		 * Check for a running instance
		 */
		try {
			if (!AppInstanceManager.tryStart()) {
				Logger.getInstance().log(LogService.LOG_ERROR,
						"Another instance is already running. Exiting.");
				return IApplication.EXIT_OK;
			}
			AppInstanceManager.registerCallback(new Runnable() {
				public void run() {
					// Unminimize and focus the application window in response
					// to
					// the user having tried to start another instance.
					final IWorkbench wb = PlatformUI.getWorkbench();
					if (wb == null)
						return;

					final Display display = wb.getDisplay();
					display.syncExec(new Runnable() {
						public void run() {
							if (display.isDisposed())
								return;
							if (wb.getActiveWorkbenchWindow() == null)
								return;
							final Shell s = wb.getActiveWorkbenchWindow()
									.getShell();
							s.setVisible(true);
							s.setActive();
							s.setFocus();
							s.setMinimized(false);
						}
					});
				}
			});
		} catch (Exception ex) {
			// catch exceptions just in case
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Chech for running instance failed.", ex);
		}

		/*
		 * Init license
		 */
		try {

			License.initializeInstance();
		} catch (LicenseValidationException ex) {
			// Create a display and shell (the workbench shell doesn't exist at
			// this point so we can't use it)
			Display display = new Display();
			Shell shell = new Shell(display);

			// Create the wizard
			LicenseWizard wizard = new LicenseWizard();

			// Show the wizard dialog
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.setBlockOnOpen(true);
			dialog.open();

			// cleanup
			shell.dispose();
			display.dispose();

			if (wizard.isLicenseInstalled()) {
				// the license file was installed, reread the license
				try {
					License.initializeInstance();
				} catch (LicenseValidationException ex2) {
					// this shouldn't happen because the LicenseWizard validates
					// the
					// license before installing...
					Logger.getInstance()
							.log(LogService.LOG_ERROR,
									"Newly installed license failed to validate. Exiting program.",
									ex2);
					return IApplication.EXIT_OK;
				}
			} else {
				// the license file was not installed, quit
				return IApplication.EXIT_OK;
			}
		}

		// TODO: Port to OSGi
		SWTPropertyEditorFactories.registerPropertyEditorFactories();

		/*
		 * Initialize the database system
		 */
		try {
			// set the derby home to a sub-directory of the RCP working
			// directory
			String workDir = Platform.getLocation().toString();
			String derbyHome = new java.io.File(workDir, "derby").toString();
			// TODO: Energy Meter
			// Database.globalInit(derbyHome);
		} catch (Exception ex) {
			// Create a display just to show the error dlg
			Display display = new Display();
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Database initialization failed. Exiting program.", ex);
			display.dispose();

			// quit
			return IApplication.EXIT_OK;
		}

		/*
		 * Init user mode
		 */
		UserMode.initialize();

		/*
		 * Application main loop
		 */
		final Display display = PlatformUI.createDisplay();
		try {
			if (display != null) {
				final int res = PlatformUI.createAndRunWorkbench(display,
						new ApplicationWorkbenchAdvisor());
				if (res == PlatformUI.RETURN_RESTART) {
					return IApplication.EXIT_RESTART;
				}
			}
		} finally {
			if (display != null && !display.isDisposed()) {
				display.dispose();
			}
		}

		/*
		 * Close the open project
		 */
		ProjectModel.getInstance().closeProject();

		/*
		 * Shutdown the database
		 */
		// TODO: Energy Meter
		// Database.globalShutdown();

		return IApplication.EXIT_OK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;

		// The call to syncExec will not return until the workbench is closed.
		// This will force the application to exit and the call to
		// createAndRunWorkbench to return

		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
