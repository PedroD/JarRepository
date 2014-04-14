package lumina.ui.swt;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Bridge between AWT and SWT buffers.
 * <p>
 * This class is needed to connect the WMF renderer (used in
 * {@link lumina.views.blueprint.figures.FloorFigure}) with SWT.
 */
public final class AWTBridge {

	/**
	 * Masks all but the least significant byte.
	 */
	private static final int BYTE_MASK = 0x000000FF;

	/**
	 * Masks the most significant byte.
	 */
	private static final int NO_ALPHA_MASK = 0x00ffffff;

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private AWTBridge() {
	}

	/**
	 * Computes the bounding box on an AWT image.
	 * 
	 * @param awtImage
	 *            the image to be analyzed
	 * @param background
	 *            the background color
	 * @return the smallest rectangle that that contains none of the background
	 *         pixels or <code>null</code> if the conversion failed.
	 */
	public static Rectangle awtComputeBoundingBox(final BufferedImage awtImage,
			final Color background) {
		final int noAlpha = 0x00ffffff;

		final int noPixelColor = background.getRGB() & noAlpha;

		final int width = awtImage.getWidth();
		final int height = awtImage.getHeight();

		final int[] data = new int[width * height];
		final PixelGrabber grabber = new PixelGrabber(awtImage, 0, 0, width,
				height, data, 0, width);

		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			return null;
		}

		if ((grabber.getStatus() & ImageObserver.ABORT) != 0) {
			return null;
		}

		int minX = width;
		int minY = height;
		int maxX = 0;
		int maxY = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int argb = data[y * width + x];

				final int pixelWithoutAlpha = argb & NO_ALPHA_MASK;
				if (pixelWithoutAlpha != noPixelColor) {
					if (x < minX) {
						minX = x;
					}
					if (x > maxX) {
						maxX = x;
					}
					if (y < minY) {
						minY = y;
					}
					if (y > maxY) {
						maxY = y;
					}

				}
			}
		}

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	/**
	 * Converts an image to SWT.
	 * 
	 * @param image
	 *            buffered image
	 * @return image data
	 */
	public static ImageData swtImage(final BufferedImage image) {
		return convertImageToSWT(image, new Rectangle(0, 0, image.getWidth(),
				image.getHeight()));
	}

	/**
	 * Converts an image to SWT.
	 * 
	 * @param image
	 *            buffered image
	 * @param clipArea
	 *            area to clip
	 * @return image data
	 */
	public static ImageData convertImageToSWT(final BufferedImage image,
			final Rectangle clipArea) {
		if (image == null) {
			return null;
		}

		final Rectangle area = clipArea.intersection(new Rectangle(0, 0, image
				.getWidth(), image.getHeight()));

		if (image.getColorModel() instanceof DirectColorModel) {
			final DirectColorModel colorModel = (DirectColorModel) image
					.getColorModel();
			final PaletteData palette = new PaletteData(
					colorModel.getRedMask(), colorModel.getGreenMask(),
					colorModel.getBlueMask());
			final ImageData data = new ImageData(area.width, area.height,
					colorModel.getPixelSize(), palette);
			final WritableRaster raster = image.getRaster();
			final int[] pixelArray = new int[1 + 1 + 1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(area.x + x, area.y + y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}

			return data;
		} else if (image.getColorModel() instanceof IndexColorModel) {
			final IndexColorModel colorModel = (IndexColorModel) image
					.getColorModel();

			final int size = colorModel.getMapSize();
			final byte[] reds = new byte[size];
			final byte[] greens = new byte[size];
			final byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);

			final RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & BYTE_MASK, greens[i] & BYTE_MASK,
						blues[i] & BYTE_MASK);
			}

			final PaletteData palette = new PaletteData(rgbs);

			final ImageData imageData = new ImageData(area.width, area.height,
					colorModel.getPixelSize(), palette);

			imageData.transparentPixel = colorModel.getTransparentPixel();
			final WritableRaster raster = image.getRaster();

			if (raster.getDataBuffer() instanceof DataBufferByte) {
				/*
				 * Optimize by copying only the needed bytes. This optimization
				 * is between 10x and 100x times faster that the generic. We can
				 * go farther and assign both buffers, setting scanlinePad = 1
				 * and bytesPerLine = data.with and data = byteBuffer, which is
				 * even faster but only works if both buffers have the same
				 * size.
				 */
				byte[] byteBuffer = ((DataBufferByte) raster.getDataBuffer())
						.getData();

				final int srcByteLen = raster.getWidth();
				final int tgtMaxBytes = Math.min(imageData.bytesPerLine,
						srcByteLen);

				for (int i = 0; i < area.height; i++) {
					System.arraycopy(byteBuffer, area.x
							+ ((area.y + i) * srcByteLen), imageData.data, i
							* imageData.bytesPerLine, tgtMaxBytes);
				}
			} else {
				/*
				 * More generic but slower alternative.
				 */
				final int[] pixelArray = new int[1];
				for (int y = 0; y < imageData.height; y++) {
					for (int x = 0; x < imageData.width; x++) {
						raster.getPixel(area.x + x, area.y + y, pixelArray);
						imageData.setPixel(x, y, pixelArray[0]);
					}
				}
			}

			return imageData;
		}

		return null;
	}

	/**
	 * Converts a rectangle.
	 * 
	 * @param r
	 *            AWT rectangle
	 * @return SWT rectangle
	 */
	public static Rectangle swtRectangle(final java.awt.Rectangle r) {
		if (r != null) {
			return new Rectangle(r.x, r.y, r.width, r.height);
		} else {
			return null;
		}
	}

	/**
	 * Converts color.
	 * 
	 * @param color
	 *            SWT color
	 * @return AWT color
	 */
	public static java.awt.Color awtColor(
			final org.eclipse.swt.graphics.Color color) {
		final RGB rgb = color.getRGB();

		return new java.awt.Color(rgb.red, rgb.green, rgb.blue);
	}

}
