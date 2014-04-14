package lumina.ui.swt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lumina.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A cache to track the image resources.
 * <p>
 * Image caches are needed to avoid creating superfluous image objects. Image
 * objects retain handles of the graphic subsystem. After a number of hours of
 * operation if these handles are not freed the application will crash because
 * it runs out of graphic handles.
 * <p>
 * The image cache should be declared within a client that uses images, for
 * example a {@link org.eclipse.ui.part.ViewPart} and the methods
 * {@link #makeDecoratedImage(Image, Object)} and
 * {@link #makeImageFromPath(String)} should be overridden as needed.
 */
public abstract class CustomizableImageCache {

	/**
	 * A map of image names and image resources.
	 */
	private final Map<Object, Image> imageResources = new HashMap<Object, Image>();

	/**
	 * Loads an image from the given path.
	 * <p>
	 * This default implementation loads from the plug-in path. This method can
	 * be overridden to provide another implementation, for example to fetch the
	 * images from the web.
	 * 
	 * @param path
	 *            the path of the image
	 * @return the image resource or <code>null</code> in case the image could
	 *         not be loaded.
	 */
	protected Image makeImageFromPath(final String path) {
		final ImageDescriptor imageDescriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID, path);
		if (imageDescriptor != null) {
			return imageDescriptor.createImage();
		}

		return null;
	}

	/**
	 * Gets the image for given path.
	 * 
	 * @param path
	 *            the path of the image
	 * @return the image resource
	 */
	public final Image getImage(final String path) {
		final Image image = imageResources.get(path);
		if (image != null && !image.isDisposed()) {
			return image;
		} else {
			final Image newImage = makeImageFromPath(path);
			if (newImage != null) {
				imageResources.put(path, newImage);
				return newImage;
			}
		}

		return null;
	}

	/**
	 * Creates an image decoration.
	 * <p>
	 * This method can be overridden to provide the correct implementation.
	 * 
	 * @param main
	 *            the main image object to be decorated
	 * @param qualifier
	 *            specifies the type of decoration
	 * @return <code>null</code> as a default implementation
	 */
	protected Image makeDecoratedImage(final Image main, final Object qualifier) {
		return null;
	}

	/**
	 * Gets an image decoration of another image.
	 * <p>
	 * Caching of image overlays is performed bay associating image overlays
	 * with a main image. Each association is distinguished by a qualifier,
	 * which can be any object, for example an enumeration.
	 * <p>
	 * This method will call {@link #makeDecoratedImage(Image, Object)}, which
	 * should be overriden.
	 * 
	 * @param main
	 *            the main main image
	 * @param qualifier
	 *            indicates what is the kind decoration that will take place
	 * @return the image resource
	 */
	public final Image getDecoratedImage(final Image main,
			final Object qualifier) {
		final Pair<Image, Object> key = new Pair<Image, Object>(main, qualifier);
		final Image image = imageResources.get(key);

		if (image != null && !image.isDisposed()) {
			return image;
		} else {
			final Image newImage = makeDecoratedImage(main, qualifier);
			if (newImage != null) {
				imageResources.put(key, newImage);
				return newImage;
			}
		}

		return null;
	}

	/**
	 * Disposes all image handlers.
	 * <p>
	 * Should be called when disposing the client.
	 */
	public final void disposeAll() {
		for (final Entry<Object, Image> e : imageResources.entrySet()) {
			e.getValue().dispose();
		}
		imageResources.clear();
	}
}
