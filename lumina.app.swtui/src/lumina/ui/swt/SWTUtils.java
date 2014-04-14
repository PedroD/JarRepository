package lumina.ui.swt;

import lumina.kernel.Logger;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.log.LogService;

import codebase.os.SysUtil;

/**
 * Utilities to make SWT dialogs easier to manage.
 */
public final class SWTUtils {

	/**
	 * Password field character mask.
	 */
	private static final char PASSWORD_CHAR = '*';

	/**
	 * Password field character mask for MAC OS.
	 */
	private static final char MACOSX_PASSWORD_CHAR = '\u2a2a';

	private static final int MINIMUM_COLUMN_WIDTH = 20;

	private static final Color ERROR_COLOR;

	private static final Color GREY_COLOR;

	static {
		// CHECKSTYLE:OFF this is a static initializer for color!
		RGB greyRGB = new RGB(200, 200, 200);
		// CHECKSTYLE:ON
		ColorRegistry cm = JFaceResources.getColorRegistry();
		cm.put("grey", greyRGB); //$NON-NLS-1$
		GREY_COLOR = cm.get("grey"); //$NON-NLS-1$

		// CHECKSTYLE:OFF this is a static initializer for color!
		RGB errorRGB = new RGB(255, 255, 180);
		// CHECKSTYLE:ON
		cm.put("error", errorRGB); //$NON-NLS-1$
		ERROR_COLOR = cm.get("error"); //$NON-NLS-1$
	}

	/**
	 * Prevent instantiation.
	 */
	protected SWTUtils() {
	}

	/**
	 * Finds the selection index for a combo or list box.
	 * 
	 * @param current
	 *            the text of the current selection.
	 * @param options
	 *            an array with the available options.
	 * @return the index in the array where 'current' can be found, or
	 *         <tt>-1</tt> if not found.
	 */
	public static int findSelectionIndex(final String current,
			final String[] options) {
		if (options != null) {
			for (int i = 0; i < options.length; i++) {
				if (options[i] != null && options[i].equals(current)) {
					return i;
				}
			}
		}

		return -1;
	}

	/**
	 * Colors the background of a text widget based on the test value.
	 * 
	 * @param widget
	 *            the widget
	 * @param testValue
	 *            the test value
	 */
	public static void colorBackground(Text widget, String testValue) {
		if (widget.getText().equals(testValue)) {
			widget.setBackground(GREY_COLOR);
		} else {
			widget.setBackground(null);
		}
	}

	/**
	 * Sets the text of the text widget, but only if the value is non-null.
	 * 
	 * @param widget
	 *            the widget to set text for
	 * @param text
	 *            the text to set
	 */
	public static void setTextWidgetValue(Text widget, String text) {
		if (text == null) {
			return;
		} else {
			widget.setText(text);
		}
	}

	/**
	 * Tests if the widget value is empty. If so, it adds an error color to the
	 * background of the cell;
	 * 
	 * @param widget
	 *            the widget to set text for
	 * @return boolean
	 */
	public static boolean testWidgetValue(Text widget) {
		if (widget.getText() == null || "".equals(widget.getText())) { //$NON-NLS-1$
			widget.setBackground(ERROR_COLOR);
			final ModifyListener ml = new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					Text t = (Text) e.widget;
					if (t.getText() != null && !"".equals(t.getText())) { //$NON-NLS-1$
						t.setBackground(null);
					} else {
						t.setBackground(ERROR_COLOR);
					}
				}
			};
			widget.addModifyListener(ml);
			return false;
		}
		return true;
	}

	/**
	 * Tests if the widget value is empty. If so, it adds an error color to the
	 * background of the cell.
	 * 
	 * @param widget
	 *            the widget to set text for
	 * @param validSelectionIndex
	 *            the first item that is a "valid" selection
	 * @return boolean
	 */
	public static boolean testWidgetValue(Combo widget, int validSelectionIndex) {
		final int selectionIndex;
		if (validSelectionIndex > 0) {
			selectionIndex = validSelectionIndex;
		} else {
			selectionIndex = 0;
		}

		if (widget.getText() == null
				|| "".equals(widget.getText()) || widget.getSelectionIndex() < selectionIndex) //$NON-NLS-1$
		{
			widget.setBackground(ERROR_COLOR);
			final ModifyListener ml = new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					Combo t = (Combo) e.widget;
					if (t.getText() != null
							&& !"".equals(t.getText()) || t.getSelectionIndex() >= selectionIndex) //$NON-NLS-1$
					{
						t.setBackground(null);
					} else {
						t.setBackground(ERROR_COLOR);
					}
				}
			};
			widget.addModifyListener(ml);
			return false;
		}
		return true;
	}

	/**
	 * Centers the shell on screen, and re-packs it to the preferred size.
	 * Packing is necessary as otherwise dialogs tend to get cut off on the Mac.
	 * 
	 * @param shell
	 *            The shell to center
	 * @param parent
	 *            The shell to center within
	 */
	public static void centerAndPack(Shell shell, Shell parent) {
		center(shell, parent);
		shell.pack();
	}

	/**
	 * Centers the shell on screen.
	 * 
	 * @param shell
	 *            The shell to center
	 * @param parent
	 *            The shell to center within
	 */
	public static void center(Shell shell, Shell parent) {
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = shell.getBounds();

		int locationX, locationY;
		locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
		locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;
		shell.setLocation(new Point(locationX, locationY));
	}

	/**
	 * Echos out "*" characters when typing in a text field.
	 * <p>
	 * This simulates a SWT.PASSWORD field, which does not work on the Mac.
	 * 
	 * @param text
	 *            the password text
	 */
	public static void setTextAsPassword(Text text) {
		char cbit = PASSWORD_CHAR;
		if (SysUtil.getOperatingSystem() == SysUtil.OS.MAC_OS) {
			cbit = MACOSX_PASSWORD_CHAR;
		}
		text.setEchoChar(cbit);
	}

	/**
	 * Indicated that this field is a "default".
	 * <p>
	 * It contains a default value and is shaded unless a new value is entered
	 * 
	 * @param text
	 *            The text widget to designate as "default"
	 * @param defaultValue
	 *            The default string value to use
	 */
	public static void setFieldWithDefaultValue(final Text text,
			final String defaultValue) {
		text.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				SWTUtils.colorBackground(text, defaultValue);
			}
		});
		text.setText(defaultValue);
	}

	/**
	 * Sets a text box to enable/disable based on a button click.
	 * <p>
	 * Generally this is used to clear a text field based on a checkbox
	 * 
	 * @param button
	 *            the button to test for the selection state
	 * @param text
	 *            the text field to clear
	 * @param clearFieldOnDisable
	 *            do we blank out the text field if its disabled?
	 */
	public static void linkButtonAndField(final Button button, final Text text,
			final boolean clearFieldOnDisable) {
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (button.getSelection()) {
					text.setEnabled(true);
				} else {
					text.setEnabled(false);
					if (clearFieldOnDisable) {
						text.setText(""); //$NON-NLS-1$
					}
				}
			}
		});
	}

	/**
	 * Sets the columns of a table to percentage widths, rather than pixels.
	 * 
	 * @param table
	 *            The table to modify
	 * @param columnPercentages
	 *            The array of percentage widths (ex. 0.2 = 20%). Does not need
	 *            to equal 100%.
	 * @param columnMinimums
	 *            The array of minimum pixel widths for each column. 0 indicates
	 *            whatever width works.
	 */
	public static void setTableColumnWidths(Table table,
			double[] columnPercentages, int[] columnMinimums) {
		Rectangle area = table.getClientArea();
		if (area.width == 0) {
			return;
		}
		Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int sideSpacer = 0;
		int spacer = table.getGridLineWidth() + table.getBorderWidth() * 2;
		int width = area.width;
		if (preferredSize.y > area.height) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		TableColumn[] columns = table.getColumns();

		for (int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			int w = (int) (width * columnPercentages[i]);
			if (i < columns.length - 1) {
				w += sideSpacer;
			} else {
				w += spacer;
			}
			if (w < MINIMUM_COLUMN_WIDTH) {
				w = MINIMUM_COLUMN_WIDTH;
			}
			column.setWidth(w);
		}
	}

	/**
	 * Calculates the percentage width of each column in the table relative to
	 * the total table size.
	 * 
	 * @param table
	 *            the table to compute
	 * @param padding
	 *            a padding value to add to each table cell to space out the
	 *            display a bit
	 * @return the array of values
	 */
	public static double[] calculateColumnPercents(Table table, double padding) {
		int sideSpacer = 0;
		int spacer = table.getGridLineWidth() + table.getBorderWidth() * 2;
		TableColumn[] columns = table.getColumns();
		double[] newPercentages = new double[columns.length];

		double[] widths = new double[columns.length];
		double totalWidth = 0;
		for (int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			int width = column.getWidth();
			int spc = spacer;
			if (i < columns.length - 1) {
				spc = sideSpacer;
			}
			widths[i] = width - spc;
			totalWidth += widths[i];
		}

		for (int i = 0; i < columns.length; i++) {
			newPercentages[i] = (widths[i] / totalWidth) + padding;
		}

		return newPercentages;
	}

	/**
	 * Calculates the width of each column in the table.
	 * 
	 * @param table
	 *            the table to compute
	 * @return the array of values
	 */
	public static int[] calculateColumnWidths(Table table) {
		int sideSpacer = 0;
		TableColumn[] columns = table.getColumns();
		int spacer = table.getGridLineWidth() + table.getBorderWidth() * 2;
		int[] widths = new int[columns.length];
		for (int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			int width = column.getWidth();
			int spc = spacer;
			if (i < columns.length - 1) {
				spc = sideSpacer;
			}
			widths[i] = width - spc;
		}

		return widths;
	}

	/**
	 * Disposes the composite.
	 * 
	 * @param composite
	 *            composite to dispose
	 * @param disposeSelf
	 *            specifies if this should be disposed as well
	 */
	public static void disposeComposite(Composite composite, boolean disposeSelf) {
		if (composite == null || composite.isDisposed())
			return;
		/*
		 * Note: we must iterate by materializing the references first.
		 */
		final Control[] controls = composite.getChildren();
		for (int i = 0; i < controls.length; i++) {
			final Control control = controls[i];
			if (control != null && !control.isDisposed()) {
				if (control instanceof Composite) {
					disposeComposite((Composite) control, true);
				}
				try {
					control.dispose();
				} catch (SWTException e) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"Could not dispose composite", e);
				}
			}
		}

		// It's possible that the composite was destroyed by the child
		if (!composite.isDisposed() && disposeSelf)
			try {
				composite.dispose();
			} catch (SWTException e) {
				Logger.getInstance().log(LogService.LOG_ERROR,
						"Could not dispose composite", e);
			}
	}

	/**
	 * Disposes the composite.
	 * 
	 * @param composite
	 *            composite to dispose
	 */
	public static void disposeComposite(Composite composite) {
		disposeComposite(composite, true);
	}

	/**
	 * Sets the background color recursively.
	 * <p>
	 * Applies the defined background color to the control and all its
	 * descendants.
	 * 
	 * @param control
	 *            control to apply the color
	 * @param color
	 *            background color to apply
	 */
	public static void recursiveSetBackgroundColor(Control control, Color color) {
		control.setBackground(color);
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (int i = 0; i < children.length; i++) {
				recursiveSetBackgroundColor(children[i], color);
			}
		}
	}

	/**
	 * Sets the shell's Icon(s) to the default Azureus icon.
	 * <p>
	 * OSX doesn't require an icon, so they are skipped
	 * 
	 * @param shell
	 *            shell
	 */
	public static void setShellIcon(Shell shell) {
		// final String[] sImageNames = { "azureus", "azureus32", "azureus64",
		// "azureus128" };
		// //if (Timing.isOSX)
		// // return;
		// try {
		// ArrayList list = new ArrayList();
		// Image[] images = new Image[] { ImageRepository.getImage("azureus"),
		// ImageRepository.getImage("azureus32"),
		// ImageRepository.getImage("azureus64"),
		// ImageRepository.getImage("azureus128") };
		// for (int i = 0; i < images.length; i++) {
		// Image image = ImageRepository.getImage(sImageNames[i]);
		// if (image != null)
		// list.add(image);
		// }
		// if (list.size() == 0)
		// return;
		// shell.setImages((Image[]) list.toArray(new Image[0]));
		// }
		// catch (NoSuchMethodError e) { /
		// // SWT < 3.
		// Image image =
		// ImageRepository.getImage(sImageNames[0]);
		// if (image != null) shell.setImage(image); } }
		// }
	}

	/**
	 * Creates a new SWT color from a given device.
	 * 
	 * @param device
	 *            the device on which to allocate the color
	 * @param rgb
	 *            the RGB values of the desired color
	 * @return color
	 * @see org.eclipse.swt.graphics.Color
	 */
	public static Color swtColor(final org.eclipse.swt.graphics.Device device,
			final RGB rgb) {
		return new Color(device, rgb);
	}

	/**
	 * Returns the maximum dimensions for a set of string using font of a
	 * control.
	 * 
	 * @param control
	 *            the control where the strings will be drawn
	 * @param strings
	 *            the list of strings to be analyzed
	 * @return a {@link Point}, where the x contains the maximum extent; and y
	 *         contains the height.
	 */
	public static Point getMaxStringDimensions(final Control control,
			final String[] strings) {
		final GC gc = new GC(control);
		gc.setFont(control.getFont());
		final FontMetrics fm = gc.getFontMetrics();

		int maxSoFar = 0;
		for (int i = 0; i < strings.length; i++) {
			final int extent = gc.textExtent(strings[i]
					+ fm.getAverageCharWidth()).x;
			if (extent > maxSoFar) {
				maxSoFar = extent;
			}
		}
		final int heightHint = fm.getHeight();
		gc.dispose();
		return new Point(maxSoFar, heightHint);
	}
}
