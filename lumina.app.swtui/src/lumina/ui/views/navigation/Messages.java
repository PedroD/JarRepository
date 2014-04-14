package lumina.ui.views.navigation;

import java.util.ResourceBundle;

import lumina.MessagesBase;


/**
 * i18n view navigation messages.
 * 
 * @author Fernando Martins
 */
public class Messages extends MessagesBase {
	private static final String BUNDLE_NAME = "lumina.ui.views.navigation.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = MessagesBase
			.getResourceBundle(BUNDLE_NAME);

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
}
