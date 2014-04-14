package lumina.ui.propertytesters;

import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;

import org.eclipse.core.expressions.PropertyTester;

/**
 * A property tester used to determine visibility of menu and toolbar commands.
 * The "expected value" passed to the property tester must be one of the values
 * of the {@link lumina.license.Capabilities.Capability} enum.
 */
public class CapabilityPropertyTester extends PropertyTester {

	/**
	 * Tests a capability.
	 * 
	 * @param receiver
	 *            object
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            expected value
	 * @return capability test
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		try {
			final Capability cap = Capability.valueOf((String) expectedValue);
			return Capabilities.canDo(cap);
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

}
