package SimpleOSGiRepository.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * The Class ManifestLoader used to load the manifest.mf.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class ManifestLoader {

	/**
	 * Gets the imported packages.
	 * 
	 * @return the imported packages
	 * @throws MojoExecutionException
	 */
	public static String[] getImportedPackages() throws MojoExecutionException {
		return parseField("Import-Package");
	}

	/**
	 * Gets the manifest.
	 * 
	 * @return the manifest
	 * @throws MojoExecutionException
	 */
	private static String getManifest() throws MojoExecutionException {
		final String manifestPath = "." + File.separator + "META-INF"
				+ File.separator + "MANIFEST.MF";
		File manifestFile = new File(manifestPath);
		if (!manifestFile.exists())
			throw new MojoExecutionException(
					"OSGi Manifest file could not be found!");
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					new FileInputStream(manifestFile)));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = bf.readLine()) != null) {
				out.append(line + "\n");
			}
			bf.close();
			return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(
					"Error reading manifest.mf! Not enough permissions? See server logs for more info.");
		}
	}

	/**
	 * Parses a given field from the jar's manifest.mf file.
	 * 
	 * @param fieldName
	 *            the desired manifest field name.
	 * @return an array with the entries in that field.
	 * @throws MojoExecutionException
	 */
	private static String[] parseField(String fieldName)
			throws MojoExecutionException {
		String manifest = getManifest();
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
		for (int i = 0; i < tmp2.length && tmp2[i].charAt(0) == ' '; i++) {
			sb.append(tmp2[i]);
		}

		/*
		 * Take out extras spaces that may exist.
		 */
		String entireFieldValue = sb.toString().replaceAll(" ", "");

		/*
		 * Separate each entry and take out the extra spaces.
		 */
		List<String> entries = new LinkedList<String>();
		String packageName = "";
		boolean insideQuote = false;
		for (int i = 0; i < entireFieldValue.length(); i++) {
			char c = entireFieldValue.charAt(i);
			if (c == '"') {
				/*
				 * Parameter detected.
				 */
				insideQuote = !insideQuote;
				packageName += c;
			} else if (c == ',' && !insideQuote) {
				/*
				 * New package detected.
				 */
				entries.add(new String(packageName));
				packageName = "";
			} else {
				packageName += c;
			}
		}
		/*
		 * Add last accumulated entry.
		 */
		if (packageName != "")
			entries.add(new String(packageName));
		if (entries.size() == 0)
			return null;
		else
			return entries.toArray(new String[entries.size()]);
	}

}
