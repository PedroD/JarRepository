package simplesolutions.dependencyserver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The Class TestUtils.
 */
public final class TestUtils {

	/**
	 * Parses the package version.
	 * 
	 * @param packageNameEntry
	 *            the package name entry.
	 * @return the string with the version as it is in the manifest, or "0.0.0"
	 *         if no version is declared.
	 */
	public static String getPackageVersion(String packageNameEntry) {
		return new JarBundleFile.PackageVersion(packageNameEntry).toString();
	}

	/**
	 * Creates a folder.
	 * 
	 * @param folderName
	 *            the folder name
	 * @return true, if successful
	 */
	private static boolean createFolder(String folderName) {
		final Path repositoryDirectory = Paths.get(folderName);
		if (!repositoryDirectory.toFile().exists()) {
			repositoryDirectory.toFile().mkdir();
		}
		return repositoryDirectory.toFile().exists();
	}

	/**
	 * Creates a bundle in a temporary folder with a given manifest.mf file.
	 * 
	 * @param fileName
	 *            the bundle file name (ex. myBundle.jar)
	 * @param manifestFileContent
	 *            the manifest file content
	 */
	public static File createTemporaryBundle(String fileName,
			String manifestFileContent) {
		try {
			/*
			 * Create a temporary folder.
			 */
			final String tempFolder = "test_temp";
			if (!createFolder(tempFolder))
				return null;

			/*
			 * The file streams.
			 */
			String jarFilePath = tempFolder + File.separator + fileName;
			FileInputStream manifestFileInputStream;
			FileOutputStream manifestFileOutputStream;
			ZipOutputStream jarFileOutputStream = new ZipOutputStream(
					new FileOutputStream(jarFilePath));

			/*
			 * Create the temporary manifest file.
			 */
			File tmpFile = new File(tempFolder + File.separator + "MANIFEST.MF");
			tmpFile.createNewFile();

			/*
			 * Write the manifest content.
			 */
			manifestFileOutputStream = new FileOutputStream(tmpFile);
			manifestFileOutputStream.write(manifestFileContent.getBytes());
			manifestFileOutputStream.close();

			/*
			 * Create the manifest file inside the zip file.
			 */
			jarFileOutputStream.putNextEntry(new ZipEntry("META-INF"
					+ File.separator + "MANIFEST.MF"));

			/*
			 * Put the content inside the manifest file.
			 */
			byte[] b = new byte[1024];
			int count;
			manifestFileInputStream = new FileInputStream(tmpFile);
			while ((count = manifestFileInputStream.read(b)) > 0) {
				System.out.println();
				jarFileOutputStream.write(b, 0, count);
			}
			jarFileOutputStream.close();
			manifestFileInputStream.close();

			/*
			 * Clear the temporary manifest file.
			 */
			new File(tempFolder + File.separator + "MANIFEST.MF").delete();

			return new File(jarFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Delete temporary folder.
	 */
	public static void deleteTemporaryFolder() {
		deleteFolder(new File("test_temp"));
	}

	/**
	 * Delete a folder.
	 * 
	 * @param folder
	 *            the folder
	 */
	private static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	/**
	 * Copies a given file to the repository jars folder.
	 * 
	 * @param jarFile
	 *            the jar file to copy.
	 * @return
	 */
	public static File moveFileToJarsFolder(File jarFile) {
		try {
			File destinationFile = new File(Main.getJarsFolderName()
					+ File.separator + jarFile.getName());
			Files.move(jarFile.toPath(), destinationFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			return destinationFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The main method (just for testing).
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {

		File f = createTemporaryBundle("lol.jar", "fdfdsfsdfsd");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		moveFileToJarsFolder(f);
	}
}
