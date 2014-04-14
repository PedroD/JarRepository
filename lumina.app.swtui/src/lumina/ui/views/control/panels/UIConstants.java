package lumina.ui.views.control.panels;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.swtdesigner.SWTResourceManager;

/**
 * Constants that affect the UI of all devices.
 */
public final class UIConstants {

	/**
	 * The name of the font used for the title of the device.
	 */
	private static final String TITLE_FONT_NAME = "Tahoma"; //$NON-NLS-1$

	/**
	 * The font used to display the title.
	 */
	public static final Font TITLE_FONT = SWTResourceManager.getFont(
			TITLE_FONT_NAME, 10, SWT.BOLD, false, false);

	/**
	 * Image path of the application icon.
	 */
	public static final String APPLICATION_ICON_IMAGE_PATH = "/icons/application/icangraph-icons.ico"; //$NON-NLS-1$

	/**
	 * Image path of the warning sign to be displayed in the warning panel.
	 */
	public static final String WARNING_PANEL_IMAGE_PATH = "/icons/model/warning.png"; //$NON-NLS-1$

	/**
	 * Image path of the spreadsheet icon to be displayed in the panel.
	 */
	public static final String SPREADSHEET_PANEL_IMAGE_PATH = "/icons/dialogs/spreadsheet.png"; //$NON-NLS-1$

	/**
	 * Background color for the warning panel.
	 */
	public static final int WARNING_PANEL_BACKGROUND_COLOR = SWT.COLOR_WIDGET_DARK_SHADOW;

	/**
	 * Foreground color for the warning panel.
	 */
	public static final int WARNING_PANEL_FOREGROUND_COLOR = SWT.COLOR_WIDGET_LIGHT_SHADOW;

	/**
	 * The limit, in milliseconds, above which the the fade time is displayed.
	 */
	public static final int COMMAND_FADE_DURATION_TRESHOLD = 2000;

	/**
	 * The color of read-only text. Like disabled properties and non-editable
	 * text in tables.
	 */
	private static final int READ_ONLY_COLOR = SWT.COLOR_DARK_GRAY;

	/**
	 * Prevent instantiation.
	 */
	private UIConstants() {
	}

	/**
	 * @return the font for the title for the control panel
	 */
	public static Font getTitleFont() {
		return TITLE_FONT;
	}

	/**
	 * Gets the color for the device name in that appears on a panel.
	 * 
	 * @return the color for the title for the control panel
	 */
	public static Color getTitleColor() {
		final Display display = Display.getCurrent();
		return display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
	}

	/**
	 * Gets the color for property text that is read-only.
	 * 
	 * @return the color for disabled text
	 */
	public static Color getReadOnlyColor() {
		final Display display = Display.getCurrent();
		return display.getSystemColor(READ_ONLY_COLOR);
	}

	/**
	 * Gets the color to paint the property editors that have changed.
	 * <p>
	 * Each time a property changes, sometimes as a result of modifying another
	 * property, the color of the property editor changes to this color in order
	 * no notify the user.
	 * 
	 * @return a yellow color
	 */
	public static Color getChangedColor() {
		final Display display = Display.getCurrent();
		return display.getSystemColor(SWT.COLOR_YELLOW);
	}

}
