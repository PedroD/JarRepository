package lumina.kernel.util.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * XML related utilities.
 */
public final class XML {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private XML() {
	}

	/**
	 * Creates a new DOM document.
	 * 
	 * @return New document.
	 */
	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException ex) {
			// should never happen
			return null;
		}
	}

	/**
	 * Loads a XML document from a stream and returns the corresponding DOM
	 * Document.
	 * 
	 * @param stream
	 *            Input stream with XML
	 * @return Parsed document
	 * @throws Exception
	 *             if the loading or parsing fails
	 */
	public static Document loadDocument(InputStream stream) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(stream);
	}

	/**
	 * Saves a DOM document to a stream, with UTF8 encoding.
	 * 
	 * @param stream
	 *            Output stream that will receive the document
	 * @param doc
	 *            Document to save
	 * @throws Exception
	 *             If a serializer cannot be obtained, or if writing fails.
	 */
	public static void saveDocument(OutputStream stream, Document doc)
			throws Exception {
		DOMImplementationRegistry registry = DOMImplementationRegistry
				.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("LS");
		if (impl == null)
			throw new RuntimeException(
					"DOM implementation does not support Load/Save feature");

		LSOutput output = impl.createLSOutput();
		output.setByteStream(stream);
		output.setEncoding("UTF-8");

		LSSerializer serializer = impl.createLSSerializer();
		// documentation says this should work, but it doesn't:
		// serializer.getDomConfig().setParameter("format-pretty-print",
		// "true");

		serializer.write(doc, output);
	}

	/**
	 * Saves a DOM document to a string.
	 * 
	 * @param doc
	 *            Document to save
	 * @return String with XML
	 * @throws Exception
	 *             If a serializer cannot be obtained.
	 */
	public static String saveDocumentToString(Document doc) throws Exception {
		DOMImplementationRegistry registry = DOMImplementationRegistry
				.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("LS");
		if (impl == null)
			throw new RuntimeException(
					"DOM implementation does not support Load/Save feature");

		LSSerializer serializer = impl.createLSSerializer();
		return serializer.writeToString(doc);
	}

	/**
	 * Gets a list of child elements by tag name.
	 * 
	 * @param parent
	 *            Parent element
	 * @param childName
	 *            Child tag name
	 * @return ArrayList of child Elements
	 */
	public static ArrayList<Element> getChildElements(Element parent,
			String childName) {
		ArrayList<Element> result = new ArrayList<Element>();

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE
					&& n.getNodeName().equals(childName))
				result.add((Element) n);
		}

		return result;
	}

	/**
	 * Gets a child element by tag name. If there is more than one child with
	 * the given name, the first one is returned.
	 * 
	 * @param parent
	 *            Parent element
	 * @param childName
	 *            Child tag name
	 * @return Child Element, or null if not found
	 */
	public static Element getChildElement(Element parent, String childName) {
		// this is inefficient, but the crappy DOM API doesn't leave any better
		// alternatives
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE
					&& n.getNodeName().equals(childName))
				return (Element) n;
		}
		return null;
	}

	/**
	 * Gets the text content of a child element by tag name. If there is more
	 * than one child with the given name, the text of the first one is
	 * returned.
	 * 
	 * @param parent
	 *            Parent element
	 * @param childName
	 *            Child tag name
	 * @return Text of the child element, or null if not found
	 */
	public static String getChildElementText(Element parent, String childName) {
		Element child = getChildElement(parent, childName);
		if (child != null)
			return child.getTextContent();
		else
			return null;
	}

	/**
	 * Creates an element with text.
	 * 
	 * @param doc
	 *            DOM Document
	 * @param elemName
	 *            Element name
	 * @param elemText
	 *            If null no text node is created, else the result of
	 *            elemText.toString() is used as the text node.
	 * @return New DOM Element
	 */
	public static Element createElementWithText(Document doc, String elemName,
			Object elemText) {
		Element elem = doc.createElement(elemName);
		if (elemText != null)
			elem.appendChild(doc.createTextNode(elemText.toString()));
		return elem;
	}

}
