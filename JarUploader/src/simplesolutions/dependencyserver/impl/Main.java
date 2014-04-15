package simplesolutions.dependencyserver.impl;

import java.io.File;

/**
 * The Class Main.
 */
public final class Main {

	/** The Constant JARS_PATH. */
	private static final String JARS_PATH = "jars";

	/** The jar registry. */
	private static BundleDatabase jarRegistry;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		jarRegistry = new BundleDatabase();
		jarRegistry.start();
		File file = new File(".");
		System.out.println("HINT: Put your bundles in "
				+ file.getAbsolutePath() + " inside the " + JARS_PATH
				+ File.separator + " folder.");
		new HttpServer(80).start();
	}

	/**
	 * Gets the jar registry.
	 * 
	 * @return the jar registry
	 */
	public static BundleDatabase getJarRegistry() {
		return jarRegistry;
	}

	public static String getJarsFolderName() {
		return JARS_PATH;
	}

}
