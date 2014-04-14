package lumina.ui.views.blueprint.actions;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A combo that presents a zoom-level.
 */
public class ZoomLevelCombo implements SelectionListener {

	private static final int COMBO_DEFAULT_HEIGHT = 20;

	private static final int COMBO_DEFAULT_WIDTH = 200;

	/**
	 * The percent sign.
	 */
	private static final String PERCENT = "%";

	/**
	 * A zoomable object.
	 * 
	 * @author Paulo Carreira
	 */
	public interface Zoomable {
		/**
		 * @return the current zoom level
		 */
		double getZoomLevel();

		/**
		 * Set the zoom level.
		 * 
		 * @param level
		 *            the zoom level. Must be > 0.
		 */
		void setZoomLevel(final double level);
	}

	/**
	 * The composite where the combo box is to be installed.
	 */
	private final Composite top;

	/**
	 * The combo widget: A {@link CCombo} is a bit smaller and fits nicely on a
	 * tool bar.
	 */
	private final CCombo zoomLevelCombo;

	/**
	 * The client object that this combo should contact when the user enters a
	 * new zoom level.
	 */
	private Zoomable zoomableClient;

	/**
	 * The number format used to represent the zoom percent.
	 */
	private NumberFormat percentFmt;

	public ZoomLevelCombo(final Composite panel, final int style) {
		top = panel;

		zoomLevelCombo = new CCombo(top, style | SWT.DROP_DOWN);
		zoomLevelCombo.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		zoomLevelCombo.setSize(COMBO_DEFAULT_WIDTH, COMBO_DEFAULT_HEIGHT);
		zoomLevelCombo.addSelectionListener(this);

		zoomLevelCombo.add("1200%");
		zoomLevelCombo.add("1000%");
		zoomLevelCombo.add("800%");
		zoomLevelCombo.add("600%");
		zoomLevelCombo.add("500%");
		zoomLevelCombo.add("400%");
		zoomLevelCombo.add("300%");
		zoomLevelCombo.add("200%");
		zoomLevelCombo.add("100%");
		zoomLevelCombo.add("50%");
		zoomLevelCombo.add("25%");

		final int defaultOption = zoomLevelCombo.indexOf("100%");

		// Select the 100% option
		zoomLevelCombo.select(defaultOption);

		percentFmt = NumberFormat.getPercentInstance();
		percentFmt.setGroupingUsed(false);
	}

	/**
	 * Sets the current zoom.
	 * 
	 * @param userInput
	 *            the zoom combo text
	 */
	private void setZoom(final String userInput) {

		try {
			final String text;
			if (userInput.endsWith(PERCENT)) {
				text = userInput;
			} else {
				text = userInput + PERCENT;
			}

			final Number level = percentFmt.parse(text);

			zoomLevelCombo.setEnabled(false);

			setZoom(level.doubleValue());

			if (zoomableClient != null) {
				zoomableClient.setZoomLevel(level.doubleValue());
			}
		} catch (java.text.ParseException e) {
			// ignore: we just do not set the value if it is malformed
		} finally {
			zoomLevelCombo.setEnabled(true);
		}
	}

	public final void setEnabled(final boolean enabled) {
		zoomLevelCombo.setEnabled(enabled);
	}

	public final void setZoom(final double scale) {
		final String levelPercent = percentFmt.format(scale);
		zoomLevelCombo.setText(levelPercent);
	}

	public final void setZoomClient(Zoomable client) {
		zoomableClient = client;
	}

	@Override
	public final void widgetDefaultSelected(SelectionEvent e) {
		setZoom(zoomLevelCombo.getText());
	}

	@Override
	public final void widgetSelected(SelectionEvent e) {
		setZoom(zoomLevelCombo.getText());
	}
}
