package lumina.kernel.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Methods to format and parse units.
 */
public final class PowerUnit {

	private static final double THOUSAND_WATTS = 1000.0;

	private static final int HUNDRED_WATTS = 100;

	/**
	 * Prevent instantiation.
	 */
	private PowerUnit() {
	}

	private static final String WATT_UNIT = "W";

	private static final String KILOWATT_UNIT = "kW";

	private static final NumberFormat WATTS_FORMATER = new DecimalFormat(
			"######" + WATT_UNIT);

	private static final NumberFormat KW_FORMATTER = new DecimalFormat(
			"######.#" + KILOWATT_UNIT);

	/**
	 * Parses a power value. Understands the "W" and "kW" units. If no unit is
	 * present assumes Watts.
	 * 
	 * @param value
	 *            String to parse
	 * @return value in Watts
	 * @throws java.text.ParseException
	 *             if the power cannot be correctly parsed according to the
	 *             format
	 */
	public static synchronized double parsePower(final String value)
			throws java.text.ParseException {
		final String strValue = value.toUpperCase();

		if (strValue.endsWith(KILOWATT_UNIT.toUpperCase())) {
			// try kilowatts
			final String normalized = strValue.substring(0, strValue.length()
					- KILOWATT_UNIT.length())
					+ KILOWATT_UNIT;
			final Number n = KW_FORMATTER.parse(normalized);
			return n.doubleValue() * THOUSAND_WATTS;
		} else if (strValue.endsWith(WATT_UNIT.toUpperCase())) {
			// try watts
			final String normalized = strValue.substring(0, strValue.length()
					- WATT_UNIT.length())
					+ WATT_UNIT;
			final Number n = WATTS_FORMATER.parse(normalized);
			return n.doubleValue();
		} else {
			// assume watts by default
			final Number n = WATTS_FORMATER.parse(strValue + WATT_UNIT);
			return n.doubleValue();
		}
	}

	/**
	 * Formats a power value.
	 * 
	 * @param value
	 *            the power in Watts
	 * @param allowRounding
	 *            whether the formatted value can be rounded to kW
	 * @return the formatted value
	 */
	public static String formatPower(final double value,
			final boolean allowRounding) {
		if (allowRounding) {
			if (value >= HUNDRED_WATTS)
				return KW_FORMATTER.format(value / THOUSAND_WATTS);
			else
				return WATTS_FORMATER.format(value);
		} else {
			if (value >= THOUSAND_WATTS && value % THOUSAND_WATTS == 0)
				return KW_FORMATTER.format(value / THOUSAND_WATTS);
			else
				return WATTS_FORMATER.format(value);
		}
	}

}
