package lumina;

import lumina.base.model.ProjectModel;
import lumina.kernel.Logger;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.dialogs.ProjectUserActionsDialogs;
import lumina.ui.perspectives.PerspectiveHelper;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.service.log.LogService;
import org.osgi.service.prefs.BackingStoreException;

import codebase.apputils.CrashDetector;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 * <p>
 * This class is also responsible for firing the application startup and
 * shutdown events. In addition it also fires the application crash event. To
 * detect the application crash the class {@link CrashDetector} is instantiated
 * to create the lock file in the eclipse platform runtime directory.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	/**
	 * Preference key to the last open project.
	 */
	private static final String PREF_LAST_PROJECT = "last_project"; //$NON-NLS-1$

	/**
	 * Application exit confirmation dialog title.
	 */
	private static final String APPLICATION_EXIT_TITLE = Messages.getString(
			"ApplicationWorkbenchAdvisor.exit.title", Constants.PRODUCT_NAME); //$NON-NLS-1$

	/**
	 * Application exit confirmation dialog message.
	 */
	private static final String APPLICATION_EXIT_MESSAGE_KEY = "ApplicationWorkbenchAdvisor.exit.message"; //$NON-NLS-1$

	/**
	 * Path of the project file to open at startup, if any.
	 */
	private String projectFileToOpen = null;

	/**
	 * Reference to the crash detector object.
	 */
	private CrashDetector crashDetector;

	/**
	 * Caches whether the application has last started up cleanly.
	 */
	private boolean startedUpCleanly;

	/**
	 * Obtains the directory where the lock file for the crash detector is to be
	 * placed.
	 * 
	 * @return the run-time directory path.
	 */
	private String getCrashLockFileDir() {
		return Platform.getLocation().toOSString();
	}

	/**
	 * Saves the preferences to disk.
	 * 
	 * @param prefs
	 *            the preferences to be saved.
	 */
	private void savePreferences(final IEclipsePreferences prefs) {
		try {
			prefs.flush();
		} catch (BackingStoreException ex) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Error saving preferences", ex); //$NON-NLS-1$
		}
	}

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PerspectiveHelper.getDefaultPerspectiveId();
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		// tell eclipse to save workbench state when it quits
		// (things like window size, view layout, etc.)
		configurer.setSaveAndRestore(true);
	}

	@Override
	public void preStartup() {
		super.preStartup();

		final IEclipsePreferences pref = Preferences.getRootNode();

		// Initializes the crash detector
		final String crashLockFileDir = getCrashLockFileDir();
		crashDetector = new CrashDetector(crashLockFileDir);
		startedUpCleanly = crashDetector.wasClean();

		// Creates the crash detector lock file
		final boolean startingClean = crashDetector.startup();
		if (!startingClean) {
			Logger.getInstance().log(LogService.LOG_INFO,
					"The crash detector could not create the lock file!"); // $NON-NLS-1$
		}

		// Get the last open project from the preferences but don't try to
		// open it yet, leave that for postStartup()
		final String lastProjFile = pref.get(PREF_LAST_PROJECT, null);

		if (lastProjFile != null && new java.io.File(lastProjFile).isFile()) {
			projectFileToOpen = lastProjFile;
		}
	}

	@Override
	public void postStartup() {
		super.postStartup();

		// Complains that the last time the application did not
		// shutdown cleanly
		if (!startedUpCleanly) {
			Logger.getInstance().log(LogService.LOG_INFO,
					"Last shutdown was not a clean one!"); // $NON-NLS-1$

			// Displays a dialog message on startup
			// if the application has crashed.
			ProjectUserActionsDialogs.informUncleanShutdown();
			ProjectModel.getInstance().applicationCrash();
		}

		// Inform about the application startup
		// before the project load
		ProjectModel.getInstance().applicationStartup();

		// Try to reopen the same project the user had open. If that fails,
		// just create an empty project and display the welcome page
		try {
			boolean openSuccess = false;
			if (projectFileToOpen != null) {
				try {
					ProjectModel.getInstance().openProject(projectFileToOpen);
					openSuccess = true;
				} catch (Exception ex) {
					Logger.getInstance()
							.log(LogService.LOG_ERROR,
									"Failed to open last open project: " + projectFileToOpen, ex); //$NON-NLS-1$
				}
			}

			if (!openSuccess) {
				ProjectModel.getInstance().newProject();
				// PerspectiveHelper.openWelcomePage();
			} else {
				// PerspectiveHelper.closeWelcomePage();
			}
		} catch (Throwable t) {
			/*
			 * This means that an unhandled error has occurred before the main
			 * event loop. Lets log it and get out.
			 */
			eventLoopException(t);
		}
	}

	/**
	 * If it is not an emergency exit, a closing confirmation will be asked to
	 * the user and, if necessary project save changes will be requested.
	 * 
	 * @return <code>true</code>, if successful; <code>false</code> otherwise
	 */
	@Override
	public boolean preShutdown() {
		final boolean emergencyExit = getWorkbenchConfigurer()
				.emergencyClosing();

		if (!emergencyExit) {
			// Not emergency exit
			final lumina.base.model.Project currentProject = ProjectModel
					.getInstance().getProject();
			// TODO: Energy Meter
			// if (currentProject.getDatabase() != null
			// || currentProject.hasTimers()) {
			// // Project stuff exists, request user exit confirmation
			// final String confirmMessage = Messages.getString(
			// APPLICATION_EXIT_MESSAGE_KEY, Constants.PRODUCT_NAME,
			//						currentProject.getName()); //$NON-NLS-1$
			// if (!SimpleDialogs.showQuestion(APPLICATION_EXIT_TITLE,
			// confirmMessage, true)) {
			// // User canceled.
			// return false;
			// }
			// }

			if (Capabilities.canDo(Capability.PROJECT_SAVE)) {
				// User can save project changes, request project save if needed
				ProjectUserActionsDialogs.SaveResult result = ProjectUserActionsDialogs
						.saveBefore(ProjectUserActionsDialogs.SaveReason.APPLICATION_EXIT);
				if (result != ProjectUserActionsDialogs.SaveResult.PROJECT_SAVED_OK) {
					return false;
				}
			}
		}

		// Inform of the application shutdown before disposing the views
		ProjectModel.getInstance().applicationShutdown();

		return super.preShutdown();
	}

	@Override
	public void postShutdown() {
		super.postShutdown();

		// Remember the currently open project
		IEclipsePreferences pref = Preferences.getRootNode();
		lumina.base.model.Project project = ProjectModel.getInstance()
				.getProject();
		if (project != null && project.getProjectFilePath() != null) {
			pref.put(PREF_LAST_PROJECT, project.getProjectFilePath());
		} else {
			pref.put(PREF_LAST_PROJECT, ""); //$NON-NLS-1$
		}
		savePreferences(pref);

		// Removes the crash detector lock file
		final boolean shuttingdownClean = crashDetector.shutdown();
		if (!shuttingdownClean) {
			Logger.getInstance().log(LogService.LOG_INFO,
					"Crash detector could not remove the lock file"); // $NON-NLS-1$
		}
	}

	@Override
	public void eventLoopException(Throwable exception) {
		// Errors (other than assertion failures) are considered fatal and will
		// cause
		// the application to exit. We look for Error instances anywhere in the
		// exception chain as they may have been wrapped inadvertently (SWT is
		// known
		// to do that sometimes)

		boolean fatal = false;
		for (Throwable t = exception; t != null; t = t.getCause()) {
			if (t instanceof NullPointerException)
				fatal = true;

			if (t instanceof Error && !(t instanceof AssertionError))
				fatal = true;
		}

		if (fatal) {
			Logger.getInstance()
					.log(LogService.LOG_ERROR,
							Messages.getString("ApplicationWorkbenchAdvisor.fatalError"), //$NON-NLS-1$
							exception);

			// initiate an emergency shutdown
			getWorkbenchConfigurer().emergencyClose();
		} else {
			Logger.getInstance()
					.log(LogService.LOG_ERROR,
							Messages.getString("ApplicationWorkbenchAdvisor.unhandledEx"), exception); //$NON-NLS-1$
		}
	}
}
