//package lumina.ui.actions;
//
//import java.lang.reflect.InvocationTargetException;
//
//import lumina.Constants;
//import lumina.Logger;
//import lumina.LuminaException;
//import lumina.base.model.Project;
//import lumina.base.model.ProjectModel;
//import lumina.energymanager.db.Database;
//import lumina.energymanager.db.Queries;
//import lumina.energymanager.report.DailyPowerConsumption;
//import lumina.energymanager.report.MonthlyPowerConsumption;
//import lumina.energymanager.report.PowerConsumptionReport;
//import lumina.energymanager.report.Report;
//import lumina.energymanager.report.ReportParameters;
//import lumina.ui.swt.SimpleDialogs;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.IWorkbenchWindowActionDelegate;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.progress.IProgressService;
//
///**
// * Report action delegate.<br/>
// * Shows the report dialog and performs report generation with progress bar in
// * monitored window.
// * <p>
// * Useful references:
// * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFaces Progress Monitor Dialog Example</a>}
// * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DialogExamples.htm">Dialog Examples</a>}
// * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
// * </p>
// * 
// * @author Fernando Martins
// */
//public class ReportsActionDelegate implements IWorkbenchWindowActionDelegate {
//
//	/**
//	 * The workbench window. Can be <code>null</code>.
//	 */
//	private IWorkbenchWindow window;
//
//	private static final String TASK_NAME = Messages
//			.getString("ReportActionProgress.taskName"); //$NON-NLS-1$
//	private static final String SUB_TASK_NAME_1 = Messages
//			.getString("ReportActionProgress.subTaskName1"); //$NON-NLS-1$
//	private static final String SUB_TASK_NAME_2 = Messages
//			.getString("ReportActionProgress.subTaskName2"); //$NON-NLS-1$
//	private static final String SUB_TASK_NAME_3 = Messages
//			.getString("ReportActionProgress.subTaskName3"); //$NON-NLS-1$
//
//	private static final String ERROR_SAVING = Messages
//			.getString("ReportActionProgress.errorSaving"); //$NON-NLS-1$
//
//	private static final String LOG_REPORT_CANCELED = "Report has been canceled.";
//	private static final String LOG_REPORT_CONCLUDED = "Report done.";
//
//	/**
//	 * Export report as action with progress.
//	 * <p>
//	 * Useful references:
//	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JFacesProgressMonitorDialog.htm">JFaces Progress Monitor Dialog Example</a>}
//	 * {@link <a href="http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DialogExamples.htm">Dialog Examples</a>}
//	 * {@link <a href="http://www.java2s.com/Code/JavaAPI/org.eclipse.jface.dialogs/newProgressMonitorDialogShellshell.htm">Progress Monitor Dialog Example</a>}
//	 * {@link <a href="http://www.cetic.be/internal.php3?id_article=225">Example</a>}
//	 * </p>
//	 */
//	private static class ReportActionProgress implements IRunnableWithProgress {
//
//		private ReportParameters reportParams = null;
//
//		/**
//		 * Constructor.
//		 * 
//		 * @param reportParams
//		 *            report data
//		 * @param workbenchWindow
//		 *            workbench window
//		 */
//		public ReportActionProgress(final ReportParameters reportParams,
//				final IWorkbenchWindow workbenchWindow) {
//			this.reportParams = reportParams;
//		}
//
//		/**
//		 * Generate report progress.
//		 * 
//		 * @param monitor
//		 *            progress monitor
//		 */
//		public void run(IProgressMonitor monitor)
//				throws InvocationTargetException, InterruptedException {
//
//			Project p = ProjectModel.getInstance().getProject();
//			if (p == null)
//				throw new IllegalStateException("Project is not available");
//			Database db = p.getDatabase();
//			if (db == null)
//				throw new IllegalStateException("Database is not available");
//
//			String subtaskName = TASK_NAME;
//			monitor.beginTask(TASK_NAME, IProgressMonitor.UNKNOWN);
//			Logger.log(TASK_NAME);
//
//			try {
//				subtaskName = SUB_TASK_NAME_1;
//				monitor.subTask(subtaskName);
//				Logger.log(subtaskName);
//				Queries.ReportResult res = null;
//
//				if (!monitor.isCanceled()) {
//					subtaskName = SUB_TASK_NAME_2;
//					monitor.subTask(subtaskName);
//					Logger.log(subtaskName + " [" + reportParams.getStartDate()
//							+ ", " + reportParams.getEndDate()
//							+ "] interval is " + reportParams.getInterval()
//							+ " and level is " + reportParams.getLevel());
//					res = Queries
//							.powerConsumption(db, reportParams.getStartDate(),
//									reportParams.getEndDate(),
//									reportParams.getInterval(),
//									reportParams.getLevel());
//
//					// for debugging, print raw data to stdout
//					/*
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,res.deviceTree);
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,"-------------------");
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,"From: " + reportParams.getStartDate()
//					 * + " To: " + reportParams.getEndDate() + " Interval: " +
//					 * reportParams.getInterval());
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,"-------------------"); for
//					 * (Queries.IntervalData idata : res.intervalData) {
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,idata.intervalStart); for
//					 * (Queries.NodeData ndata : idata.nodeData) {
//					 * Logger.getInstance().log(LogService.LOG_DEBUG,"\t" + ndata.node.getElement() + ": "
//					 * + ndata.powerConsumption + " Watts (Max " +
//					 * ndata.maxPowerConsumption + " Watts)" +
//					 * (ndata.missingData ? "\t**NO DATA**" : "") ); } }
//					 */
//
//				}
//				if (!monitor.isCanceled()) {
//					subtaskName = SUB_TASK_NAME_3;
//					monitor.subTask(subtaskName);
//					Logger.log(subtaskName);
//					if (!this.reportParams.getReportFile().endsWith(
//							Constants.REPORT_FILE_EXTENSION)) {
//						reportParams.setReportFile(this.reportParams
//								.getReportFile()
//								+ Constants.REPORT_FILE_EXTENSION);
//					}
//				}
//				if (!monitor.isCanceled()) {
//					Report report;
//					if (this.reportParams.getConsumptionType() == ReportParameters.ConsumptionType.Daily) {
//						report = new DailyPowerConsumption(
//								this.reportParams.getProjectName(),
//								this.reportParams.getReportFile(), res);
//					} else {
//						report = new MonthlyPowerConsumption(
//								this.reportParams.getProjectName(),
//								this.reportParams.getReportFile(), res);
//					}
//					if (!report.generate()) {
//						Logger.log(ERROR_SAVING);
//						Logger.log(report.getErrorMessage());
//						SimpleDialogs.showError(ERROR_SAVING,
//								report.getErrorMessage(), true);
//					} else {
//						Logger.log(LOG_REPORT_CONCLUDED);
//					}
//				} else {
//					Logger.log(LOG_REPORT_CANCELED);
//				}
//
//			} catch (IllegalStateException exState) {
//				throw new LuminaException(
//						Messages.getString("ReportActionProgress.errorSaving"), //$NON-NLS-1$
//						Messages.getString("ReportsActionDelegate.databaseUnavailableError"), exState); //$NON-NLS-1$
//
//			} catch (IllegalArgumentException exArgument) {
//				throw new LuminaException(
//						Messages.getString("ReportActionProgress.errorSaving"), //$NON-NLS-1$
//						Messages.getString("ReportsActionDelegate.databaseUnavailableError"), exArgument); //$NON-NLS-1$
//
//			} catch (Exception ex) {
//				throw new LuminaException(
//						Messages.getString("ReportActionProgress.errorSaving"), //$NON-NLS-1$
//						Messages.getString("ReportActionProgress.errorSavingDetail"), ex); //$NON-NLS-1$
//			} finally {
//				monitor.done();
//			}
//
//			if (monitor.isCanceled())
//				throw new InterruptedException(subtaskName);
//
//		}
//	}
//
//	/**
//	 * Runs action with progress dialog.
//	 * 
//	 * @param reportParams
//	 *            report data
//	 * @param workbenchWdw
//	 *            workbench window
//	 */
//	private void runProgress(final ReportParameters reportParams,
//			final IWorkbenchWindow workbenchWdw) {
//		Runnable runnable = new Runnable() {
//			public void run() {
//				IWorkbench wb = PlatformUI.getWorkbench();
//				IProgressService ps = wb.getProgressService();
//				try {
//					ps.run(false, true, new ReportActionProgress(reportParams,
//							workbenchWdw));
//				} catch (InvocationTargetException ex) {
//					// All exceptions that occur come wrapped in a
//					// InvocationTargetException,
//					// so unwrap them.
//					Throwable t = ex.getCause();
//
//					// Serious errors should be passed upward
//					if (t instanceof Error && !(t instanceof AssertionError))
//						throw (Error) t;
//
//					// Normal errors should already be turned into a
//					// LuminaException by
//					// the time they get here.
//					if (t instanceof LuminaException)
//						Logger.logAndDisplay(t.getMessage(), t, false, false);
//					else
//						Logger.logAndDisplay(
//								Messages.getString("ReportActionProgress.unexpectedError"), t); //$NON-NLS-1$
//				} catch (InterruptedException ex) {
//					Logger.logAndDisplay(
//							Messages.getString("ReportActionProgress.userCancelation"), ex); //$NON-NLS-1$
//				}
//			}
//		};
//
//		Display.getDefault().syncExec(runnable);
//	}
//
//	/**
//	 * Initialize.
//	 * 
//	 * @param window
//	 *            workbench window
//	 */
//	public void init(IWorkbenchWindow window) {
//		this.window = window;
//	}
//
//	/**
//	 * Terminate.
//	 */
//	public void dispose() {
//	}
//
//	/**
//	 * Trigger for selection change.
//	 * 
//	 * @param action
//	 *            action
//	 * @param selection
//	 *            selection
//	 */
//	public void selectionChanged(IAction action, ISelection selection) {
//	}
//
//	/**
//	 * Action execution.
//	 * 
//	 * @param action
//	 *            action
//	 */
//	public void run(IAction action) {
//		if (ProjectModel.getInstance().getProject().getDatabase() == null) {
//			// no database - warn the user that report generation is not
//			// possible
//			SimpleDialogs
//					.showInfo(
//							Messages.getString("ReportsActionDelegate.errorMsgTitle"),
//							Messages.getString("ReportsActionDelegate.errorMsgReportsUnavailable"),
//							false);
//		} else {
//			// display the report wizard
//			PowerConsumptionReport dialog = new PowerConsumptionReport(
//					window.getShell());
//			if (Dialog.OK == dialog.open()) {
//				runProgress(PowerConsumptionReport.getPowerConsumptionData(),
//						this.window);
//			}
//		}
//	}
//}
