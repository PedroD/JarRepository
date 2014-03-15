package simplesolutions.dependencyserver.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XML DataBase used to store in a XML all the registered jars.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class XMLDataBase {

	/** The Constant FILE_NAME. */
	private static final String FILE_NAME = "contents.xml";

	/** The file handler. */
	private static File fileHandler = null;

	/** The Constant registry. */
	private static final List<XMLParseable> registry = new ArrayList<XMLParseable>();

	/**
	 * Open file.
	 * 
	 * @return true, if successful
	 */
	private static boolean openFile() {
		try {
			File fileHandler = new File(FILE_NAME);
			if (!fileHandler.exists()) {
				fileHandler.createNewFile();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			fileHandler = null;
			return false;
		}
	}

	/**
	 * Adds the object.
	 * 
	 * @param o
	 *            the XML Parseable object
	 */
	public void addObject(XMLParseable o) {
		registry.add(o);
	}

	/**
	 * Removes the object.
	 * 
	 * @param o
	 *            the XML Parseable object
	 */
	public void removeObject(XMLParseable o) {
		registry.remove(o);
	}

	/**
	 * The Interface XMLParseable.
	 */
	public interface XMLParseable {

		/**
		 * To xml.
		 * 
		 * @return a XML representation of this object.
		 */
		String toXML();
	}

}
