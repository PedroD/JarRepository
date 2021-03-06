package simplesolutions.dependencyserver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

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
	public static final String FILE_NAME = "contents.xml";

	/** The file in. */
	private static FileInputStream fileIn = null;

	/** The file out. */
	private static FileOutputStream fileOut = null;

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
	 * @param registry
	 * 
	 * @return true, if successful
	 */
	public synchronized static boolean save(Map<String, JarBundleFile> registry) {
		try {
			if (!openFile())
				return false;
			fileOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\t<bundles>\r\n"
					.getBytes());
			for (Map.Entry<String, JarBundleFile> o : registry.entrySet())
				fileOut.write(("\t" + o.getValue().toXML() + "\r\n").getBytes());
			fileOut.write("\t</bundles>\r\n".getBytes());
			fileOut.flush();
			if (!closeFile())
				return false;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
