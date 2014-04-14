package lumina.ui.swt;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * Used for overlaying image icons.
 */
public class OverlayImageIcon extends CompositeImageDescriptor {
	/**
	 * The relative position of the overlay icon.
	 */
	public static enum Position {
		/**
		 * Static constants for the top left location of the overlay icon.
		 */
		TOP_LEFT,
		/**
		 * Static constants for the top right location of the overlay icon.
		 */
		TOP_RIGHT,
		/**
		 * Static constants for the bottom left location of the overlay icon.
		 */
		BOTTOM_LEFT,
		/**
		 * Static constants for the bottom right location of the overlay icon.
		 */
		BOTTOM_RIGHT;
	}

	// Base image of the object
	private Image iconBaseImage;

	// Size of the base image
	private Point sizeOfImage;

	// The overlay image
	private Image iconOverlayImage;

	// The chosen icon position
	private Position iconPosition;

	/**
	 * Constructor for overlayImageIcon.
	 * 
	 * @param baseImage
	 *            the base image
	 * @param overlayImage
	 *            the overlay image
	 * @param position
	 *            the position
	 */
	public OverlayImageIcon(final Image baseImage, final Image overlayImage,
			final Position position) {
		if (baseImage == null) {
			throw new IllegalArgumentException("Base image must be assigned");
		}
		iconBaseImage = baseImage;

		sizeOfImage = new Point(baseImage.getBounds().width,
				baseImage.getBounds().height);

		if (overlayImage == null) {
			throw new IllegalArgumentException("Overlay image must be assigned");
		}

		iconOverlayImage = overlayImage;

		iconPosition = position;
	}

	/**
	 * Draw composite image.
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,int)
	 *      DrawCompositeImage is called to draw the composite image.
	 */
	protected void drawCompositeImage(int x, int y) {
		drawImage(iconBaseImage.getImageData(), 0, 0);
		final ImageData imageData = iconOverlayImage.getImageData();

		switch (iconPosition) {
		// Draw on the top left corner
		case TOP_LEFT:
			drawImage(imageData, 0, 0);
			break;

		// Draw on top right corner
		case TOP_RIGHT:
			drawImage(imageData, sizeOfImage.x - imageData.width, 0);
			break;

		// Draw on bottom left corner
		case BOTTOM_LEFT:
			drawImage(imageData, 0, sizeOfImage.y - imageData.height);
			break;

		// Draw on bottom right corner
		case BOTTOM_RIGHT:
			drawImage(imageData, sizeOfImage.x - imageData.width, sizeOfImage.y
					- imageData.height);
			break;
		}
	}

	/**
	 * Gets the size of the image.
	 * 
	 * @return image size
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		return sizeOfImage;
	}

	/**
	 * Get the image formed by overlaying different images on the base image.
	 * 
	 * @return composite image
	 */
	public Image getImage() {
		return createImage();
	}

}
