package lumina.ui.views.control.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import lumina.ui.swt.ApplicationImageCache;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A widget for displaying fade time on the device UI.
 */
public class FadeTimerLabel extends CLabel {

	/**
	 * Icon that suggests the fading.
	 */
	private static final String FADING_IMAGE = "/icons/actions/fading.png"; //$NON-NLS-1$

	/**
	 * Tooltip for the remaining fade time.
	 */
	private static final String FADING_TOOLTIP = Messages
			.getString("FadeTimer.remainingTime"); //$NON-NLS-1$ //$NON-NLS-1$

	/**
	 * The time separator.
	 */
	private static final String TIME_SEPARATOR = ":";

	/**
	 * Unknown string for minutes and seconds.
	 */
	private static final String UNKNOWN_MINUTE_SECS = "--" + TIME_SEPARATOR
			+ "--";

	/**
	 * Unknown string for hours, minutes and seconds.
	 */
	private static final String UNKNOWN_HOUR_MINUTE_SECS = "--"
			+ TIME_SEPARATOR + "--" + TIME_SEPARATOR + "--";

	/**
	 * The time formatter.
	 */
	private final SimpleDateFormat timeFormatter;

	/**
	 * Tracks whether hours should be displayed too.
	 */
	private final boolean displayHours;

	/**
	 * Creates a timer that displays minutes and seconds.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the SWT style
	 */
	public FadeTimerLabel(Composite parent, int style) {
		this(parent, style, false);
	}

	/**
	 * Creates a timer that displays minutes and seconds and optionally hours.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the SWT style
	 * @param hours
	 *            whether hours should be displayed
	 */
	public FadeTimerLabel(Composite parent, int style, final boolean hours) {
		super(parent, style);
		displayHours = hours;

		final Image fadingImage = ApplicationImageCache.getInstance().getImage(
				FADING_IMAGE);
		setImage(fadingImage);

		setToolTipText(FADING_TOOLTIP);

		if (hours) {
			timeFormatter = new SimpleDateFormat("HH:mm:ss");
		} else {
			timeFormatter = new SimpleDateFormat("mm:ss");
		}
	}

	/**
	 * Sets the timer to be displayed.
	 * 
	 * @param millis
	 *            the time duration to be displayed, if smaller than zero
	 *            displays the timer unknown string
	 */
	public void setTime(final long millis) {
		if (millis < 0) {
			if (displayHours) {
				setText(UNKNOWN_MINUTE_SECS);
			} else {
				setText(UNKNOWN_HOUR_MINUTE_SECS);
			}
		} else {
			setText(timeFormatter.format(new Date(millis)));
		}
	}
}
