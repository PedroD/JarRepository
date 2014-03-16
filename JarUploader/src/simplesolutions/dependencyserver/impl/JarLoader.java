package simplesolutions.dependencyserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Class JarLoader used to load jars and parse their manifest.mf.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class JarLoader {

	/**
	 * Gets the exported packages.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the exported packages
	 */
	public static String[] getExportedPackages(String fileName) {
		return parseField(fileName, "Export-Package");
	}

	/**
	 * Gets the imported packages.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the imported packages
	 */
	public static String[] getImportedPackages(String fileName) {
		return parseField(fileName, "Import-Package");
	}

	/**
	 * Gets the manifest.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the manifest
	 */
	private static String getManifest(String fileName) {
		BufferedReader bf = loadJarFile(fileName);
		try {
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = bf.readLine()) != null) {
				out.append(line + "\n");
			}
			bf.close();
			return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load jar file.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the buffered reader
	 */
	@SuppressWarnings("resource")
	private static BufferedReader loadJarFile(String fileName) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(fileName);
			for (Enumeration<? extends ZipEntry> e = zip.entries(); e
					.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if (entry.toString().equals("META-INF/MANIFEST.MF")) {
					return new BufferedReader(new InputStreamReader(
							zip.getInputStream(entry)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isValidJarFileWithManifest(String fileName) {
		return loadJarFile(fileName) != null;
	}

	/**
	 * Parses a given field from the jar's manifest.mf file.
	 * 
	 * @param fileName
	 *            the jar file path.
	 * @param fieldName
	 *            the desired manifest field name.
	 * @return an array with the entries in that field.
	 */
	private static String[] parseField(String fileName, String fieldName) {
		String manifest = getManifest(fileName);
		if (manifest == null)
			return null;

		/*
		 * Divide the file in two parts, the first one is trash the second one
		 * contains the desired entries and some trash appended.
		 */
		String[] tmp1 = manifest.split(fieldName + ":");
		if (tmp1.length != 2)
			return null; // No entries for that field.
		String[] tmp2 = tmp1[1].split("\n");

		/*
		 * Seek for the end of current manifest parameter i.e. the first line
		 * not beginning by a space character. (We assume the line where
		 * fieldName is a valid line containing at least one entry). The first
		 * line not beginning with a space character and the followers are
		 * trash.
		 */
		StringBuilder sb = new StringBuilder();
		for (int i = 0; tmp2[i].charAt(0) == ' ' && i < tmp2.length; i++) {
			sb.append(tmp2[i]);
		}

		/*
		 * Separate each entry and take out the extra spaces.
		 */
		String[] entries = sb.toString().split(",");
		for (int i = 0; i < entries.length; i++) {
			entries[i] = entries[i].replaceAll(" ", ""); // Take spaces out
		}
		return entries;
	}

}
