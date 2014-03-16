package simplesolutions.dependencyserver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XML DataBase used to store in a XML all the registered jars.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class XMLDataBase {

	/**
	 * The Interface XMLParseable.
	 */
	public interface XMLParseable {

		/**
		 * Read from xml.
		 * 
		 * @return true, if successful
		 */
		boolean fromXML();

		/**
		 * To xml.
		 * 
		 * @return a XML representation of this object.
		 */
		String toXML();
	}

	/** The Constant FILE_NAME. */
	private static final String FILE_NAME = "contents.xml";

	/** The file in. */
	private static FileInputStream fileIn = null;

	/** The file out. */
	private static FileOutputStream fileOut = null;

	/** The Constant registry. */
	private static final List<XMLParseable> registry = new ArrayList<XMLParseable>();

	/**
	 * Close file.
	 * 
	 * @return true, if successful
	 */
	private static boolean closeFile() {
		try {
			fileIn.close();
			fileOut.flush();
			fileOut.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

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
			fileIn = new FileInputStream(fileHandler);
			fileOut = new FileOutputStream(fileHandler);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Save data into XML file.
	 * 
	 * @return true, if successful
	 */
	public static boolean save() {
		try {
			if (!openFile())
				return false;
			for (XMLParseable o : registry)
				fileOut.write((o.toXML() + "\n").getBytes());
			if (!closeFile())
				return false;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Adds the object.
	 * 
	 * @param o
	 *            the XML Parseable object
	 */
	public static void addObject(XMLParseable o) {
		if (o != null)
			registry.add(o);
	}

	/**
	 * Removes the object.
	 * 
	 * @param o
	 *            the XML Parseable object
	 */
	public static void removeObject(XMLParseable o) {
		if (o != null)
			registry.remove(o);
	}

}
