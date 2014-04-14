package lumina.ui.views.blueprint;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;

import lumina.kernel.Logger;
import lumina.ui.swt.AWTBridge;
import lumina.ui.swt.Graphics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.log.LogService;

import WMF2Viewer.Gdi.GDI2State;
import WMF2Viewer.Gdi.WMF2DrawListener;
import WMF2Viewer.Gdi.WMF2Object;

/**
 * A multi-threaded renders for WMF images.
 * <p>
 * This object renders the WMF object passed on the constructor.
 * <p>
 * In order to render another image a new image renderer object has to be
 * created.
 */
public final class WMFImageRenderer {

	/**
	 * DPI to report to the WMF renderer.
	 * <p>
	 * It determines the pixel size of the image that results from rasterizing
	 * the WMF. Using the actual display DPI would result in different image
	 * sizes on different computers which is not what we want. Instead a
	 * constant value should be used.
	 */
	private static final int RENDER_DPI = 98;

	/**
	 * Interface for listeners of rendering operations.
	 */
	public interface RenderingListener {
		/**
		 * Notifies the listener that the image has been rendered.
		 * 
		 * @param swtImageData
		 *            the image buffer
		 * @param drawingScale
		 *            the scale at which the image has been rendered
		 * @param finished
		 *            a flag that when <code>true</code>, indicates if this
		 *            rendering is the last
		 */
		void imageRendered(ImageData swtImageData, double drawingScale,
				boolean finished);

		/**
		 * Notifies the listener that the rendering failed.
		 * 
		 * @param cause
		 *            The error that caused the rendering to fail
		 */
		void renderError(Throwable cause);
	}

	private Color backgroundColor;

	private final WMF2Object wmfObject;

	private final Rectangle wmfObjectDimensions;

	/**
	 * Reference to the thread the renders the WMF.
	 */
	private Thread rendererThread;

	/**
	 * The bounding box is the smallest rectangle that contains the
	 * "interesting" part of the image, i.e., excluding the empty borders. The
	 * bounding box dimensions are normalized to match the size of the image at
	 * zoom 1.0
	 */
	private Rectangle boundingBox;

	/**
	 * Creates a WMF image renderer for the supplied WMF object.
	 * 
	 * @param wmf
	 *            the WMF object to be rendered. Should be loaded.
	 * @param background
	 *            the background color
	 */
	public WMFImageRenderer(final WMF2Object wmf, final Color background) {
		if (wmf == null) {
			throw new IllegalArgumentException("WMF object has to be assigned");
		}
		wmfObject = wmf;

		if (background == null) {
			throw new IllegalArgumentException(
					"Background color has to be assigned");
		}
		backgroundColor = background;

		wmfObjectDimensions = getSize(wmfObject);
		if (wmfObjectDimensions == null) {
			throw new IllegalStateException(
					"Could not get the size of the WMF object because the WMF object is not yet loaded.");
		}
	}

	/**
	 * Checks if the given zoom level exceeds the supported maximum addressable
	 * space in the image canvas.
	 * <p>
	 * It should be called before attempting to render the zoomed image. The
	 * constraint here is that the SWT internals use an int to calculate the
	 * total image size (in bytes). If the integer overflows the SWT classes
	 * will not fail gracefully, hence the need to check beforehand.
	 * 
	 * @param zoom
	 *            The desired zoom level
	 * @return true if the zoom is OK, false otherwise
	 */
	public boolean validateMaxZoom(final double zoom) {
		// WARNING this assumes 1 byte per pixel, which is what we currently use

		final long width = (long) (wmfObjectDimensions.width * zoom);
		final long height = (long) (wmfObjectDimensions.height * zoom);

		final boolean isIntegerOverflowSafe = (width * height) < (long) Integer.MAX_VALUE;
		return isIntegerOverflowSafe;
	}

	/**
	 * Compute the bounding box of the image.
	 * 
	 * @param image
	 *            The image
	 * @param scale
	 *            Current image zoom scale
	 */
	private void calculateBoundingBox(final BufferedImage image,
			final double scale) {
		final Rectangle box = AWTBridge.awtComputeBoundingBox(image,
				AWTBridge.awtColor(backgroundColor));

		if (box != null) {
			boundingBox = Graphics.scale(box, 1.0 / scale);
		} else {
			boundingBox = null;
		}
	}

	/**
	 * Computes the original dimensions of the WMF.
	 * 
	 * @param wmf
	 *            the WMF object
	 * @return a rectangle with the dimensions of the WMF or <code>null</code>
	 *         if the dimensions cannot be determined
	 */
	public static Rectangle getSize(final WMF2Object wmf) {
		final java.awt.Rectangle size = wmf.getWMFSize(RENDER_DPI);
		return AWTBridge.swtRectangle(size);
	}

	/**
	 * @return the image bounding box on <code>null</code> if not yet computed.
	 */
	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	// put this on string utils
	private static String percent(final double d) {
		final DecimalFormat format = new DecimalFormat("   .0%");
		return format.format(d);
	}

	/**
	 * The depth of the color pallete. Most WMF images don't need this much
	 * colors. Since we are assuming one byte per pixel, this is the most we can
	 * get. Maybe we could do better once we get rid of the AWT canvas and all
	 * the copy operation we have to do. More colors mean more bytes to copy
	 * between buffers.
	 */
	private static final int COLOR_PALETTE_SIZE = 256;

	/**
	 * Creates the palette to use in rendering the WMF.
	 * <p>
	 * The background color is assigned to palette index 0. The rest of the
	 * palette is filled with a generic color ramp (code adapted from
	 * java.awt.image.BufferedImage)
	 * 
	 * @param bgColor
	 *            Color that will be used for the image background.
	 * @return An IndexColorModel with a 256 color palette.
	 */
	// CHECKSTYLE:OFF - Constants in this method are self-explaning
	private static IndexColorModel createPalette(java.awt.Color bgColor) {
		final int[] cmap = new int[COLOR_PALETTE_SIZE];

		// reserve palette index 0 for the background color
		cmap[0] = bgColor.getRGB();

		// Create a 6x6x6 color cube
		int i = 1;
		for (int r = 0; r < 256; r += 51) {
			for (int g = 0; g < 256; g += 51) {
				for (int b = 0; b < 256; b += 51) {
					cmap[i++] = (r << 16) | (g << 8) | b;
				}
			}
		}

		// Populate the remainder of the palette with gray values
		final int grayIncr = 256 / (256 - i);
		int gray = grayIncr * 3;
		for (; i < 256; i++) {
			cmap[i] = (gray << 16) | (gray << 8) | gray;
			gray += grayIncr;
		}

		return new IndexColorModel(8, 256, cmap, 0, false, -1,
				DataBuffer.TYPE_BYTE);
	}

	// CHECKSTYLE:ON

	/**
	 * Thread the renders the WMF object.
	 * <p>
	 * If the object is too big or the computer is too slow, it the rendering is
	 * performed incrementally, by calling
	 * {@link RenderingListener#imageRendered(ImageData, double, boolean)}
	 */
	private final class RenderingThread extends Thread {

		/**
		 * Drawing refresh interval.
		 */
		private static final int REFRESH_INTERVAL = 1050;

		/**
		 * Flag that indicates if the current drawing thread is interrupted by
		 * another. Used to interrupt the current wmf drawing thread.
		 */
		private boolean isInterrupted;

		private final BufferedImage wmfRenderBuffer;

		private final Graphics2D graphics2d;

		private final GDI2State gdi2state;

		private final double wmfScale;

		/**
		 * Listener of the WMFRenderer that updates the canvas.
		 */
		private final class IncrementalDrawListener implements WMF2DrawListener {
			private final double drawingScale;

			public IncrementalDrawListener(final double scale) {
				drawingScale = scale;
			}

			private void refresh(final Rectangle bestBoundingBox,
					final boolean last) {
				// if no one is listening, no point in continuing
				if (renderingListener == null) {
					return;
				}

				// sanity checks
				if (drawingScale - RenderingThread.this.wmfScale != 0.0f) {
					return;
				}
				if (bestBoundingBox == null) {
					return;
				}

				// send the partial image asynchronously, so that the rendering
				// can
				// continue
				// simultaneously
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						final ImageData swtImageData;

						try {
							// use the current bounding box as clip area
							final Rectangle interestingArea = Graphics.scale(
									bestBoundingBox, wmfScale);

							swtImageData = AWTBridge.convertImageToSWT(
									wmfRenderBuffer, interestingArea);
						} catch (OutOfMemoryError ex) {
							// Out of memory Errors are expected here if the
							// zoom level
							// is too large for the available memory, so handle
							// them.
							if (last) {
								// if ran out of memory in the last render,
								// abort
								renderingListener.renderError(ex);
								return;
							} else {
								// if ran out of memory in a incremental render,
								// log but
								// ignore
								Logger.getInstance()
										.log(LogService.LOG_ERROR,
												"Out of memory in incremental render step (1)");
								return;
							}
						} catch (Exception ex) {
							if (last) {
								// unexpected error in last render, abort
								renderingListener.renderError(ex);
								return;
							} else {
								// unexpected error in a incremental render, log
								// but
								// ignore
								Logger.getInstance().log(LogService.LOG_ERROR,
										"Error in incremental render step (1)",
										ex);
								return;
							}
						}

						renderingListener.imageRendered(swtImageData,
								drawingScale, last);
					}
				});
			}

			/**
			 * Called once the drawing is finished.
			 */
			public void drawingFinished() {
				/*
				 * Bounding box is always recomputed in the end.
				 */
				calculateBoundingBox(wmfRenderBuffer, wmfScale);

				if (progressMonitor != null) {
					progressMonitor.done();
				}

				refresh(boundingBox, true);

				dispose();
			}

			/**
			 * Called multiple times by wmf renderer to update the partial
			 * drawing.
			 */
			public void hasDrawn() {
				final boolean firstTimeBeingDrawn = boundingBox == null;
				if (firstTimeBeingDrawn) {
					/*
					 * Calculate the bounding box once.
					 */
					calculateBoundingBox(wmfRenderBuffer, wmfScale);
				}

				refresh(boundingBox, false);

				if (progressMonitor != null) {
					progressMonitor.worked(IProgressMonitor.UNKNOWN);
				}
			}

			public boolean isDrawingInterrupted() {
				if (progressMonitor != null) {
					return progressMonitor.isCanceled() || isInterrupted;
				} else {
					return isInterrupted;
				}
			}
		};

		private final IncrementalDrawListener drawListener;

		private final IProgressMonitor progressMonitor;
		private final RenderingListener renderingListener;

		RenderingThread(final String taskName, final double scale,
				final IProgressMonitor monitor, final RenderingListener listener) {
			super("Image Renderer [" + taskName + "])"); // NON-NLS-1

			if (!validateMaxZoom(scale)) {
				throw new InvalidParameterException(
						"Parameter 'scale' exceeds the supported maximum");
			}

			wmfScale = scale;
			progressMonitor = monitor;
			renderingListener = listener;

			final int width = (int) (wmfObjectDimensions.width * wmfScale);
			final int height = (int) (wmfObjectDimensions.height * wmfScale);
			final java.awt.Color awtBgColor = AWTBridge
					.awtColor(backgroundColor);

			wmfRenderBuffer = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_INDEXED, createPalette(awtBgColor));

			graphics2d = wmfRenderBuffer.createGraphics();

			gdi2state = new GDI2State(RENDER_DPI);
			gdi2state.setViewportOrg(0, 0);
			gdi2state.setViewportExt(wmfRenderBuffer.getWidth(),
					wmfRenderBuffer.getHeight());

			graphics2d.scale(1.0, 1.0);
			graphics2d.setColor(awtBgColor);
			graphics2d.fillRect(0, 0, wmfRenderBuffer.getWidth(),
					wmfRenderBuffer.getHeight());

			/*
			 * Set to true to see anti-aliasing in effect
			 */
			// if (false) {
			// graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING,
			// RenderingHints.VALUE_RENDER_QUALITY);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
			// RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_DITHERING,
			// RenderingHints.VALUE_DITHER_DISABLE);
			//
			// graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
			// RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			// }

			drawListener = new IncrementalDrawListener(scale);
		}

		private void dispose() {
			graphics2d.dispose();
			wmfRenderBuffer.flush();
		}

		public void run() {
			try {
				wmfObject.drawWMF(graphics2d, gdi2state, drawListener,
						REFRESH_INTERVAL);
			} catch (Throwable ex) {
				if (renderingListener != null) {
					renderingListener.renderError(ex);
				} else {
					// if we can't notify the caller, log it instead
					Logger.getInstance().log(LogService.LOG_ERROR,
							"Error running WMF rendering thread", ex);
				}
			}
		}

		@Override
		public void interrupt() {
			isInterrupted = true;

			if (progressMonitor != null) {
				progressMonitor.setCanceled(true);
			}

			dispose();

			super.interrupt();
		}

	};

	public void cancelRendering() {
		if (rendererThread != null) {
			/*
			 * Stop the current drawing thread
			 */
			rendererThread.interrupt();
			rendererThread = null;
		}
	}

	/**
	 * Renders the wmfObject specified.
	 * 
	 * @param wmfScale
	 *            the wmf scale to perform the rendering
	 * @param monitor
	 *            the progress monitor object
	 * @param renderingListener
	 *            the rendering listener
	 */
	public void renderImage(final double wmfScale,
			final IProgressMonitor monitor,
			final RenderingListener renderingListener) {
		if (rendererThread != null) {
			cancelRendering();
		}

		final String taskName = "Zooming image to " + percent(wmfScale); // NON-NLS-1
		if (monitor != null) {
			monitor.beginTask("taskName", IProgressMonitor.UNKNOWN);
		}

		/*
		 * Create a new rendering thread.
		 */
		try {
			rendererThread = new RenderingThread(taskName, wmfScale, monitor,
					renderingListener);

			rendererThread.start();
		} catch (Throwable ex) {
			if (renderingListener != null) {
				renderingListener.renderError(ex);
			} else {
				// if we can't notify, log
				Logger.getInstance().log(LogService.LOG_ERROR,
						"Error creating WMF rendering thread", ex);
			}
		}
	}

	/**
	 * Sets the background color.
	 * <p>
	 * This method does not re-render the image. Changing the background color
	 * will take effect on the next rendering.
	 * 
	 * @param color
	 *            the new color to be set.
	 */
	public void setBackgroundColor(final Color color) {
		backgroundColor = color;
	}
}
