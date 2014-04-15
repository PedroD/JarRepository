package lumina.extensions.drivers.rs232rxtx.transport;

import java.util.ResourceBundle;

/**
 * Messages for network physical drivers.
 * 
 */
public class Messages extends MessagesBase {

	private static final String BUNDLE_NAME = "lumina.extensions.drivers.rs232rxtx.transport.messages"; //$NON-NLS-1$

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
