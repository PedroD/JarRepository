package lumina.ui.swt;

import org.eclipse.swt.graphics.Image;

/**
 * A global cache to track image resources global to the application.
 */
public final class ApplicationImageCache extends CustomizableImageCache {

	/**
	 * Singleton image cache instance.
	 */
	private static final ApplicationImageCache DEFAULT_INSTANCE = new ApplicationImageCache();

	/**
	 * Gets the default instance.
	 * 
	 * @return the default instance
	 */
	public static ApplicationImageCache getInstance() {
		return DEFAULT_INSTANCE;
	}

	/**
	 * Creates an image decoration.
	 * 
	 * @param main
	 *            ignored
	 * @param qualifier
	 *            ignored
	 * @return nothing.
	 */
	protected Image makeDecoratedImage(final Image main, final Object qualifier) {
		throw new UnsupportedOperationException();
	}
}
