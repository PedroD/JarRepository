package lumina.ui.dialogs;

import java.util.ResourceBundle;

import lumina.MessagesBase;


/**
 * Dialogs messages.<br/>
 * i18n action messages.
 * 
 * @author Fernando Martins
 */
public class Messages extends MessagesBase {
	private static final int MONTHS_IN_A_YEAR = 12;

	private static final String BUNDLE_NAME = "lumina.ui.dialogs.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = MessagesBase
			.getResourceBundle(BUNDLE_NAME);

	private static final String COMMON_MONTHS = "Common.Month";

	/**
	 * Returns the requested i18n string.
	 * 
	 * @param key
	 *            message key
	 * @return i18n message
	 */
	protected static final String getString(final String key) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key);
	}

	/**
	 * Returns the requested i18n string.
	 * 
	 * @param key
	 *            message key
	 * @param messageArguments
	 *            arguments to be aplied to the message
	 * @return i18n composed message
	 */
	protected static final String getString(final String key,
			final Object... messageArguments) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key, messageArguments);
	}

	/**
	 * Returns the months names.
	 * 
	 * @return months an array with months
	 */
	public static String[] getMonths() {
		String[] result = new String[MONTHS_IN_A_YEAR];

		for (int i = 0; i < MONTHS_IN_A_YEAR; i++) {
			result[i] = Messages.getString(COMMON_MONTHS.concat(".").concat(
					Integer.toString(i + 1)));
		}

		return result;
	}
}
