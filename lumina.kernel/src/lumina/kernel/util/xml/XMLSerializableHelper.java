package lumina.kernel.util.xml;

import org.w3c.dom.Element;

/**
 * This class contains methods to serialize/deserialize existing classes that
 * cannot be made to implement XMLSerializable.
 */
public final class XMLSerializableHelper {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private XMLSerializableHelper() {
	}

	/**
	 * Saves a RGB color to a XML element.
	 * 
	 * @param parentElem
	 *            the parent XML elem
	 * @param rgb
	 *            the rgb strucuture
	 */
	public static void saveRGB(Element parentElem,
			org.eclipse.swt.graphics.RGB rgb) {
		parentElem.setTextContent(rgb.red + "," + rgb.green + "," + rgb.blue);
	}

	/**
	 * Restores a RGB color from an XML element.
	 * 
	 * @param parentElem
	 *            the parent XML element
	 * @return the {@link org.eclipse.swt.graphics.RGB} or <code>null</code> if
	 *         the color could not be parsed.
	 */
	public static org.eclipse.swt.graphics.RGB restoreRGB(Element parentElem) {
		String[] components = parentElem.getTextContent().split(",");
		if (components.length != 1 + 1 + 1)
			return null;

		try {
			int r = Integer.parseInt(components[0]);
			int g = Integer.parseInt(components[1]);
			int b = Integer.parseInt(components[2]);

			return new org.eclipse.swt.graphics.RGB(r, g, b);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

}
