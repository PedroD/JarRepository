package lumina.ui.views.blueprint.figures;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import lumina.kernel.Logger;
import lumina.ui.swt.SWTImageCanvas;
import lumina.ui.views.blueprint.WMFImageRenderer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

import WMF2Viewer.Gdi.WMF2Object;

/**
 * The base class for handling figures rendering and display in the plan.
 * <p>
 * This class extends an eclipse Draw2D canvas object.
 * <p>
 * This class manages loading a figure file, performing and aborting the render
 * process. Rendering is is performed assynchronously.
 */
public abstract class BaseFloorFigure extends LightweightSystem {

	/**
	 * Flag used to activate diagnostic messages for this module.
	 */
	private static final boolean DIAGNOSTIC = true;

	/**
	 * Default background color.
	 */
	private static final int DEFAULT_BACKGROUND_COLOR = SWT.COLOR_BLACK;

	/**
	 * Interface for event listeners that are notified when the image is ready.
	 * <p>
	 * An image is considered ready when it is rendered on the canvas.
	 */
	public interface ImageReadyListener {
		/**
		 * Image ready.
		 * 
		 * @param figure
		 *            the figure
		 * @param canvas
		 *            the canvas
		 */
		void imageReady(final BaseFloorFigure figure,
				final SWTImageCanvas canvas);
	}

	/**
	 * Error handling strategies when rendering the floor plan. See
	 * rollbackBackgroundRender for their interpretation.
	 */
	private enum OnRenderError {
		/**
		 * Rendering has failed.
		 */
		FAIL,
		/**
		 * Rendering should continue. No problem.
		 */
		CONTINUE,
		/**
		 * Try zooming to 100%.
		 */
		ZOOM_TO_100,
		/**
		 * Try zooming to the previous zoom level.
		 */
		ZOOM_TO_PREVIOUS,
	}

	/**
	 * Holds the set of image listeners to be notified.
	 */
	private final Set<ImageReadyListener> imageReadyListeners = new HashSet<ImageReadyListener>();

	/**
	 * Scale at which the image is rendered.
	 */
	private double zoomScale = 1.0;

	/**
	 * Previous scale. Used to rollback an asynchronous zoom operation if it
	 * fails.
	 */
	private double previousZoomScale;

	/**
	 * The byte array where the figure was read from.
	 */
	private byte[] figureData;

	/**
	 * The WMF object loaded. This object is necessary because we cannot create
	 * the renderer right away inside ActionEventListener of the WMF load
	 * method.
	 */
	private WMF2Object wmfObject;

	/**
	 * The main renderer used to render the wmfObject into a raster image.
	 */
	private WMFImageRenderer mainImageRenderer;

	/**
	 * The image renderer lock.
	 */
	private final Object imageRendererLock = new Object();

	/**
	 * Flag that indicates if the background image is ready.
	 * <p>
	 * This flag is <code>false</code> if the rendering is in progress.
	 */
	private boolean backgroundImageReady;

	/**
	 * The visibility status of the figure.
	 */
	private boolean figureIsVisible;

	/**
	 * A mutex that is released when the wmfObject is loaded.
	 * <p>
	 * This helps preventing that any method that need to render an image waits
	 * for an ongoing load operation.
	 */
	private static final class WMFLoadMonitor {

		/**
		 * The has loaded.
		 */
		private volatile boolean hasLoaded;

		/**
		 * Unlocks the thread waiting on {@link #notifyLoadFinished()}.
		 */
		public synchronized void notifyLoadFinished() {
			hasLoaded = true;
			notifyAll();
		}

		/**
		 * Waits until another thread calls {@link #notifyLoadFinished()}.
		 */
		public synchronized void waitLoadFinished() {
			while (!hasLoaded) {
				try {
					wait(); // wait for value to be consumed
				} catch (InterruptedException e) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"WMFLoad monitor", e); //$NON-NLS-1$
				}
			}

			hasLoaded = false;
		}
	};

	/**
	 * The WMF monitor object.
	 */
	private final WMFLoadMonitor wmfObjectLoadedMutex = new WMFLoadMonitor();

	/**
	 * The parent widget.
	 */
	private final SWTImageCanvas imageCanvas;

	/**
	 * Instantiates a new base floor figure.
	 * 
	 * @param top
	 *            the top
	 */
	public BaseFloorFigure(final SWTImageCanvas top) {
		super(top);
		imageCanvas = top;

		// feedBackMessage = new FeedBackMessage(imageCanvas, SWT.NONE);
		// feedBackMessage.setMode(FeedBackMessage.Mode.NO_IMAGE_AVAILABLE);

		doInstallLabel();
	}

	// /**
	// * {@link PaintListener} used to update the message coordinates.
	// */
	// private PaintListener messagePaintListerner;
	//
	// private static class FeedBackMessage
	// extends Composite {
	// public enum Mode {
	// NO_MESSAGE, NO_IMAGE_AVAILABLE, LOADING,
	// };
	//
	// /**
	// * Message displayed when no image is available for the current floor.
	// */
	// private static final String NO_IMAGE_DEFINED_MESSAGE = Messages.getString("BaseFloorFigure.noImageAvailable"); //$NON-NLS-1$
	//
	// /**
	// * Message displayed when the floor is loading.
	// */
	// private static final String LOADING_MESSAGE = Messages.getString("BaseFloorFigure.loadingBlueprintImage"); //$NON-NLS-1$
	//
	// /**
	// * Default label color.
	// */
	// private static final int DEFAULT_LABEL_COLOR = SWT.COLOR_WHITE;
	//
	// /**
	// * Label used to display the message.
	// */
	// private final org.eclipse.swt.widgets.Label label;
	//
	// /**
	// * Object that displays the load progress.
	// */
	// private final ImageSequencer loadProgress;
	//
	// /**
	// * The current message mode.
	// */
	// private Mode currentMode = Mode.NO_MESSAGE;
	//
	// public FeedBackMessage(final Composite parent, final int style) {
	// super(parent, style);
	//
	// setLayout(new GridLayout(1, true));
	// setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	//
	// // loadProgress = new ImageSequencer(this, SWT.NONE, new Image[] {
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_1),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_2),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_3),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_4),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_5),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_6),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_7),
	// // getImage(SampleToolBoxImageRegistry.IMG_INDICATOR_B_8), }, 80, true);
	// // loadProgress.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING,
	// true,
	// // false));
	//
	// label = new org.eclipse.swt.widgets.Label(this, SWT.None);
	// label.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true,
	// false));
	// label.setForeground(parent.getDisplay().getSystemColor(DEFAULT_LABEL_COLOR));
	// }
	//
	// private void setLoadProgressVisible(final boolean visible) {
	// // if (visible) {
	// // loadProgress.startSequence();
	// // loadProgress.setVisible(true);
	// // }
	// // else {
	// // loadProgress.stopSequence();
	// // loadProgress.setVisible(false);
	// // }
	// }
	//
	// public void setMode(Mode mode) {
	// if (mode == Mode.NO_MESSAGE) {
	// setLoadProgressVisible(false);
	//
	// label.setText("");
	// label.setVisible(false);
	// }
	// else if (mode == Mode.NO_IMAGE_AVAILABLE) {
	// setLoadProgressVisible(false);
	//
	// label.setText(NO_IMAGE_DEFINED_MESSAGE);
	// label.setVisible(true);
	//
	// }
	// else if (mode == Mode.LOADING) {
	// setLoadProgressVisible(true);
	//
	// label.setText(LOADING_MESSAGE);
	// label.setVisible(true);
	// }
	//
	// pack();
	// layout();
	//
	// currentMode = mode;
	// }
	//
	// public Mode getMode() {
	// return currentMode;
	// }
	//
	// @Override
	// public void setBackground(Color color) {
	// //loadProgress.setBackground(color);
	// label.setBackground(color);
	//
	// super.setBackground(color);
	// }
	//
	// @Override
	// public void setVisible(boolean visible) {
	// if (currentMode == Mode.LOADING) {
	// setLoadProgressVisible(visible);
	// }
	//
	// super.setVisible(visible);
	// }
	// }
	//
	// /**
	// * The feedback message object.
	// */
	// private FeedBackMessage feedBackMessage;

	/**
	 * Install the <tt>No image ready</tt> label. FIXME: Completed the render
	 * progress indicator!
	 */
	private void doInstallLabel() {
		// if (messagePaintListerner == null) {
		// messagePaintListerner = new PaintListener() {
		// public void paintControl(PaintEvent e) {
		// if (feedBackMessage != null) {
		// final Dimension d = getViewPortArea();
		// //Logger.getInstance().log(LogService.LOG_DEBUG,"Painting the label");
		//
		// feedBackMessage.layout();
		// feedBackMessage.setBackground(Display.getDefault()
		// .getSystemColor(SWT.COLOR_BLACK));
		// feedBackMessage.setSize(feedBackMessage.computeSize(SWT.DEFAULT,
		// SWT.DEFAULT));
		//
		// final org.eclipse.swt.graphics.Point o = xxx.Graphics
		// .centerIfPossible(feedBackMessage.getSize().x,
		// feedBackMessage.getSize().x, 0, 0, d.width,
		// d.height);
		// feedBackMessage.setLocation(o);
		// }
		// }
		// };
		// }
		//
		// imageCanvas.setImageData(null);
		// imageCanvas.addPaintListener(messagePaintListerner);
		//
		// feedBackMessage.setVisible(true);
	}

	/**
	 * Removes the <tt>No valid image</tt> label.
	 * 
	 * @see #doInstallLabel()
	 */
	private void doRemoveLabel() {
		// feedBackMessage.setVisible(false);
		// imageCanvas.removePaintListener(messagePaintListerner);
	}

	/**
	 * Call this to initiate the floor plan rendering, which is done
	 * asynchronously.
	 * 
	 * @param renderErrorHandling
	 *            Error handling strategy
	 */
	private void doBackgroundRender(final OnRenderError renderErrorHandling) {
		final WMFImageRenderer renderer = getRenderer();
		if (renderer == null) {
			return;
		}

		// validate max zoom
		if (!renderer.validateMaxZoom(zoomScale)) {
			rollbackBackgroundRender(null, OnRenderError.CONTINUE);
			return;
		}

		backgroundImageReady = false;

		// make sure that the image is rendered with the most up to date
		// background color.
		renderer.setBackgroundColor(getBackgroundColor());

		renderer.renderImage(zoomScale, null,
				new WMFImageRenderer.RenderingListener() {
					public void imageRendered(ImageData swtImageData,
							double drawingScale, boolean finished) {
						try {
							imageCanvas.setImageData(swtImageData);
						} catch (OutOfMemoryError ex) {
							// Out of memory Errors are expected here if the
							// zoom level
							// is too large for the available memory, so handle
							// them.
							if (finished) {
								// if ran out of memory in the last render,
								// abort
								rollbackBackgroundRender(ex,
										renderErrorHandling);
								return;
							} else {
								// if ran out of memory in a incremental render,
								// log but ignore
								Logger.getInstance()
										.log(LogService.LOG_ERROR,
												"Out of memory in incremental render step (2)");
								return;
							}
						} catch (Exception ex) {
							if (finished) {
								// unexpected error in last render, abort
								rollbackBackgroundRender(ex,
										renderErrorHandling);
								return;
							} else {
								// unexpected error in a incremental render, log
								// but ignore
								Logger.getInstance().log(LogService.LOG_ERROR,
										"Error in incremental render step (2)",
										ex);
								return;
							}
						}

						doRemoveLabel();

						backgroundImageReady = true;

						handleImageReady();
						handleSetZoomRatio(zoomScale);

						fireImageReadyEvent();
					}

					public void renderError(Throwable cause) {
						rollbackBackgroundRender(cause, renderErrorHandling);
					}
				});
	}

	/**
	 * Called to handle errors that happen during the floorplan rendering.
	 * 
	 * @param cause
	 *            The exception that caused the rendering to fail, or null if
	 *            the class state failed a sanity check (e.g. excessively large
	 *            value of zoomScale)
	 * @param renderErrorHandling
	 *            Error handling strategy
	 */
	private void rollbackBackgroundRender(final Throwable cause,
			final OnRenderError renderErrorHandling) {

		// Logger.getInstance().log(LogService.LOG_DEBUG,"*** rollbackBackgroundRender " +
		// renderErrorHandling);

		// decide how to present the error
		if (cause == null) {
			MessageDialog
					.openWarning(
							getShell(),
							Messages.getString("BaseFloorFigure.error.zoomExceeded.title"),
							Messages.getString("BaseFloorFigure.error.zoomExceeded.unsupported"));
		} else if (cause instanceof OutOfMemoryError) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Out of memory while rendering floor figure"); //$NON-NLS-1$
			MessageDialog
					.openWarning(
							getShell(),
							Messages.getString("BaseFloorFigure.error.zoomExceeded.title"),
							Messages.getString("BaseFloorFigure.error.zoomExceeded.outOfMemory"));
		} else {
			Logger.getInstance()
					.log(LogService.LOG_ERROR,
							Messages.getString("BaseFloorFigure.error.zoomExceeded.error"), cause); //$NON-NLS-1$
		}

		switch (renderErrorHandling) {
		case ZOOM_TO_PREVIOUS:
			// restore the previous zoom level
			zoomScale = previousZoomScale;

			backgroundImageReady = false;
			doBackgroundRender(OnRenderError.ZOOM_TO_100);
			break;

		case ZOOM_TO_100:
			// set the zoom to 100%
			zoomScale = 1.0;

			backgroundImageReady = false;
			doBackgroundRender(OnRenderError.FAIL);
			break;

		case CONTINUE:
			// restore the previous zoom level, but re-rendering is not
			// necessary
			zoomScale = previousZoomScale;

			backgroundImageReady = true;

			handleImageReady();
			handleSetZoomRatio(zoomScale);

			fireImageReadyEvent();
			break;

		case FAIL:
			// just give up...
			break;

		default:
			throw new InvalidParameterException("renderErrorHandling="
					+ renderErrorHandling);
		}
	}

	/**
	 * Notifies the image ready event listeners that a new image is ready on the
	 * canvas.
	 */
	private void fireImageReadyEvent() {
		final ImageReadyListener[] listeners = imageReadyListeners
				.toArray(new ImageReadyListener[0]);

		for (int i = 0; i < listeners.length; i++) {
			try {
				listeners[i].imageReady(this, imageCanvas);
			} catch (final Exception e) {
				Logger.getInstance().log(LogService.LOG_ERROR,
						"Exception notifying an ImageReadyListener", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Gets the renderer.
	 * 
	 * @return the renderer
	 */
	private WMFImageRenderer getRenderer() {
		if (!isWmfLoaded()) {
			return null;
		} else {
			synchronized (imageRendererLock) {
				if (mainImageRenderer == null) {
					Logger.getInstance().log(LogService.LOG_ERROR,"IMAGE OBJECT LOADING");
					/*
					 * wait for ongoing load to finish
					 */
					wmfObjectLoadedMutex.waitLoadFinished();

					mainImageRenderer = new WMFImageRenderer(wmfObject,
							getBackgroundColor());

					if (DIAGNOSTIC) {
						Logger.getInstance().log(LogService.LOG_DEBUG,"Renderer "
								+ mainImageRenderer.toString() + " allocated");
					}
				}
			}

			return mainImageRenderer;
		}
	}

	/**
	 * Disposes the current renderer stopping any undergoing rendering
	 * operation.
	 */
	private void disposeRender() {
		Logger.getInstance().log(LogService.LOG_ERROR,"RENDERING ATTEMPTED");
		synchronized (imageRendererLock) {
			if (mainImageRenderer != null) {
				mainImageRenderer.cancelRendering();
				mainImageRenderer = null;
				Logger.getInstance().log(LogService.LOG_ERROR,"RENDERING CANCELED");
			}
		}
	}

	/**
	 * Aborts the current rendering operation but does not dispose the renderer.
	 */
	private void abortRendering() {
		synchronized (imageRendererLock) {
			if (mainImageRenderer != null) {
				mainImageRenderer.cancelRendering();
			}
		}
	}

	/**
	 * Refreshes the image by aborting any on-going rendering operation and
	 * re-rendering it again.
	 * 
	 * @param renderErrorHandling
	 *            the render error handling
	 */
	private void refreshInternal(final OnRenderError renderErrorHandling) {
		abortRendering();
		doBackgroundRender(renderErrorHandling);
	}

	/**
	 * Checks if the image is showing.
	 * <p>
	 * An image is ready once it has been loaded and finished rendering. This
	 * method returns <code>false</code> from the moment that
	 * {@link #doBackgroundRender(OnRenderError)} has been called until the
	 * moment right before firing the image ready events.
	 * <p>
	 * If the renderer has never been called, returns <code>false</code>.
	 * 
	 * @return <code>true</code> if the background image is ready, and
	 *         <code>false</code> otherwise.
	 */
	public final boolean isImageReady() {
		return isWmfLoaded() && backgroundImageReady;
	}

	/**
	 * Adds the image ready listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public final void addImageReadyListener(final ImageReadyListener listener) {
		imageReadyListeners.add(listener);
	}

	/**
	 * Redraw.
	 */
	public final void redraw() {
		imageCanvas.redraw();
	}

	/**
	 * Removes the image ready listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public final void removeImageReadyListener(final ImageReadyListener listener) {
		imageReadyListeners.remove(listener);
	}

	/**
	 * Load wmf.
	 * 
	 * @param stream
	 *            the stream
	 * @param monitor
	 *            the monitor
	 */
	protected final void loadWMF(final InputStream stream,
			final IProgressMonitor monitor) {
		final ActionListener loadProgressListener = new java.awt.event.ActionListener() {
			private int lastLoaded;

			public void actionPerformed(final ActionEvent e) {
				if (WMF2Object.SIZELOADED
						.equalsIgnoreCase(e.getActionCommand())) {
					if (monitor != null) {
						monitor.beginTask(
								Messages.getString("BaseFloorFigure.loadingBlueprintImage"), wmfObject //$NON-NLS-1$
										.getLoadSize());

					}

					// Display.getDefault().asyncExec(new Runnable() {
					// public void run() {
					// feedBackMessage.setMode(FeedBackMessage.Mode.LOADING);
					// feedBackMessage.redraw();
					// }
					// });

					// Logger.getInstance().log(LogService.LOG_DEBUG,"LOADED:" + wmfObject.getLoadSize());
					// System.out.flush();
				} else if (WMF2Object.HASLOADED.equalsIgnoreCase(e
						.getActionCommand())) {
					if (monitor != null) {
						monitor.worked(wmfObject.getBytesLoaded() - lastLoaded);
					}
					//
					// Display.getDefault().asyncExec(new Runnable() {
					// public void run() {
					// feedBackMessage.redraw();
					// }
					// });

					// Logger.getInstance().log(LogService.LOG_DEBUG,"LOADING:" +
					// wmfObject.getBytesLoaded());
					lastLoaded = wmfObject.getBytesLoaded();
				} else if (WMF2Object.LOADFINISHED.equalsIgnoreCase(e
						.getActionCommand())) {
					wmfObjectLoadedMutex.notifyLoadFinished();

					if (monitor != null) {
						monitor.done();
					}
				}
			}
		};

		// /*
		// * Try background load
		// */
		// final Thread backgroundLoader = new Thread("Image Loader") { //
		// $NON-NLS-1
		// @Override
		// public void run() {
		// wmfObject = new WMF2Object(stream);
		// wmfObject.addActionListener(loadProgressListener);
		//
		// Display.getDefault().asyncExec(new Runnable() {
		// public void run() {
		// doRemoveLabel();
		// doBackgroundRender();
		// }
		// });
		// }
		// };
		//
		// backgroundLoader.start();

		wmfObject = new WMF2Object(stream);
		wmfObject.addActionListener(loadProgressListener);
	}

	/**
	 * Loads a buffered image from a WMF or BMP file.
	 * <p>
	 * Used for testing purposes. The stream should be supplied by calling
	 * 
	 * @param filePath
	 *            the file path
	 * @param monitor
	 *            the monitor
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link #setFigureData(byte[])}.
	 */
	protected final void loadWMF(final String filePath,
			final IProgressMonitor monitor) throws IOException {
		final File file = new File(filePath);

		InputStream fileReader = null;
		try {
			fileReader = new FileInputStream(file);
		} catch (IOException e) {
			if (monitor != null) {
				monitor.setCanceled(true);
			}
			throw e;
		}

		loadWMF(fileReader, monitor);
	}

	/**
	 * Gets the shell.
	 * 
	 * @return the shell
	 */
	protected final Shell getShell() {
		return imageCanvas.getShell();
	}

	/**
	 * Obtains the area of the viewport.
	 * 
	 * @return a rectangle with the client area of the parent or a 0-sized
	 *         rectangle if the parent is not set for this figure.
	 */
	protected final Rectangle getViewportArea() {
		final Rectangle clientArea;
		if (imageCanvas != null) {
			return new Rectangle(imageCanvas.getBounds());
		} else {
			clientArea = new Rectangle(0, 0, 0, 0);
		}
		return clientArea;
	}

	// /**
	// * Obtains the area of the viewport.
	// * <p>
	// * Returns the client are a size if no image is being displayed. If the
	// * image is being displayed it returns the bounds determined by the
	// maximum
	// * with and height of the client area vs image size.
	// *
	// * @return the dimensions of the figure
	// */
	// private final Dimension getViewPortArea() {
	// final Rectangle clientArea = getViewportArea();
	// return new Dimension(clientArea.width, clientArea.width);
	// }

	/**
	 * Gets the image canvas where rendering take place.
	 * 
	 * @return the image canvas where the floor plans are rendered.
	 */
	public final SWTImageCanvas getCanvas() {
		return imageCanvas;
	}

	/**
	 * Reacts to making the background image available.
	 */
	protected abstract void handleImageReady();

	/**
	 * Reacts to the change of visibility.
	 * 
	 * @param visible
	 *            whether the current figure should be visible or not.
	 */
	protected abstract void handleSetVisible(final boolean visible);

	/**
	 * Reacts to zoom level adjustments.
	 * <p>
	 * This method is called each time zoom level is changed or a new image is
	 * rendered. Descending classes should implement this method in order to
	 * adjustment to the new zoom level.
	 * 
	 * @param scale
	 *            the new zoom level specified.
	 */
	protected abstract void handleSetZoomRatio(final double scale);

	/**
	 * Gets the background color.
	 * 
	 * @return the background color
	 */
	public final Color getBackgroundColor() {
		final Color background = imageCanvas.getBackground();
		if (background == null) {
			final Color defaultColor = imageCanvas.getDisplay().getSystemColor(
					DEFAULT_BACKGROUND_COLOR);
			return defaultColor;
		} else {
			return background;
		}
	}

	/**
	 * Gets the scale at which the image is rendered.
	 * 
	 * @return the current zoom level
	 */
	public final double getZoomScale() {
		return zoomScale;
	}

	/**
	 * Checks if is wmf loaded.
	 * 
	 * @return true, if is wmf loaded
	 */
	public final boolean isWmfLoaded() {
		return figureData != null && wmfObject != null;
	}

	/**
	 * Computes the zoom level that can be used to perform zoom to fit.
	 * 
	 * @return the zoom level that performs a zoom to fit using the bounding box
	 *         and the and screen size.
	 */
	public final double getZoomToFitLevel() {
		final WMFImageRenderer renderer = getRenderer();
		if (renderer != null) {
			final org.eclipse.swt.graphics.Rectangle box = renderer
					.getBoundingBox();
			if (box != null) {
				final Rectangle clientRect = getViewportArea();
				final float zx = box.width * 1.0f / clientRect.width;
				final float zy = box.height * 1.0f / clientRect.height;
				return 1 / Math.max(zx, zy);
			}
		}
		return 0.0;
	}

	/**
	 * Checks if is visible.
	 * 
	 * @return true, if is visible
	 */
	public final boolean isVisible() {
		return figureIsVisible;
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the new visible
	 */
	public final void setVisible(final boolean visible) {
		if (visible == figureIsVisible) {
			return;
		}

		figureIsVisible = visible;
		if (visible) {
			if (isWmfLoaded()) {
				doRemoveLabel();
				doBackgroundRender(OnRenderError.ZOOM_TO_100);
			} else {
				doInstallLabel();
			}
		} else {
			abortRendering();
			doRemoveLabel();
			imageCanvas.setImageData(null);
		}

		handleSetVisible(visible);
	}

	/**
	 * Sets the background color.
	 * 
	 * @param bg
	 *            the background color, should not be <code>null</code>
	 */
	public final void setBackgroundColor(final Color bg) {
		if (bg != null) {
			if (!getBackgroundColor().equals(bg)) {
				imageCanvas.setBackground(bg);
				refreshInternal(OnRenderError.ZOOM_TO_100);
			}
		}
	}

	/**
	 * Sets the data to load the image from.
	 * 
	 * @param wmfData
	 *            the data read from the WMF file.
	 */
	public final void setFigureData(final byte[] wmfData) {
		disposeRender();

		figureData = wmfData;

		if (wmfData != null) {
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(
					wmfData);
			loadWMF(inputStream, null);
		} else {
			setVisible(false);
			redraw();
		}
	}

	/**
	 * Gets the byte array where the figure was read from.
	 * 
	 * @return the data used to load the image.
	 */
	public final byte[] getFigureData() {
		return figureData;
	}

	/**
	 * Changes the current zoom scale of the figure.
	 * <p>
	 * Each time a new scale is set, the background image is rendered and
	 * 
	 * @param newScale
	 *            the new zoom scale, must be positive
	 *            {@link #handleSetZoomRatio(double)} is called.
	 */
	public final void setZoomScale(final double newScale) {
		if (newScale != zoomScale && newScale > 0.0f) {
			previousZoomScale = zoomScale;
			zoomScale = newScale;

			refreshInternal(OnRenderError.ZOOM_TO_PREVIOUS);
		}
	}

}
