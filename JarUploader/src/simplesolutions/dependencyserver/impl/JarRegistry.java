package simplesolutions.dependencyserver.impl;

import java.io.File;
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
 * Looks into the jars folder waiting for new jars to appear, get modified or
 * removed, in order to register them in the repository's database.
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
		/*
		 * File deleted?
		 */
		File affectedFile = new File(affectedFileName);
		if (!affectedFile.exists()) {
			XMLDataBase.removeObject(registry.remove(affectedFileName));
			return;
		}
		/*
		 * Update its manifest data.
		 */
		if (JarLoader.isValidJarFileWithManifest(affectedFileName)) {
			String[] importedPackages = JarLoader
					.getImportedPackages(affectedFileName);
			String[] exportedPackages = JarLoader
					.getExportedPackages(affectedFileName);
				XMLDataBase.removeObject(o);
			registry.put(affectedFileName, new JarFile(affectedFileName,
					importedPackages, exportedPackages));
		} else {
			registry.remove(affectedFileName);
		}
		updateContentsXML();
	}

	/**
	 * Update contents.xml.
	 */
	private void updateContentsXML() {
		XMLDataBase.save();
	}
}
