package lumina;

import junit.framework.Test;
import junit.framework.TestSuite;
import lumina.extensions.transport.rs232.TestRXTXRS232TransportDriver;

/**
 * Runs all transport driver tests.
 * <p>
 * <b>IMPORTANT</b>: The tests of most network components are very timing
 * sensitive. It may be necessary to run the tests several times.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$

		// Communication tests
		suite.addTestSuite(TestRXTXRS232TransportDriver.class);

		// $JUnit-END$
		return suite;
	}
}
