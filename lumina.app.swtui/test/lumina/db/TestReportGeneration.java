//package lumina.db;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import lumina.report.DailyPowerConsumption;
//import lumina.report.MonthlyPowerConsumption;
//import lumina.report.Report;
//import lumina.report.ReportParameters;
//import lumina.kernel.util.FilenameUtil;
//import lumina.kernel.util.IO;
//import lumina.kernel.util.SysUtil;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import com.sun.xml.fastinfoset.util.StringArray;
//
//public class TestReportGeneration {
//
//	// <Report Test Parameters>
//	// Number of years to test
//	private static final int TEST_YEARS = 1;
//
//	// Number of minutes between events
//	private static final int TEST_MINUTES_INTERVAL = 5;
//
//	// Monthly report time interval
//	private static final int MONTHLY_REPORT_INTERVAL = 5;
//
//	// Maximum power levels
//	private static final Integer[] MAX_POWER_LEVEL = new Integer[] { 100, 150,
//			200, 100, 150, 200, 100, 150, 200 };
//
//	// Power levels events
//	private static final Integer[] POWER_LEVEL_EVENTS = new Integer[] { 10, 7,
//			3, 9, 6, 2, 11, 8, 4 };
//
//	// Database date format
//	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
//	// </Report Test Parameters>
//
//	// Data generation begin date
//	private static Date beginDate;
//
//	// Data generation end date
//	private static Date endDate;
//
//	private static String workDir = null;
//	private static lumina.db.Database db = null;
//	private static StringArray sqlScriptTemplate;
//	private static Connection connection;
//	private static String dbLocation;
//	private static String derbyHome;
//
//	/**
//	 * Prints a message with a time stamp.
//	 */
//	protected static void print(final String message) {
//		Logger.getInstance().log(LogService.LOG_DEBUG,"<" + getFormatedDate(Calendar.getInstance()) + "> "
//				+ message);
//	}
//
//	/**
//	 * Get the script file content.
//	 * 
//	 * @param fileName
//	 *            script file name
//	 * @return script content
//	 * @throws IOException
//	 */
//	private static StringArray getTestFileContents(final String fileName)
//			throws IOException {
//		StringArray result = new StringArray();
//
//		final String path = SysUtil.makePath(SysUtil.getApplicationPath(),
//				"testdata", fileName);
//		final String[] fileContents = IO.readTextFile(path).split("\n");
//
//		for (String line : fileContents) {
//			String ln = line.trim();
//			if (!ln.startsWith("--") && ln.length() > 0) {
//				result.add(line);
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * Gets a date formated into a string.
//	 * 
//	 * @param value
//	 *            date
//	 * @return formated date
//	 */
//	protected static String getFormatedDate(final Date value) {
//		SimpleDateFormat dt = new SimpleDateFormat(DATE_FORMAT);
//		return dt.format(value);
//	}
//
//	/**
//	 * Gets a time difference as a string.
//	 * 
//	 * @param start
//	 *            date
//	 * @param stop
//	 *            date
//	 * @return time difference
//	 */
//	protected static String getFormatedDifference(final Calendar start,
//			final Calendar stop) {
//		double deltaSeconds = (stop.getTimeInMillis() - start.getTimeInMillis()) / 1000.0;
//
//		return Double.toString(deltaSeconds);
//	}
//
//	/**
//	 * Gets a date formated into a string.
//	 * 
//	 * @param value
//	 *            date
//	 * @return formated date
//	 */
//	protected static String getFormatedDate(final Calendar value) {
//		return getFormatedDate(value.getTime());
//	}
//
//	/**
//	 * Instantiates a template.
//	 * 
//	 * @param template
//	 *            template to instantiate
//	 * @param key
//	 *            key to be replaced
//	 * @param value
//	 *            value to replace the key
//	 * @return template instantiated
//	 */
//	protected static String instantiate(final String template,
//			final String key, final String value) {
//		String result = template;
//		return result.replaceAll(key, value);
//	}
//
//	/**
//	 * Instantiates a template.
//	 * 
//	 * @param template
//	 *            template to instantiate
//	 * @param key
//	 *            key to be replaced
//	 * @param value
//	 *            date to replace the key
//	 * @return template instantiated
//	 */
//	protected static String instantiate(final String template,
//			final String key, final Calendar value) {
//		return instantiate(template, key, getFormatedDate(value));
//	}
//
//	/**
//	 * Instantiates a template.
//	 * 
//	 * @param template
//	 *            template to instantiate
//	 * @param key
//	 *            key to be replaced
//	 * @param value
//	 *            integer value to replace the key
//	 * @return template instantiated
//	 */
//	protected static String instantiate(final String template,
//			final String key, final Integer value) {
//		return instantiate(template, key, value.toString());
//	}
//
//	/**
//	 * Instantiate the project structure.
//	 * 
//	 * @param template
//	 *            sql template
//	 * @param values
//	 *            max power levels
//	 * @return sql ready for execution
//	 */
//	protected static String instantiateProjectStructure(final String template,
//			final Integer... values) {
//		String result = template;
//
//		int d = 1;
//		for (Integer maxPowerLevel : values) {
//			result = instantiate(result, "%pm" + Integer.toString(d),
//					maxPowerLevel);
//			d++;
//		}
//
//		return result;
//	}
//
//	/**
//	 * Instantiate start and stop event.
//	 * 
//	 * @param template
//	 *            sql template
//	 * @param dt
//	 *            date
//	 * @return sql ready for execution
//	 */
//	protected static String instantiateStartStopEvent(final String template,
//			final Calendar dt) {
//		return instantiate(template, "%d", dt);
//	}
//
//	/**
//	 * Instantiate the project structure.
//	 * 
//	 * @param template
//	 *            sql template
//	 * @param values
//	 *            max power levels
//	 * @return sql ready for execution
//	 */
//	protected static String instantiatePowerEvents(final String template,
//			final Calendar dt, final Integer... values) {
//		String result = instantiate(template, "%d", dt);
//
//		int d = 1;
//		for (Integer powerLevel : values) {
//			result = instantiate(result, "%pl" + Integer.toString(d),
//					powerLevel);
//			d++;
//		}
//
//		return result;
//	}
//
//	/**
//	 * Populates data.
//	 * 
//	 * @throws SQLException
//	 */
//	protected static void populateData() throws SQLException {
//		final String projectStructure = sqlScriptTemplate.get(0);
//		final String openTemplate = sqlScriptTemplate.get(1);
//		final String closeTemplate = sqlScriptTemplate.get(2);
//		final String eventTemplate = sqlScriptTemplate.get(3);
//		String sql;
//
//		print("Generating data for " + TEST_YEARS + " year(s)");
//		Calendar currentDate = Calendar.getInstance();
//		currentDate.setLenient(true);
//		endDate = currentDate.getTime();
//
//		Calendar testDate = Calendar.getInstance();
//		testDate.setLenient(true);
//		testDate.setTime(currentDate.getTime());
//		testDate.add(Calendar.YEAR, -TEST_YEARS);
//		beginDate = testDate.getTime();
//
//		sql = instantiateProjectStructure(projectStructure, MAX_POWER_LEVEL);
//		lumina.kernel.util.jdbc.JdbcUtil.executeScript(connection, sql, ";");
//
//		print("Project Open: " + getFormatedDate(testDate));
//		sql = instantiateStartStopEvent(openTemplate, testDate);
//		lumina.kernel.util.jdbc.JdbcUtil.executeScript(connection, sql, ";");
//
//		print("Events will chage every: " + TEST_MINUTES_INTERVAL + " minutes");
//		Integer[] currentLevels = POWER_LEVEL_EVENTS.clone();
//
//		Integer[] movement = new Integer[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
//
//		int lastMonth = 0;
//		while (testDate.before(currentDate)) {
//			testDate.add(Calendar.MINUTE, TEST_MINUTES_INTERVAL);
//
//			if (lastMonth != testDate.get(Calendar.MONTH)) {
//				print("Calculating for new month: " + getFormatedDate(testDate));
//				lastMonth = testDate.get(Calendar.MONTH);
//			}
//
//			sql = instantiatePowerEvents(eventTemplate, testDate, currentLevels);
//			lumina.kernel.util.jdbc.JdbcUtil.executeScript(connection, sql, ";");
//
//			for (int p = 0; p < currentLevels.length; p++) {
//				// Calculate next power level
//				currentLevels[p] += movement[p] * POWER_LEVEL_EVENTS[p];
//
//				// Check power level boundaries and switch movement if necessary
//				if (currentLevels[p] > MAX_POWER_LEVEL[p]) {
//					currentLevels[p] = MAX_POWER_LEVEL[p];
//					movement[p] = -1;
//				} else if (currentLevels[p] <= 0) {
//					currentLevels[p] = 0;
//					movement[p] = 1;
//				}
//			}
//		}
//
//		endDate = testDate.getTime();
//		print("Project Close: " + getFormatedDate(testDate));
//		sql = instantiateStartStopEvent(closeTemplate, testDate);
//		lumina.kernel.util.jdbc.JdbcUtil.executeScript(connection, sql, ";");
//
//	}
//
//	@BeforeClass
//	public static void globalSetUp() throws Exception {
//		// prepare temp dirs and create derby DB
//		workDir = SysUtil
//				.makePath(SysUtil.getTempPath(), "lumina_report_tests");
//		IO.deleteDirectory(new File(workDir));
//
//		derbyHome = SysUtil.makePath(workDir, "derby_home");
//		(new File(derbyHome)).mkdirs();
//		lumina.db.Database.globalInit(derbyHome);
//
//		dbLocation = SysUtil.makePath(workDir, "test_report");
//		db = new lumina.db.Database(dbLocation);
//		db.start();
//
//		// insert test data
//		connection = db.getConnection();
//		sqlScriptTemplate = getTestFileContents("test_setup_report_data.sql");
//
//		try {
//			populateData();
//			print(workDir + " size: " + IO.getSizeInKbytes(new File(workDir))
//					+ " Kbytes");
//		} finally {
//			try {
//				connection.close();
//			} catch (SQLException ex) {/* nothing to do */
//			}
//		}
//	}
//
//	@AfterClass
//	public static void globalTearDown() throws Exception {
//		db.shutdown();
//		lumina.db.Database.globalShutdown();
//		print("Database size: " + IO.getSizeInKbytes(new File(dbLocation))
//				+ " Kbytes");
//		IO.deleteDirectory(new File(workDir));
//		print(workDir + " has been deleted. Test completed.");
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test Daily Report Generation
//	// -----------------------------------------------------------------------------------
//	@Test
//	public void test_reportGeneration() throws Exception {
//		Calendar benginGeneration;
//		Calendar endGeneration;
//		boolean resultGeneration;
//
//		// Daily
//		print("Generating daily report between " + getFormatedDate(beginDate)
//				+ " and " + getFormatedDate(endDate) + "...");
//		benginGeneration = Calendar.getInstance();
//		final String dailyFileName = FilenameUtil.concat(workDir,
//				"TestGenerationDaily.xls");
//		ReportParameters dailyReportParams = new ReportParameters();
//		dailyReportParams
//				.setConsumptionType(ReportParameters.ConsumptionType.Daily);
//		Queries.ReportResult dailyRes = Queries.powerConsumption(db, beginDate,
//				endDate, ReportParameters.DAILY_MINUTES,
//				dailyReportParams.getLevel());
//
//		endGeneration = Calendar.getInstance();
//		print("Query took "
//				+ getFormatedDifference(benginGeneration, endGeneration)
//				+ " seconds");
//		Report dailyReport = new DailyPowerConsumption("TestReportGeneration",
//				dailyFileName, dailyRes);
//		resultGeneration = dailyReport.generate();
//		if (!resultGeneration) {
//			print(dailyReport.getErrorMessage());
//		}
//		assertEquals(resultGeneration, true);
//		endGeneration = Calendar.getInstance();
//		print("Total generation (query and report) took "
//				+ getFormatedDifference(benginGeneration, endGeneration)
//				+ " seconds");
//		print(dailyFileName + " size: "
//				+ IO.getSizeInKbytes(new File(dailyFileName)) + " Kbytes");
//
//		// Monthly
//		Calendar endDateMonthly = Calendar.getInstance();
//		endDateMonthly.setLenient(true);
//		endDateMonthly.set(Calendar.DAY_OF_MONTH, 1);
//		endDateMonthly.set(Calendar.HOUR, 0);
//		endDateMonthly.set(Calendar.MINUTE, 0);
//		endDateMonthly.set(Calendar.SECOND, 0);
//		endDateMonthly.set(Calendar.MILLISECOND, 0);
//
//		Calendar beginDateMonthly = Calendar.getInstance();
//		beginDateMonthly.setLenient(true);
//		beginDateMonthly.add(Calendar.MONTH, -1);
//		beginDateMonthly.set(Calendar.DAY_OF_MONTH, 1);
//		beginDateMonthly.set(Calendar.HOUR, 0);
//		beginDateMonthly.set(Calendar.MINUTE, 0);
//		beginDateMonthly.set(Calendar.SECOND, 0);
//		beginDateMonthly.set(Calendar.MILLISECOND, 0);
//
//		print("Generating monthly report for "
//				+ getFormatedDate(beginDateMonthly).substring(0, 10)
//				+ " with interval of "
//				+ Integer.toBinaryString(MONTHLY_REPORT_INTERVAL)
//				+ " minute(s)");
//		benginGeneration = Calendar.getInstance();
//		final String monthlyFileName = FilenameUtil.concat(workDir,
//				"TestReportGenerationMonthly.xls");
//		ReportParameters monthlyReportParams = new ReportParameters();
//		monthlyReportParams
//				.setConsumptionType(ReportParameters.ConsumptionType.Monthly);
//
//		Queries.ReportResult monthlyRes = Queries.powerConsumption(db,
//				beginDateMonthly.getTime(), endDateMonthly.getTime(),
//				MONTHLY_REPORT_INTERVAL, monthlyReportParams.getLevel());
//		print(monthlyRes.toString());
//		endGeneration = Calendar.getInstance();
//		print("Query took "
//				+ getFormatedDifference(benginGeneration, endGeneration)
//				+ " seconds");
//		Report monthlyReport = new MonthlyPowerConsumption(
//				"TestReportGeneration", monthlyFileName, monthlyRes);
//		resultGeneration = monthlyReport.generate();
//		if (!resultGeneration) {
//			print(monthlyReport.getErrorMessage());
//		}
//		assertEquals(resultGeneration, true);
//		endGeneration = Calendar.getInstance();
//		print("Total generation (query and report) took "
//				+ getFormatedDifference(benginGeneration, endGeneration)
//				+ " seconds");
//		print(monthlyFileName + " size: "
//				+ IO.getSizeInKbytes(new File(monthlyFileName)) + " Kbytes");
//	}
//
//}
