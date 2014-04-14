package lumina.ui.swt.handlers;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester for guarding interceptable handlers.
 * <p>
 * This property tester return <code>true</code> if a given command is being
 * intercepted. This ensures that the call is in fact forwarded to the
 * interceptor.
 */
public abstract class InterceptedPropertyTester extends PropertyTester {

	private final String commandId;

	/**
	 * Builds a new property tester that checks whether the handler associated
	 * with the give id has been overridden.
	 */
	public InterceptedPropertyTester() {
		commandId = getCommandId();
	}

	/**
	 * Tests the property.
	 * <p>
	 * Contacts the the command handler to check if the command has been
	 * Overridden.
	 * 
	 * @param receiver
	 *            ignored
	 * @param property
	 *            ignored
	 * @param args
	 *            ignored
	 * @param expectedValue
	 *            the expected value
	 * @return <code>false</code> if the command has been overridden, in order
	 *         to forward the call to the next command handler. Otherwise
	 *         returns the result
	 */
	public final boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (HandlerInterceptionService.getInstance().isIntercepted(commandId)) {
			return true;
		} else {
			return testProperty(receiver, property, args, expectedValue);
		}
	}

	/**
	 * Gets the command id for the handler being overridden.
	 * 
	 * @return a String containing the command id.
	 */
	public abstract String getCommandId();

	/**
	 * Executes the property test determined by the parameter
	 * <code>property</code>.
	 * 
	 * @param receiver
	 *            the receiver of the property test
	 * @param property
	 *            the property to test
	 * @param args
	 *            additional arguments to evaluate the property. If no arguments
	 *            are specified in the <code>test</code> expression an array of
	 *            length 0 is passed
	 * @param expectedValue
	 *            the expected value of the property. The value is either of
	 *            type <code>java.lang.String</code> or a boxed base type. If no
	 *            value was specified in the <code>test</code> expressions then
	 *            <code>null</code> is passed
	 * @return returns <code>true</code> if the property is equal to the
	 *         expected value; otherwise <code>false</code> is returned
	 */
	protected abstract boolean testProperty(final Object receiver,
			final String property, final Object[] args,
			final Object expectedValue);
}
