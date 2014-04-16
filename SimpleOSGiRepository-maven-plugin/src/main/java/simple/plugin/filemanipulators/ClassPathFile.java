package simple.plugin.filemanipulators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import simple.plugin.utils.ErrorMessageFormatter;

// TODO: Auto-generated Javadoc
/**
 * Loads and parses .classfiles.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class ClassPathFile {

	/** The class path file. */
	private final File classPathFile;

	/**
	 * The class path lines
	 * <p>
	 * <b>Note:</b> Usually each line has only one entry, but not always. Since
	 * we don't care for this, because we're just going to append new lines at
	 * the end of the file and not edit existing entries, we'll treat each line
	 * as an entry.
	 */
	private final List<String> classPathEntries;

	/** Tells if the classpath file needs to be saved. */
	private boolean dirty;

	/**
	 * Instantiates a new class path file.
	 * 
	 * @throws MojoExecutionException
	 *             the mojo execution exception
	 */
	public ClassPathFile() throws MojoExecutionException {
		/*
		 * Get the classpath file.
		 */
		classPathFile = new File(".classpath");
		if (!classPathFile.exists())
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("There is no .classpath file in this project's root directory. Is this a valid Eclipse project?"));
		/*
		 * Try to read each line.
		 */
		try {
			classPathEntries = Files.readAllLines(classPathFile.toPath(),
					Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Error reading the .classpath file. Access denied?"));
		}
		/*
		 * Add an informative comment in the classpath file in the section we're
		 * going to edit.
		 */
		boolean tagFound = false;
		for (int i = 0; i < classPathEntries.size(); i++) {
			if (classPathEntries.get(i).contains("</classpath>")) {
				classPathEntries.set(i, "<!-- Simple OSGi Added Libraries -->");
				tagFound = true;
			}
		}
		/*
		 * Couldn't find the </classpath> tag?
		 */
		if (!tagFound)
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("The classpath file is corrupted!"));
	}

	/**
	 * Checks for the existence of a given entry (jar file name) in the
	 * classpath file as an included library (i.e. having kind="lib").
	 * 
	 * @param jarFileName
	 *            the jar file name (ex. cutejarfile.jar)
	 * @return true, if that entry exists
	 */
	public boolean hasLibraryEntry(String jarFileName) {
		for (String entry : classPathEntries)
			if (entry.contains(jarFileName) && entry.contains("kind=\"lib\""))
				return true;
		return false;
	}

	/**
	 * Adds a new jar library entry if it doesn't exist already in the classpath
	 * file.
	 * 
	 * @param path
	 *            the path
	 * @param jarFileName
	 *            the jar file name
	 * @throws MojoExecutionException
	 */
	public void addLibraryEntry(String path, String jarFileName)
			throws MojoExecutionException {
		/*
		 * Add the new entry.
		 */
		if (!hasLibraryEntry(jarFileName)) {
			classPathEntries.add("\t<classpathentry kind=\"lib\" path=\""
					+ path + jarFileName + "\"/>");
			dirty = true;
		}
	}

	/**
	 * Saves modifications to the classpath file on disk.
	 * 
	 * @throws MojoExecutionException
	 *             the mojo execution exception
	 */
	public void save() throws MojoExecutionException {
		if (!dirty)
			return;
		try {
			FileOutputStream out = new FileOutputStream(classPathFile, false);
			for (String entry : classPathEntries)
				out.write((entry + "\r\n").getBytes());
			/*
			 * Re-insert the closing tag that we took in the constructor.
			 */
			out.write("<!-- End of Simple OSGi Added Libraries -->\r\n"
					.getBytes());
			out.write("</classpath>\r\n".getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Error writting to the .classpath file. Access denied?"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Error writting to the .classpath file. Access denied?"));
		}
	}
}
