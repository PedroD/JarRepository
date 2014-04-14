package lumina.ui.swt;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class that provides simple dialogs.
 */
public final class SimpleDialogs {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private SimpleDialogs() {
	}

	/**
	 * Show information.
	 * 
	 * @param title
	 *            dialog title
	 * @param message
	 *            information message
	 * @param blockOnOpen
	 *            block window in modal status
	 */
	public static void showInfo(final String title, final String message,
			final boolean blockOnOpen) {
		MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), title, null, message,
				MessageDialog.INFORMATION,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		msgDialog.setBlockOnOpen(blockOnOpen);
		msgDialog.open();
	}

	/**
	 * Show warning.
	 * 
	 * @param title
	 *            dialog title
	 * @param message
	 *            warning message
	 * @param blockOnOpen
	 *            block window in modal status
	 */
	public static void showWarn(final String title, final String message,
			final boolean blockOnOpen) {
		MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), title, null, message, MessageDialog.WARNING,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		msgDialog.setBlockOnOpen(blockOnOpen);
		msgDialog.open();
	}

	/**
	 * Show error.
	 * 
	 * @param title
	 *            dialog title
	 * @param message
	 *            warning message
	 * @param blockOnOpen
	 *            block window in modal status
	 */
	public static void showError(final String title, final String message,
			final boolean blockOnOpen) {
		MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), title, null, message, MessageDialog.ERROR,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		msgDialog.setBlockOnOpen(blockOnOpen);
		msgDialog.open();
	}

	/**
	 * Shows a yes/no question.
	 * 
	 * @param title
	 *            dialog title
	 * @param message
	 *            warning message
	 * @param blockOnOpen
	 *            block window in modal status
	 * @return true if user agrees with the question, false otherwise
	 */
	public static boolean showQuestion(final String title,
			final String message, final boolean blockOnOpen) {
		MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), title, null, message,
				MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL }, 1);
		msgDialog.setBlockOnOpen(blockOnOpen);
		return 0 == msgDialog.open();
	}

}
