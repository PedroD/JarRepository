package lumina.ui.swt;

import org.eclipse.swt.widgets.Display;

/**
 * Clipboard utility class.
 */
public final class ClipboardUtils {

	/**
	 * Lazily-created clipboard object.
	 */
	private static org.eclipse.swt.dnd.Clipboard clipboard;

	/**
	 * Prevent instantiation.
	 */
	private ClipboardUtils() {
	}

	/**
	 * @return the clipboard associated with this view.
	 */
	public static synchronized org.eclipse.swt.dnd.Clipboard getClipboard() {
		if (clipboard == null) {
			clipboard = new org.eclipse.swt.dnd.Clipboard(Display.getDefault());
		}

		return clipboard;
	}

	/**
	 * Disposes clipboard.
	 */
	public static void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
		}
	}
}
