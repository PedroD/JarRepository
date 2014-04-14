package lumina.ui.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs the tests of the SWT Utilities.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(TestImageCache.class);
		// $JUnit-END$
		return suite;
	}

}
