package lumina.kernel.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface is implemented by classes that have the ability to save and
 * restore their state to XML.
 */
public interface XMLSerializable {

	/**
	 * Saves the object to XML.
	 * <p>
	 * This method is expected to return a DOM subtree (rooted in the returned
	 * Element) representing the object's state. The returned Element can later
	 * be passed to <code>restoreFromXML()</code> to recreate the object's
	 * state.
	 * 
	 * @param doc
	 *            Document (used to create DOM nodes)
	 * @return DOM Element containing the object state
	 */
	Element saveToXML(Document doc);

	/**
	 * Restores an object from XML.
	 * <p>
	 * The Element passed to this method is what was returned by
	 * <code>saveToXML()</code>
	 * 
	 * @param elem
	 *            DOM Element containing the object state
	 */
	void restoreFromXML(Element elem);

}
