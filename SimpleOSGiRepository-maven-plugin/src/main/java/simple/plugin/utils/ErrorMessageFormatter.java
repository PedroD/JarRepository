package simple.plugin.utils;

/**
 * The Class ErrorMessageFormatter.
 */
public final class ErrorMessageFormatter {

	/**
	 * Formats an error message to show up correctly into the maven's output.
	 * 
	 * @param m
	 *            the message to format.
	 * @return the string
	 */
	public static String format(String m) {
		return "\n\n Simple OSGi Repository: " + m + "\n\n";
	}

}
