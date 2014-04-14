package lumina.ui.views.blueprint;

import java.util.ResourceBundle;

import lumina.MessagesBase;


/**
 * Blueprint messages.
 */
public class Messages extends MessagesBase {
	private static final String BUNDLE_NAME = "lumina.ui.views.blueprint"; //$NON-NLS-1$

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
