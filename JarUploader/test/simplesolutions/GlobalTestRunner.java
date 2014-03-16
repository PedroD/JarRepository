package simplesolutions;

import simplesolutions.dependencyserver.impl.TestJarFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The Class GlobalTestRunner.
 */
public class GlobalTestRunner {

	/**
	 * Suite.
	 *
	 * @return the test
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(GlobalTestRunner.class.getName());
		// $JUnit-BEGIN$

		suite.addTestSuite(TestJarFile.class);

		// $JUnit-END$
		return suite;
	}

	/**
	 * Kick off the tests.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.main(new String[] { GlobalTestRunner.class
				.getName() });
	}
}
