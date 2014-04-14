package lumina.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Listens to mouse wheel events.
 * <p>
 * When the mouse enters the area of a given composite, all mouse wheel events
 * are captured and redirected to that composite.
 * <p>
 * If the user is pressing <tt>SHIFT</tt>, an horizontal scroll notification is
 * sent.
 * <p>
 * If the user is pressing <tt>CTRL</tt>, a zoom notification is sent.
 */
public class MouseWheelEventListener {

	/**
	 * Composite that is used for detecting the mouse wheel events.
	 */
	private final Composite enterExitComposite;

	/**
	 * The event listener that forwards the mouse whell events to the event
	 * listener methods.
	 */
	private final Listener wheelEventListener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.MouseEnter:
				enterExitComposite.getDisplay().addFilter(SWT.MouseWheel, this);
				break;
			case SWT.MouseWheel:
				final int direction = event.count > 0 ? -1 : 1;

				if (event.stateMask == SWT.NONE || event.stateMask == SWT.SHIFT) {
					if (event.stateMask == SWT.NONE) {
						scrollVertically(direction);
					} else if (event.stateMask == SWT.SHIFT) {
						scrollHorizontally(direction);
					}

					event.doit = false;
				} else if (event.stateMask == SWT.CTRL) {
					/*
					 * CTRL + MouseWheel means ZOOM
					 */
					zoom(direction);
				}
				break;
			case SWT.MouseExit:
				enterExitComposite.getDisplay().removeFilter(SWT.MouseWheel,
						this);
				break;
			}
		}
	};

	/**
	 * Builds the event listener for a given composite.
	 * 
	 * @param composite
	 *            the composite to which the wheel listener must be added.
	 */
	public MouseWheelEventListener(Composite composite) {
		enterExitComposite = composite;

		enterExitComposite.addListener(SWT.MouseEnter, wheelEventListener);
		enterExitComposite.addListener(SWT.MouseExit, wheelEventListener);
	}

	/**
	 * Dispose the wheel event listener. Removes the internal listeners added to
	 * the composite.
	 */
	public void dispose() {
		enterExitComposite.removeListener(SWT.MouseExit, wheelEventListener);
		enterExitComposite.removeListener(SWT.MouseEnter, wheelEventListener);
	}

	/**
	 * Scroll the image vertically.
	 * <p>
	 * Descending classes must override this method and provide the appropriate
	 * vertical scroll behavior.
	 * 
	 * @param direction
	 *            the direction vector; value of <tt>-1</tt> is a decrement and
	 *            a value of <tt>1</tt> is an increment.
	 */
	public void scrollVertically(final int direction) {
	}

	/**
	 * Scroll the image horizontally.
	 * <p>
	 * Descending classes must override this method and provide the appropriate
	 * horizontally scroll behavior.
	 * 
	 * @param direction
	 *            the direction vector; value of <tt>-1</tt> is a decrement and
	 *            a value of <tt>1</tt> is an increment.
	 */
	public void scrollHorizontally(final int direction) {
	}

	/**
	 * Zoom the image.
	 * <p>
	 * Descending classes must override this method and provide the appropriate
	 * zoom behavior.
	 * 
	 * @param direction
	 *            the direction of the zoom; value of <tt>-1</tt> is a zoom out
	 *            and a value of <tt>1</tt> is a zoom in.
	 */
	public void zoom(final int direction) {
	}
}
