package lumina.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lumina.kernel.Logger;
import lumina.network.LuminaException;
import lumina.ui.swt.SimpleDialogs;

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

import codebase.os.SysUtil;

/**
 * Action for support log generation.
 * 
 * @author Fernando Martins
 */
public class GenerateSupportLogActionDelegate implements
		IWorkbenchWindowActionDelegate {

	/**
	 * Methods to format dates and date components of Calendar objects.
	 */
	public static final class SimpleCalendar {

		/**
		 * Prevent instantiation.
		 */
		private SimpleCalendar() {
		}

		/**
		 * Returns the date as a formated string.
		 * 
		 * @param calendar
		 *            calendar
		 * @param separator
		 *            date separator
		 * @return date YYYY<separator>MM<separator>DD
		 */
		public static String getDate(final Calendar calendar,
				final String separator) {
			StringBuffer result = new StringBuffer();

			result.append(calendar.get(Calendar.YEAR));
			result.append(separator);
			result.append(calendar.get(Calendar.MONTH));
			result.append(separator);
			result.append(calendar.get(Calendar.DAY_OF_MONTH));

			return result.toString();
		}

		/**
		 * Returns the date as a formated string.
		 * 
		 * @param calendar
		 *            calendar
		 * @return date YYYY-MM-DD
		 */
		public static String getDate(final Calendar calendar) {
			return getDate(calendar, "-");
		}

		/**
		 * Returns the time as a formated string.
		 * 
		 * @param calendar
		 *            calendar
		 * @param separator
		 *            time separator
		 * @param seconds
		 *            indicates if seconds should be included
		 * @return time as a formated string HH<separated>MM[<separated>SS]
		 */
		public static String getTime(final Calendar calendar,
				final String separator, final boolean seconds) {
			StringBuffer result = new StringBuffer();

			result.append(calendar.get(Calendar.HOUR_OF_DAY));
			result.append(separator);
			result.append(calendar.get(Calendar.MINUTE));
			if (seconds) {
				result.append(separator);
				result.append(calendar.get(Calendar.SECOND));
			}

			return result.toString();
		}

		/**
		 * Returns the time as a formated string.
		 * 
		 * @param calendar
		 *            calendar
		 * @param seconds
		 *            indicates if seconds should be included
		 * @return time as a formated string HH:MM[:SS]
		 */
		public static String getTime(final Calendar calendar,
				final boolean seconds) {
			return getTime(calendar, ":", seconds);
		}
	};

	private IWorkbenchWindow workbenchWindow;

	private boolean success;

	private static final String TASK_NAME = Messages
			.getString("GenerateSupportLogActionProgress.taskName"); //$NON-NLS-1$

	private static final String SUB_TASK_NAME1 = Messages
			.getString("GenerateSupportLogActionProgress.subTaskName1"); //$NON-NLS-1$

	private static final String SUB_TASK_NAME2 = Messages
			.getString("GenerateSupportLogActionProgress.subTaskName2"); //$NON-NLS-1$

	private static final String SUB_TASK_NAME3 = Messages
			.getString("GenerateSupportLogActionProgress.subTaskName3"); //$NON-NLS-1$

	/**
	 * Generate support log file.
	 * <p>
	 * See http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/
	 * JFacesProgressMonitorDialog.htm,
	 * http://www.java2s.com/Code/JavaAPI/org.eclipse
	 * .jface.dialogs/newProgressMonitorDialogShellshell.htm,
	 * http://www.cetic.be/internal.php3?id_article=225
	 */
	private class GenerateSupportLogActionProgress implements
			IRunnableWithProgress {

		private static final int MAX_COMPRESSION = 9;

		private static final int DEFAULT_READ_BUFFER_SIZE = 1024;

		private static final String SUPPORT_LOG_FILE_PREFIX = "SupportLog-"; //$NON-NLS-1$
		private String[] supportLogFiles;
		private String destinationPath = "";

		/**
		 * Constructor.
		 * 
		 * @param logFiles
		 *            log files to make available
		 * @param destinationPath
		 *            path where the log file should be available
		 * @param workbenchWindow
		 *            workbench window
		 */
		public GenerateSupportLogActionProgress(final String[] logFiles,
				final String destinationPath,
				final IWorkbenchWindow workbenchWindow) {
			this.supportLogFiles = logFiles;
			this.destinationPath = destinationPath;
		}

		/**
		 * Action execution with progress.
		 * 
		 * @param monitor
		 *            process monitor
		 */
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask(TASK_NAME, this.supportLogFiles.length);
			monitor.subTask(SUB_TASK_NAME1);

			final GregorianCalendar timestamp = new GregorianCalendar();
			StringBuffer supportLogFile = new StringBuffer();
			StringBuffer simpleLogFileName = new StringBuffer();

			simpleLogFileName.append(SUPPORT_LOG_FILE_PREFIX);
			simpleLogFileName.append(SimpleCalendar.getDate(timestamp, ""));
			simpleLogFileName.append(SimpleCalendar.getTime(timestamp, "",
					false));
			simpleLogFileName.append(".zip");

			supportLogFile.append(this.destinationPath);
			if (!this.destinationPath.endsWith(File.pathSeparator)) {
				supportLogFile.append(File.separator);
			}
			supportLogFile.append(simpleLogFileName.toString());

			try {
				// Create a buffer for reading the files
				byte[] buf = new byte[DEFAULT_READ_BUFFER_SIZE];

				try {
					ZipOutputStream out = new ZipOutputStream(
							new FileOutputStream(supportLogFile.toString()));
					out.setLevel(MAX_COMPRESSION);

					monitor.subTask(SUB_TASK_NAME2);
					for (int i = 0; i < this.supportLogFiles.length; i++) {
						final int fileNameStart = this.supportLogFiles[i]
								.lastIndexOf(File.separator) + 1;
						FileInputStream in = new FileInputStream(
								this.supportLogFiles[i]);

						out.putNextEntry(new ZipEntry(this.supportLogFiles[i]
								.substring(fileNameStart,
										this.supportLogFiles[i].length())));

						int len;
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}

						out.closeEntry();
						in.close();
						monitor.worked(1);
					}

					monitor.subTask(SUB_TASK_NAME3);
					out.close();
					success = true;
				} catch (IOException e) {
					Logger.getInstance().log(LogService.LOG_ERROR, "ERROR", e);
					success = false;
				}

				if (success) {
					String message;
					if (SysUtil.getOperatingSystem() == SysUtil.OS.WINDOWS) {
						message = Messages
								.getString(
										"GenerateSupportLogActionProgress.dialog.information.desktop",
										simpleLogFileName.toString());
					} else {
						message = Messages
								.getString(
										"GenerateSupportLogActionProgress.dialog.information",
										supportLogFile.toString());
					}

					SimpleDialogs
							.showInfo(
									Messages.getString("GenerateSupportLogActionProgress.dialog.title"),
									message, true);
				} else {
					SimpleDialogs.showInfo("Fail", supportLogFile.toString(),
							true);
				}

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
	 * @param logFiles
	 *            support log files to make available
	 * @param destinationPath
	 *            path where the log file should be available
	 * @param workbenchWdw
	 *            workbench window
	 */
	private void runProgress(final String[] logFiles,
			final String destinationPath, final IWorkbenchWindow workbenchWdw) {

		Runnable runnable = new Runnable() {
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				try {
					ps.run(false, false, new GenerateSupportLogActionProgress(
							logFiles, destinationPath, workbenchWdw));
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
										Messages.getString("GenerateSupportLogActionDelegate.unexpectedError"), t); //$NON-NLS-1$
				} catch (InterruptedException ex) {
					// never happens because cancelable=false
					success = false;
				}
			}
		};

		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Initializes the support log generation action.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	/**
	 * Executes the support log generation.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		// TODO: update the file and location for production mode
		// System.getProperties().list(System.out);
		final String outputPath = SysUtil.getDesktopPath();
		final String logFile = System.getProperty("osgi.logfile");
		/*
		 * String logFile = System.getProperty("osgi.instance.area");
		 * Logger.getInstance().log(LogService.LOG_DEBUG,outputPath);
		 * 
		 * if (logFile.startsWith("file:/")) { logFile =
		 * logFile.split("file:/")[1]; } Logger.getInstance().log(LogService.LOG_DEBUG,logFile);
		 */

		runProgress(new String[] { logFile }, outputPath, this.workbenchWindow);

	}

	/**
	 * Terminate.
	 */
	public void dispose() {
	}

	/**
	 * Trigger for selection changed.
	 * 
	 * @param action
	 *            action
	 * @param selection
	 *            selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * @return true if the last action run() was successful, false otherwise
	 */
	public boolean getActionSuccess() {
		return success;
	}

}
