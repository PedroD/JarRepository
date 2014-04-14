package lumina.ui.actions;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs the tests related to actions.
 **/
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(TestDropItemsAction.class);
		// $JUnit-END$
		return suite;
	}

}
