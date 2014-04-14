package lumina.qp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs the tests of the real-time query processor.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(TestAggregator.class);
		// $JUnit-END$
		return suite;
	}

}
