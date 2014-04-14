package lumina.ui.swt;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Graphics utility class.
 */
public final class Graphics {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private Graphics() {
	}

	/**
	 * Computes the outset of a rectangle.
	 * 
	 * @param r
	 *            rectangle
	 * @param margin
	 *            margin
	 * @return outset rectangle
	 */
	public static Rectangle outset(final Rectangle r, final int margin) {
		return new Rectangle(r.x - margin, r.y - margin, r.width + 2 * margin,
				r.height + 2 * margin);
	}

	/**
	 * Scales an SWT point.
	 * 
	 * @param p
	 *            point to scale
	 * @param scale
	 *            scale factor
	 * @return scaled point
	 */
	public static Point scale(final Point p, final double scale) {
		return new Point((int) (scale * p.x), (int) (scale * p.y));
	}

	/**
	 * Scales an SWT rectangle.
	 * 
	 * @param r
	 *            rectangle to scale
	 * @param scale
	 *            scale factor
	 * @return scaled rectangle
	 */
	public static Rectangle scale(final Rectangle r, final double scale) {
		return new Rectangle((int) (scale * r.x), (int) (scale * r.y),
				(int) (scale * r.width), (int) (scale * r.height));
	}

	/**
	 * Converts an SWT rectangle to a DRAW2D rectangle.
	 * 
	 * @param r
	 *            rectangle
	 * @return rectangle
	 * @see org.eclipse.draw2d.geometry.Rectangle
	 */
	public static org.eclipse.draw2d.geometry.Rectangle toDraw2d(Rectangle r) {
		return new org.eclipse.draw2d.geometry.Rectangle(r);
	}

	/**
	 * Centers one rectangle within another if possible.
	 * <p>
	 * if the the inner rectangle wider than the outer rectangle it is aligned
	 * top-left.
	 * 
	 * @param innerWidth
	 *            inner width
	 * @param innerHeight
	 *            inner height
	 * @param outer
	 *            rectangle
	 * @return centered rectangle
	 */
	public static org.eclipse.swt.graphics.Point centerIfPossible(
			final int innerWidth, final int innerHeight, Rectangle outer) {
		return centerIfPossible(innerWidth, innerHeight, outer.x, outer.y,
				outer.width, outer.height);
	}

	/**
	 * Centers one rectangle within another if possible.
	 * <p>
	 * if the the inner rectangle wider than the outer rectangle it is aligned
	 * top-left.
	 * 
	 * @param innerWidth
	 *            inner width
	 * @param innerHeight
	 *            inner height
	 * @param outer
	 *            rectangle
	 * @return centered rectangle
	 */
	public static org.eclipse.swt.graphics.Point centerIfPossible(
			final int innerWidth, final int innerHeight,
			org.eclipse.draw2d.geometry.Rectangle outer) {
		return centerIfPossible(innerWidth, innerHeight, outer.x, outer.y,
				outer.width, outer.height);
	}

	/**
	 * Centers one rectangle within another if possible.
	 * <p>
	 * if the the inner rectangle wider than the outer rectangle it is aligned
	 * top-left.
	 * 
	 * @param innerWidth
	 *            inner width
	 * @param innerHeight
	 *            inner height
	 * @param outerXPos
	 *            the X coordinate of the outer and larger rectangle (if
	 *            possible)
	 * @param outerYPos
	 *            the Y coordinate of the outer and larger rectangle (if
	 *            possible)
	 * @param outerWidth
	 *            the width of the outer and larger rectangle (if possible)
	 * @param outerHeight
	 *            the height of the outer and larger rectangle (if possible)
	 * @return the top-left coordinates that center the inner rectangle within
	 *         the outer rectangle or <tt>(0, 0)</tt> if the inner rectangle is
	 *         wider that the outer rectangle
	 */
	public static org.eclipse.swt.graphics.Point centerIfPossible(
			final int innerWidth, final int innerHeight, final int outerXPos,
			final int outerYPos, final int outerWidth, final int outerHeight) {
		final int width;
		if (outerWidth > innerWidth) {
			width = outerWidth - innerWidth;
		} else {
			width = 0;
		}

		final int height;
		if (outerHeight > innerHeight) {
			height = outerHeight - innerHeight;
		} else {
			height = 0;
		}

		return new org.eclipse.swt.graphics.Point(outerXPos + width / 2,
				outerYPos + height / 2);
	}
}
