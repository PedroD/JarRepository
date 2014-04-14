package lumina.ui.swt;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * A scrollable image canvas that extends org.eclipse.swt.graphics.Canvas.
 */
public final class SWTImageCanvas extends Canvas implements PaintListener {

	/**
	 * Constant used to get the size of the increments in the scroll bar.
	 */
	private static final int SCROLLBAR_INCREMENT_GRANULARITY = 100;

	/**
	 * The background color.
	 */
	private Color bgColor;

	/**
	 * The background image.
	 */
	private Image image;

	/**
	 * Background image width.
	 */
	private int imageWidth;

	/**
	 * Background image height.
	 */
	private int imageHeight;

	/**
	 * Border width; > 0 when the viewport is wider than the background image.
	 */
	private int borderWidth;

	/**
	 * Border height; > 0 when the viewport is taller than the background image.
	 */
	private int borderHeight;

	/** Viewport scroll. */
	private Point translate = new Point(0, 0);

	/**
	 * The mouse wheel event listener to be disposed later.
	 */
	private final MouseWheelEventListener mouseWheelEventListener;

	/**
	 * Records the size of the last client area to detect client area size
	 * changes.
	 */
	private Rectangle lastClientArea;

	/**
	 * Flag that indicates whether the scroll whether the current change to the
	 * status of the canvas implies the notification of the event listeners.
	 */
	private boolean mustNotifyMotionEventListeners;

	/**
	 * Motion listeners for the viewport to be notified when the viewport is
	 * moved.
	 */
	private final Set<ViewportMotionListener> moveListeners = new HashSet<ViewportMotionListener>();

	/**
	 * Creates a new SWTImageCanvas.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public SWTImageCanvas(final Composite parent) {
		this(parent, SWT.NULL);
	}

	/**
	 * Constructor for ScrollableCanvas.
	 * 
	 * @param parent
	 *            the parent of this control.
	 * @param style
	 *            the style of this control.
	 */
	public SWTImageCanvas(final Composite parent, int style) {
		super(parent, style | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.NO_BACKGROUND);

		final Color black = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		bgColor = black;
		
		/*
		 * Resize listener.
		 */
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				syncScrollBars();
			}
		});

		/*
		 * paint listener.
		 */
		addPaintListener(this);

		/*
		 * Init Horizontal scroll bar
		 */
		final ScrollBar horizontal = getHorizontalBar();

		horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
				mustNotifyMotionEventListeners = true;
			}
		});

		/*
		 * Init Vertical scroll bar
		 */
		final ScrollBar vertical = getVerticalBar();
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
				mustNotifyMotionEventListeners = true;
			}
		});

		/*
		 * Mouse wheel listener
		 */
		mouseWheelEventListener = new MouseWheelEventListener(this) {
			/**
			 * The value used to adjust the scroll speed (scroll vector).
			 */
			private static final int SCROLL_MULTIPLIER = 3;

			@Override
			public void scrollHorizontally(int direction) {
				if (canScrollHorizontally()) {
					mustNotifyMotionEventListeners = true;
					SWTImageCanvas.this.scrollHorizontally(direction
							* SCROLL_MULTIPLIER);
				}
			}

			@Override
			public void scrollVertically(int direction) {
				if (canScrollVertically()) {
					mustNotifyMotionEventListeners = true;
					SWTImageCanvas.this.scrollVertically(direction
							* SCROLL_MULTIPLIER);
				}
			}
		};
	}

	/**
	 * Scroll horizontally.
	 */
	private void scrollHorizontally(final ScrollBar hScrollBar) {
		if (!hasValidImage()) {
			return;
		}

		int tx = translate.x;
		int select = -hScrollBar.getSelection();

		scroll((select - tx), 0, 0, 0, getClientArea().width,
				getClientArea().height, false);

		translate.x = select;
	}

	/**
	 * Scroll vertically.
	 */
	private void scrollVertically(final ScrollBar vScrollBar) {
		if (!hasValidImage()) {
			return;
		}

		int ty = translate.y;
		int select = -vScrollBar.getSelection();

		scroll(0, (select - ty), 0, 0, getClientArea().width,
				getClientArea().height, false);

		translate.y = select;
	}

	private void setImageDataInternal(final ImageData data, final Rectangle area) {
		if (image != null && !image.isDisposed()) {
			image.dispose();
			image = null;
		}

		if (data != null) {
			imageWidth = data.width;
			imageHeight = data.height;

			borderWidth = Math.max(area.width - imageWidth, 0);
			borderHeight = Math.max(area.height - imageHeight, 0);

			image = new Image(getDisplay(), data);
		} else {
			imageWidth = 0;
			imageHeight = 0;
		}

		assert image != null : "Border-safe image is assigned";
		assert !image.isDisposed() : "Border safe image is valid";
	}

	/**
	 * Interface for listeners of viewport motion events.
	 * <p>
	 * Viewport motion occurs when the canvas is scrolled or when the the canvas
	 * image is zoomed to a size smaller than the viewport size.
	 * 
	 * @author Paulo Carreira
	 */
	public interface ViewportMotionListener {
		void moveTo(final int x, final int y, final int width, final int height);
	};

	/**
	 * Defines the background color.
	 * 
	 * @param color
	 *            background color
	 */
	public void setBackground(Color color) {
		bgColor = color;
	}

	/**
	 * Returns the background color.
	 * 
	 * @return background color
	 */
	public Color getBackground() {
		return bgColor;
	}

	/**
	 * Free the resources of the source image an of the source image.
	 */
	public void dispose() {
		if (image != null && !image.isDisposed())
			image.dispose();

		mouseWheelEventListener.dispose();
	}

	/**
	 * Adds move listener.
	 * 
	 * @param listener
	 *            viewport motion listener
	 */
	public final void addMoveListener(final ViewportMotionListener listener) {
		moveListeners.add(listener);
	}

	/**
	 * Centers.
	 * 
	 * @param cx
	 *            x coordinate
	 * @param cy
	 *            y coordinate
	 */
	public void center(final int cx, final int cy) {
		final int width = getClientArea().width;
		final int height = getClientArea().height;
		scrollTo(-cx + width / 2, -cy + height / 2);
	}

	/**
	 * Checks if it can scroll horizontally.
	 * 
	 * @return true if it can scroll horizontally, false otherwise
	 */
	public boolean canScrollHorizontally() {
		if (hasValidImage()) {
			final Rectangle clientArea = getClientArea();
			final boolean imageWiderThanViewport = (imageWidth + borderWidth) > clientArea.width;
			return imageWiderThanViewport;
		} else {
			return false;
		}
	}

	/**
	 * Checks if it can scroll vertically.
	 * 
	 * @return true if it can scroll vertically, false otherwise
	 */
	public boolean canScrollVertically() {
		if (hasValidImage()) {
			final Rectangle clientArea = getClientArea();
			final boolean imageTallerThanViewport = (imageHeight + borderHeight) > clientArea.height;
			return imageTallerThanViewport;
		} else {
			return false;
		}
	}

	private void fireMoveEvent(final int tx, final int ty, final int width,
			final int height) {
		for (ViewportMotionListener l : moveListeners) {
			l.moveTo(tx, ty, width, height);
		}
	}

	/**
	 * Obtains the resolution of the image being displayed.
	 * 
	 * @return a {@link Point} with the width and height of the image or
	 *         <code>null</code> is not valid image exists.
	 * @see #hasValidImage()
	 */
	public Point getImageResolution() {
		if (hasValidImage()) {
			return new Point(imageWidth, imageHeight);
		} else {
			return null;
		}
	}

	/**
	 * Returns the new point after the scroll.
	 * 
	 * @return new scrolled point
	 */
	public Point getScroll() {
		return new Point(translate.x, translate.y);
	}

	/**
	 * Checks if the canvas is displaying an image.
	 * 
	 * @return <code>true</code> if an image is being displayed.
	 */
	public boolean hasValidImage() {
		return !(image == null || image.isDisposed());
	}

	/**
	 * Paints the control.
	 * 
	 * @param event
	 *            paint event
	 */
	public void paintControl(final PaintEvent event) {
		/*
		 * /!\ WARNING: Extreme care must be taken when changing this method.
		 * Paint messages can be triggered indirectly resulting on the CPU
		 * occupation to reach 100%.
		 */
		if (hasValidImage()) {
			final Rectangle clientArea = getClientArea();
			final int viewPortWidth = clientArea.width;
			final int viewPortHeight = clientArea.height;

			if (lastClientArea == null || !clientArea.equals(lastClientArea)) {
				// viewport size changed
				borderWidth = Math.max(viewPortWidth - imageWidth, 0);
				borderHeight = Math.max(viewPortHeight - imageHeight, 0);

				mustNotifyMotionEventListeners = true;
				syncScrollBars();

				lastClientArea = clientArea;
			}

			// Logger.getInstance().log(LogService.LOG_DEBUG,"*** in paintControl " +
			// event.gc.getClipping());

			// there are borders around the image, fill with the bg color
			if (borderWidth > 0 || borderHeight > 0) {
				event.gc.setBackground(bgColor);
				event.gc.fillRectangle(0, 0, viewPortWidth, viewPortHeight);
			}

			final int srcX = Math.max(-translate.x, 0);
			final int srcY = Math.max(-translate.y, 0);
			final int srcWidth = Math.min(viewPortWidth, imageWidth);
			final int srcHeight = Math.min(viewPortHeight, imageHeight);

			final int destX = borderWidth / 2;
			final int destY = borderHeight / 2;
			final int destWidth = srcWidth;
			final int destHeight = srcHeight;

			event.gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, destX,
					destY, destWidth, destHeight);

			if (mustNotifyMotionEventListeners) {
				fireMoveEvent(srcX - (borderWidth / 2), srcY
						- (borderHeight / 2), imageWidth, imageHeight);
				mustNotifyMotionEventListeners = false;
			}
		} else {
			// Logger.getInstance().log(LogService.LOG_DEBUG,"*** in paintControl NOIMAGE " +
			// event.gc.getClipping());

			final Rectangle clientRect = getClientArea();
			event.gc.setBackground(bgColor);
			event.gc.setClipping(clientRect);
			event.gc.fillRectangle(clientRect);
		}
	}

	/**
	 * Removes the move listener.
	 * 
	 * @param listener
	 *            viewport motion listener
	 */
	public final void removeMoveListener(final ViewportMotionListener listener) {
		if (!moveListeners.contains(listener)) {
			throw new IllegalArgumentException(
					"Scroll listener does not exist in the set of scroll listeners");
		}

		moveListeners.remove(listener);
	}

	/**
	 * Scrolls to a specified coordinate.
	 * 
	 * @param tx
	 *            new x coordinate
	 * @param ty
	 *            new y coordinate
	 */
	public void scrollTo(final int tx, final int ty) {
		translate.x = tx;
		translate.y = ty;

		mustNotifyMotionEventListeners = true;
		syncScrollBars();
		redraw();
	}

	/**
	 * Scrolls to a specified point.
	 * 
	 * @param p
	 *            new point
	 */
	public void scrollTo(final Point p) {
		translate.x = p.x;
		translate.y = p.y;

		mustNotifyMotionEventListeners = true;
		syncScrollBars();
		redraw();
	}

	/**
	 * Scroll the image canvas horizontally.
	 * <p>
	 * The parameter specified will be used to compute the increment to the
	 * scroll bar. A value of <tt>-1</tt> is a decrement and a value of
	 * <tt>1</tt> is an increment. Values with greater absolute values will
	 * result in greater increments. In practice, greater absolute values
	 * translate to faster scrolling.
	 * 
	 * @param direction
	 *            the direction vector.
	 */
	public void scrollHorizontally(final int direction) {
		final int increment = direction
				* this.getHorizontalBar().getIncrement();
		this.getHorizontalBar().setSelection(
				this.getHorizontalBar().getSelection() + increment);

		scrollHorizontally(this.getHorizontalBar());
	}

	/**
	 * Scroll the image canvas vertically.
	 * <p>
	 * The parameter specified will be used to compute the increment to the
	 * scroll bar. A value of <tt>-1</tt> is a decrement and a value of
	 * <tt>1</tt> is an increment. Values with greater absolute values will
	 * result in greater increments. In practice, greater absolute values
	 * translate to faster scrolling.
	 * 
	 * @param direction
	 *            the direction vector.
	 */
	public void scrollVertically(final int direction) {
		final int increment = direction * this.getVerticalBar().getIncrement();
		this.getVerticalBar().setSelection(
				this.getVerticalBar().getSelection() + increment);

		scrollVertically(this.getVerticalBar());
	}

	/**
	 * Reset the image data and update the image.
	 * 
	 * @param data
	 *            image data to be set
	 */
	public void setImageData(final ImageData data) {
		if (!this.isDisposed()) {
			setImageDataInternal(data, getClientArea());

			syncScrollBars();
			mustNotifyMotionEventListeners = true;
		}
	}

	/**
	 * Synchronize the scrollbar with the image.
	 */
	public void syncScrollBars() {
		if (!hasValidImage()) {
			redraw();
			return;
		} else {
			int tx = translate.x;
			int ty = translate.y;

			if (tx > 0) {
				tx = 0;
			}

			if (ty > 0) {
				ty = 0;
			}

			final int cw = getClientArea().width;
			final int ch = getClientArea().height;

			final ScrollBar hScrollBar = getHorizontalBar();
			hScrollBar.setIncrement(cw / SCROLLBAR_INCREMENT_GRANULARITY);
			hScrollBar.setPageIncrement(cw);

			final ScrollBar vScrollBar = getVerticalBar();
			vScrollBar.setIncrement(ch / SCROLLBAR_INCREMENT_GRANULARITY);
			vScrollBar.setPageIncrement(ch);

			if (imageWidth > cw) {
				/*
				 * image is wider than client area
				 */
				hScrollBar.setEnabled(true);

				/*
				 * compute the compensation to keep the zoom focused on the
				 * center of the image
				 */
				final int oldImageWidth = hScrollBar.getMaximum();
				final int dtx = (oldImageWidth - imageWidth) / 2;
				tx += dtx;

				final int maxHScroll = imageWidth - cw;
				if (-tx > maxHScroll) {
					tx = -maxHScroll;
				}

				if (tx > 0) {
					tx = 0;
				}
			} else {
				/*
				 * image is narrower than client area
				 */
				hScrollBar.setEnabled(false);
				tx = (cw - imageWidth) / 2; // center if too small.
			}

			hScrollBar.setMaximum(imageWidth);
			hScrollBar.setSelection(-tx);
			hScrollBar.setThumb(cw);

			if (imageHeight > ch) {
				/*
				 * image is taller than client area
				 */
				vScrollBar.setEnabled(true);

				/*
				 * compute the compensation to keep the zoom focused on the
				 * center of the image
				 */

				final int oldImageHeight = vScrollBar.getMaximum();
				final int dty = (oldImageHeight - imageHeight) / 2;
				ty += dty;

				final int maxVScroll = imageHeight - ch;
				if (-ty > maxVScroll) {
					ty = -maxVScroll;
				}

				if (ty > 0) {
					ty = 0;
				}
			} else {
				/*
				 * image is less tall than client area
				 */
				vScrollBar.setEnabled(false);
				ty = (ch - imageHeight) / 2; // center if too small.
			}
			vScrollBar.setMaximum(imageHeight);
			vScrollBar.setSelection(-ty);
			vScrollBar.setThumb(ch);

			/*
			 * update the coords translation
			 */
			translate.x = tx;
			translate.y = ty;
		}
	}
}
