package lumina.ui.views;

import java.util.ResourceBundle;

import lumina.MessagesBase;


/**
 * i18n views messages.
 * 
 * @author Fernando Martins
 */
public class Messages extends MessagesBase {
	private static final String BUNDLE_NAME = "lumina.ui.views.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = MessagesBase
			.getResourceBundle(BUNDLE_NAME);

	/**
	 * Returns the requested i18n string.
	 * <p>
	 * This has been made public in order to be reused by
	 * {@link lumina.ui.views.properties.PropertiesView}.
	 * 
	 * @param key
	 *            message key
	 * @return i18n message
	 */
	public static final String getString(final String key) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key);
	}

	/**
	 * Returns the requested i18n string. This has been made public in order to
	 * be reused by {@link lumina.ui.views.properties.PropertiesView}.
	 * 
	 * @param key
	 *            message key
	 * @param messageArguments
	 *            arguments to be applied to the message
	 * @return i18n composed message
	 */
	public static final String getString(final String key,
			final Object... messageArguments) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key, messageArguments);
	}
}
