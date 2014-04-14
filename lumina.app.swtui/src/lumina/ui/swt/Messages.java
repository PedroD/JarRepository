package lumina.ui.swt;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * i18n SWT utility messages.
 * 
 * @author Fernando Martins
 */
public final class Messages {
	private static final String BUNDLE_NAME = "lumina.ui.swt.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Prevents instantiation.
	 */
	private Messages() {
	}

	/**
	 * Returns the requested i18n string.
	 * 
	 * @param key
	 *            message key
	 * @return i18n message
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the requested i18n string. This has been made public in order to
	 * be reused by {@link lumina.views.properties.PropertiesView}.
	 * 
	 * @param key
	 *            message key
	 * @param messageArguments
	 *            argument to be applied to the message
	 * @return i18n composed message
	 */
	public static String getString(String key, String messageArguments) {
		return MessageFormat.format(Messages.getString(key), messageArguments);
	}

	/**
	 * Returns the requested i18n string. This has been made public in order to
	 * be reused by {@link lumina.views.properties.PropertiesView}.
	 * 
	 * @param key
	 *            message key
	 * @param messageArguments
	 *            arguments to be applied to the message
	 * @return i18n composed message
	 */
	public static String getString(String key, Object[] messageArguments) {
		MessageFormat messageForm = new MessageFormat("");
		messageForm.setLocale(RESOURCE_BUNDLE.getLocale());
		messageForm.applyPattern(getString(key));
		return messageForm.format(messageArguments);
	}
}
