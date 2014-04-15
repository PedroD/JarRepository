package SimpleOSGiRepository.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Loads and parses .classfiles.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class ClassfileLoader {

	/*
	 * Reads the classpath file entries of this project's .classpath file. This
	 * includes the output entry. As a side effect, unknown elements are stored
	 * in the given map (if not null) Throws exceptions if the file cannot be
	 * accessed or is malformed.
	 */
	/**
	 * Gets the raw classpath.
	 * 
	 * @return the raw classpath
	 */
	public static IClasspathEntry[] getRawClasspath() {
		try {
			File rscFile = new File(".classpath");
			Byte[] bytes;
			if (rscFile.exists()) {
				// Get byte array from classpath file
				bytes = readFileToArray(rscFile);
			} else {
				throw new MojoExecutionException(
						"This project contains no .classpath file! Is this an Eclipse project?");
			}
			/*
			 * Checks if it is encoded correctly.
			 */
			if (hasUTF8BOM(bytes)) { // see
										// https://bugs.eclipse.org/bugs/show_bug.cgi?id=240034
				int length = bytes.length
						- IContentDescription.BOM_UTF_8.length;
				System.arraycopy(bytes, IContentDescription.BOM_UTF_8.length,
						bytes = new Byte[length], 0, length);
			}
			String xmlClasspath;
			try {
				xmlClasspath = new String(
						convertByteArrayObjectToPrimitive(bytes),
						org.eclipse.jdt.internal.compiler.util.Util.UTF_8); // .classpath
																			// always
																			// encoded
																			// with
																			// UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .classpath with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				xmlClasspath = new String(
						convertByteArrayObjectToPrimitive(bytes));
			}
			return decodeClasspath(xmlClasspath, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Read file to array.
	 * 
	 * @param file
	 *            the file
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static Byte[] readFileToArray(File file) throws IOException {
		/*
		 * We could use a linked list here, but array lists are amortized, so we
		 * rarely get insertions with O(n) time.
		 */
		List<Byte> bytesList = new ArrayList<Byte>();
		FileInputStream in = new FileInputStream(file);
		byte b;
		while ((b = (byte) in.read()) != -1)
			bytesList.add(b);
		in.close();
		return bytesList.toArray(new Byte[bytesList.size()]);
	}

	/**
	 * Adds a new classpath entry to this project's .classpath file.
	 * 
	 * @param bundlePath
	 *            the bundle path
	 * @throws MojoExecutionException
	 *             the mojo execution exception
	 */
	private static void addClasspathEntry(String bundlePath)
			throws MojoExecutionException {
		try {
			IClasspathEntry[] rawClasspath = getRawClasspath();
			List<IClasspathEntry> list = new LinkedList<IClasspathEntry>(
					java.util.Arrays.asList(rawClasspath));
			boolean isAlreadyAdded = false;
			for (IClasspathEntry cpe : rawClasspath) {
				isAlreadyAdded = cpe.getPath().toOSString().equals(bundlePath);
				if (isAlreadyAdded)
					break;
			}
			if (!isAlreadyAdded) {
				IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(
						bundlePath), null, null);
				list.add(jarEntry);
			}
			IClasspathEntry[] newClasspath = (IClasspathEntry[]) list
					.toArray(new IClasspathEntry[0]);
			javaProject.setRawClasspath(newClasspath, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if a byte array uses charset utf8 bom.
	 * 
	 * @param bytes
	 *            the byte array to check.
	 * @return true, if successful
	 */
	private static boolean hasUTF8BOM(Byte[] bytes) {
		if (bytes.length > IContentDescription.BOM_UTF_8.length) {
			for (int i = 0, length = IContentDescription.BOM_UTF_8.length; i < length; i++) {
				if (IContentDescription.BOM_UTF_8[i] != bytes[i])
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Converts a Byte array object to a primitive <b>byte</b> array.
	 * 
	 * @param oldArray
	 *            the Byte object array.
	 * @return the byte[]
	 */
	private static byte[] convertByteArrayObjectToPrimitive(Byte[] oldArray) {
		byte[] newArray = new byte[oldArray.length];
		for (int i = 0; i < oldArray.length; i++)
			newArray[i] = oldArray[i];
		return newArray;
	}

	/**
	 * Reads and decode an XML classpath string
	 * 
	 * @param xmlClasspath
	 * @param unknownElements
	 * @return
	 */
	public IClasspathEntry[] decodeClasspath(String xmlClasspath) {
		ArrayList paths = new ArrayList();
		IClasspathEntry defaultOutput = null;
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader))
					.getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Messages.file_badFormat);
		} catch (ParserConfigurationException e) {
			throw new IOException(Messages.file_badFormat);
		} finally {
			reader.close();
		}

		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
			throw new IOException(Messages.file_badFormat);
		}
		NodeList list = cpElement.getElementsByTagName("classpathentry"); //$NON-NLS-1$
		int length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				IClasspathEntry entry = ClasspathEntry.elementDecode(
						(Element) node, this, unknownElements);
				if (entry != null) {
					if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
						defaultOutput = entry; // separate output
					} else {
						paths.add(entry);
					}
				}
			}
		}
		// return a new empty classpath is it size is 0, to differenciate from
		// an INVALID_CLASSPATH
		int pathSize = paths.size();
		IClasspathEntry[] entries = new IClasspathEntry[pathSize
				+ (defaultOutput == null ? 0 : 1)];
		paths.toArray(entries);
		if (defaultOutput != null)
			entries[pathSize] = defaultOutput; // ensure output is last item
		return entries;
	}
}
