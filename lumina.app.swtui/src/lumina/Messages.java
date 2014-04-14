package lumina;

import java.util.ResourceBundle;

import lumina.MessagesBase;


/**
 * Lumina messages.
 * 
 * @author Fernando Martins
 */
public class Messages extends MessagesBase {
	private static final String BUNDLE_NAME = "lumina.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = MessagesBase
			.getResourceBundle(BUNDLE_NAME);

	protected static final String getString(final String key) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key);
	}

	protected static final String getString(final String key,
			final Object... messageArguments) {
		return MessagesBase.getString(RESOURCE_BUNDLE, key, messageArguments);
	}

}
