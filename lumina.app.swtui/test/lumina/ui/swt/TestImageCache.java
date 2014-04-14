package lumina.ui.swt;

import junit.framework.TestCase;

import org.eclipse.swt.graphics.Image;

/**
 * Tests the image cache implementation.
 * <p>
 * 
 */
public class TestImageCache extends TestCase {

	/**
	 * An instantiation of {@link CustomizableImageCache}.
	 */
	private static final class ImageCacheTest extends CustomizableImageCache {

		/**
		 * @return an image object pretending that the main image object was
		 *         decorated.
		 */
		@Override
		protected Image makeDecoratedImage(Image main, Object qualifier) {
			return new Image(null, 8, 8);
		}

	}

	final Object A_DECORATION_QUALIFIER = new Object();
	final Object ANOTHER_DECORATION_QUALIFIER = new Object();

	/**
	 * The maximum number of images to use in the stress test. On Windows XP,
	 * the applications handles limit is around 8K handles. Setting this limit
	 * to 9K will make the stress test fail.
	 */
	static final int MAX_STRESS_TEST_QUALIFIERS = 1 * 1024;

	/**
	 * Tests that the decorated image is successfully recovered.
	 */
	public void testFind() {
		ImageCacheTest t = new ImageCacheTest();
		Image main = new Image(null, 1, 1);

		Image initial = t.getDecoratedImage(main, A_DECORATION_QUALIFIER);
		Image recovered = t.getDecoratedImage(main, A_DECORATION_QUALIFIER);

		assertEquals(initial, recovered);
	}

	/**
	 * Tests that the decorated image is not confused
	 */
	public void testNoConfusion() {
		ImageCacheTest t = new ImageCacheTest();
		Image main = new Image(null, 1, 1);

		Image decorated1 = t.getDecoratedImage(main, A_DECORATION_QUALIFIER);
		Image decorated2 = t.getDecoratedImage(main,
				ANOTHER_DECORATION_QUALIFIER);

		assertNotSame(decorated1, decorated2);
	}

	/**
	 * Performs a stress and correctness test.
	 */
	public void testStress() {
		ImageCacheTest t = new ImageCacheTest();
		Image main = new Image(null, 1, 1);

		// Initialize the cache
		final Image[] images = new Image[MAX_STRESS_TEST_QUALIFIERS];
		final Object[] qualifiers = new Object[MAX_STRESS_TEST_QUALIFIERS];
		for (int i = 0; i < MAX_STRESS_TEST_QUALIFIERS; i++) {
			qualifiers[i] = new Object();
			images[i] = t.getDecoratedImage(main, qualifiers[i]);
		}

		// Verifies the cache behavior
		for (int i = 0; i < 2 * MAX_STRESS_TEST_QUALIFIERS; i++) {
			int index = (int) Math.floor(Math.random()
					* MAX_STRESS_TEST_QUALIFIERS);
			assertSame(images[index],
					t.getDecoratedImage(main, qualifiers[index]));
		}
	}
}
