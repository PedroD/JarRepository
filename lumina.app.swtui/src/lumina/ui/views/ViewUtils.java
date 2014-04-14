package lumina.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.swtdesigner.SWTResourceManager;

/**
 * Utility class for views.
 * 
 * @author Paulo Carreira
 */
public final class ViewUtils {

	private static final int DEFAULT_LABEL_FONT_SIZE = 13;

	/**
	 * Font registry key for the panel warning messages.
	 */
	public static final String SELECTION_WARNING_FONT = "Tahoma";

	/**
	 * The color of the panel warning messages.
	 */
	public static final int WARNING_MESSAGE_FOREGROUND_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;

	/**
	 * Prevent instantiation.
	 */
	private ViewUtils() {
	}

	/**
	 * Method that creates a label to display a warning when no item is
	 * selected.
	 * 
	 * @param parent
	 *            the composite where the label is to be placed
	 * @param message
	 *            label message
	 */
	public static void createLabelPane(final Composite parent,
			final String message) {
		parent.setLayout(new GridLayout());
		final GridData centering = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(centering);

		final CLabel labelNoDeviceSelected = new CLabel(parent, SWT.CENTER);
		labelNoDeviceSelected.setText(message);

		labelNoDeviceSelected.setFont(SWTResourceManager.getFont(
				SELECTION_WARNING_FONT, DEFAULT_LABEL_FONT_SIZE, SWT.BOLD,
				false, false));

		final Color gray = parent.getDisplay().getSystemColor(
				WARNING_MESSAGE_FOREGROUND_COLOR);
		labelNoDeviceSelected.setForeground(gray);

		final GridData gdDeviceNameLabel = new GridData(SWT.CENTER, SWT.CENTER,
				true, true);
		labelNoDeviceSelected.setLayoutData(gdDeviceNameLabel);

		parent.layout(true, false);
	}

	/**
	 * Disposes all the existing controls of a composite.
	 * 
	 * @param top
	 *            composite to dispose
	 */
	public static void disposeExistingControls(final Composite top) {
		assert top != null;

		/*
		 * Dispose all children
		 */
		while (!top.isDisposed() && top.getChildren().length > 0) {
			final Control c = top.getChildren()[0];
			// Logger.getInstance().log(LogService.LOG_DEBUG,"CONTROL:" + c.getClass().getCanonicalName());
			// System.out.flush();
			if (!c.isDisposed()) {
				c.dispose();
			}
		}
	}

}
