package simplesolutions.dependencyserver.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class JarRegistry.
 * <p>
 * Looks into the repository directory waiting for new jars to appear,
 * get modified or removed, in order to register them in the repository's
 * database.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class JarRegistry extends Thread {

	/** The registry. */
	private final Map<String, JarFile> registry;

	/** The repository directory. */
	private static final Path repositoryDirectory = Paths.get("jars");

	/** The watcher service. */
	private WatchService watcherService;

	/** The watch key. */
	private WatchKey watchKey;

	/**
	 * Instantiates a new jar registry.
	 */
	public JarRegistry() {
		registry = new HashMap<String, JarFile>();
		try {
			if (!repositoryDirectory.toFile().exists())
				repositoryDirectory.toFile().mkdir();
			watcherService = FileSystems.getDefault().newWatchService();
			watchKey = repositoryDirectory.register(watcherService,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		/*
		 * Watch for affected files in the repository folder.
		 */
		while (true) {
			try {
				watchKey = watcherService.take();
				for (WatchEvent<?> event : watchKey.pollEvents()) {
					WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
					String affectedFile = repositoryDirectory.resolve(
							watchEvent.context()).toString();
					updateRegistry(affectedFile);
					watchKey.reset();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates jar registry.
	 * 
	 * @param kind
	 *            the kind of event occurred.
	 * @param affectedFile
	 *            the affected file.
	 */
	private void updateRegistry(String affectedFileName) {
		System.err.println("File modified: " + affectedFileName);
		File affectedFile = new File(affectedFileName);
		/*
		 * File deleted? Remove it from our registry.
		 */
		if (!affectedFile.exists()) {
			System.err.println("File was deleted.");
			registry.remove(affectedFileName);
			updateContentsOfXML();
			return;
		}
		/*
		 * Is the file still being modified/locked?
		 */
		while (!affectedFile.canRead()) {
			try {
				System.err.println("File locked, will try again in 5 sec...");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
		}
		/*
		 * Valid jar file?
		 */
		if (!isZipFile(affectedFile)) {
			System.err.println("Not a valid JAR file.");
			return;
		}
		/*
		 * File added or modified? Update its manifest data.
		 */
		if (JarLoader.isValidJarFileWithManifest(affectedFileName)) {
			String[] importedPackages = JarLoader
					.getImportedPackages(affectedFileName);
			String[] exportedPackages = JarLoader
					.getExportedPackages(affectedFileName);
			registry.put(affectedFileName, new JarFile(affectedFileName,
					importedPackages, exportedPackages));
			System.err.println("Bundle added with success!");
		} else {
			registry.remove(affectedFileName);
			System.err.println("Not a valid bundle file!");
		}
		updateContentsOfXML();
	}

	/**
	 * Determine whether a file is a ZIP File.
	 * 
	 * @param file
	 *            the file
	 * @return true, if is zip file
	 */
	public boolean isZipFile(File file) {
		if (file.isDirectory()) {
			return false;
		}
		if (file.length() < 4) {
			return false;
		}
		DataInputStream in;
		try {
			in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			int test = in.readInt();
			in.close();
			return test == 0x504b0304;
		} catch (FileNotFoundException e) {
			// Nothing to do here.
		} catch (IOException e) {
			// Nothing to do here.
		}
		return false;
	}

	/**
	 * Update contents.xml.
	 */
	private void updateContentsOfXML() {
		XMLDataBase.save(registry);
	}
}
