//package lumina.db;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import lumina.db.Queries.NodeData;
//import lumina.kernel.util.IO;
//import lumina.kernel.util.SysUtil;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//public class TestQueries {
//
//	private static String workDir = null;
//	private static lumina.db.Database db = null;
//
//	@SuppressWarnings("deprecation")
//	private Date makeDate(int y, int m, int d) {
//		return new Date(y - 1900, m - 1, d);
//	}
//
//	private static String getTestFileContents(String fileName)
//			throws IOException {
//		// get the script file content
//		String path = SysUtil.makePath(SysUtil.getApplicationPath(),
//				"testdata", fileName);
//		return IO.readTextFile(path);
//	}
//
//	private static final Comparator<Queries.NodeData> nodeComparator = new Comparator<Queries.NodeData>() {
//		public int compare(NodeData n1, NodeData n2) {
//			// sort by depth and then by name
//			int level1 = n1.node.getLevel();
//			int level2 = n2.node.getLevel();
//			if (level1 != level2)
//				return level1 - level2;
//			else
//				return n1.node.getElement().compareTo(n2.node.getElement());
//		}
//	};
//
//	private static String resultsToText(Queries.ReportResult res) {
//		StringBuilder text = new StringBuilder();
//
//		String nl = System.getProperty("line.separator");
//
//		text.append(res.deviceTree);
//		text.append(nl);
//		text.append("-------------------");
//		text.append(nl);
//		for (Queries.IntervalData idata : res.intervalData) {
//			text.append(String.format("%1$tF - %1$tT", idata.intervalStart));
//			text.append(nl);
//
//			List<Queries.NodeData> sortedNodes = idata.nodeData;
//			Collections.sort(sortedNodes, nodeComparator);
//
//			for (Queries.NodeData ndata : sortedNodes) {
//				text.append(String.format(Locale.US, "\t%s: %.1f (max %.1f)",
//						ndata.node.getElement(), ndata.powerConsumption,
//						ndata.maxPowerConsumption));
//				if (ndata.missingData)
//					text.append("\t**NO DATA**");
//				text.append(nl);
//			}
//		}
//
//		return text.toString();
//	}
//
//	@BeforeClass
//	public static void globalSetUp() throws Exception {
//		// prepare temp dirs and create derby DB
//		workDir = SysUtil.makePath(SysUtil.getTempPath(), "lumina_db_tests");
//		IO.deleteDirectory(new File(workDir));
//
//		String derbyHome = SysUtil.makePath(workDir, "derby_home");
//		(new File(derbyHome)).mkdirs();
//		lumina.db.Database.globalInit(derbyHome);
//
//		String dbLocation = SysUtil.makePath(workDir, "test_db");
//		db = new lumina.db.Database(dbLocation);
//		db.start();
//
//		// insert test data
//		Connection conn = db.getConnection();
//		try {
//			String sqlScript = getTestFileContents("test_setup.sql");
//			lumina.kernel.util.jdbc.JdbcUtil.executeScript(conn, sqlScript, ";");
//		} finally {
//			try {
//				conn.close();
//			} catch (SQLException ex) {/* nothing to do */
//			}
//		}
//	}
//
//	@AfterClass
//	public static void globalTearDown() throws Exception {
//		db.shutdown();
//		lumina.db.Database.globalShutdown();
//		IO.deleteDirectory(new File(workDir));
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 1:
//	// - basic test
//	// - only power events, all devices properly initialized, only one device
//	// changing
//	// power level.
//	// - report interval 60min
//	// - test is repeated for different report depths: device, area, floor,
//	// project
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test1_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 11),
//				makeDate(2009, 1, 12), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test1_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test1_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 11),
//				makeDate(2009, 1, 12), 60, 2);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test1_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test1_pt3() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 11),
//				makeDate(2009, 1, 12), 60, 1);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test1_pt3.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test1_pt4() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 11),
//				makeDate(2009, 1, 12), 60, 0);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test1_pt4.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 2:
//	// - same as test1 with missing initialization events (this should never
//	// happen,
//	// but at least the code should tolerate it)
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test2_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 13),
//				makeDate(2009, 1, 14), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test2_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test2_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 13),
//				makeDate(2009, 1, 14), 60, 2);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test2_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test2_pt3() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 13),
//				makeDate(2009, 1, 14), 60, 1);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test2_pt3.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test2_pt4() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 13),
//				makeDate(2009, 1, 14), 60, 0);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test2_pt4.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 3:
//	// - same as test1 but with periods of no information (project was closed
//	// and then
//	// opened)
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test3_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 15),
//				makeDate(2009, 1, 16), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test3_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test3_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 15),
//				makeDate(2009, 1, 16), 60, 2);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test3_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test3_pt3() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 15),
//				makeDate(2009, 1, 16), 60, 1);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test3_pt3.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test3_pt4() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 15),
//				makeDate(2009, 1, 16), 60, 0);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test3_pt4.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 4:
//	// - same as test3 but with missing close event (app crash)
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test4_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 17),
//				makeDate(2009, 1, 18), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test4_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test4_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 17),
//				makeDate(2009, 1, 18), 60, 2);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test4_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test4_pt3() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 17),
//				makeDate(2009, 1, 18), 60, 1);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test4_pt3.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test4_pt4() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 1, 17),
//				makeDate(2009, 1, 18), 60, 0);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test4_pt4.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 5:
//	// - "normal case" test
//	// - power & project events, all devices properly initialized, multiple
//	// devices
//	// changing power levels
//	// - report interval 60min
//	// - test is repeated for 2 report depths: device, area
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test5_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 2, 1),
//				makeDate(2009, 2, 3), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test5_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test5_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 2, 1),
//				makeDate(2009, 2, 3), 60, 2);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test5_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 6:
//	// - same as test 5 using different report intervals: 5m, 15m, 1d
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test6_pt1() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 2, 1),
//				makeDate(2009, 2, 3), 5, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test6_pt1.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test6_pt2() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 2, 1),
//				makeDate(2009, 2, 3), 15, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test6_pt2.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	@Test
//	public void test6_pt3() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 2, 1),
//				makeDate(2009, 2, 3), 1440, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test6_pt3.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 7:
//	// - max power level of a device changes
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test7() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 3, 2),
//				makeDate(2009, 3, 3), 60, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test7.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 8:
//	// - high values (test for overflows)
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test8() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 3, 4),
//				makeDate(2009, 3, 5), 1440, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test8.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//	// -----------------------------------------------------------------------------------
//	// Test 9:
//	// - Individual device offline / device online events
//	// -----------------------------------------------------------------------------------
//
//	@Test
//	public void test9() throws Exception {
//		Queries.ReportResult res;
//
//		res = Queries.powerConsumption(db, makeDate(2009, 3, 15),
//				makeDate(2009, 3, 16), 15, 3);
//		String obtained = resultsToText(res).trim();
//		String expected = getTestFileContents("result_test9.txt").trim();
//		assertEquals(expected, obtained);
//	}
//
//}
