package simplesolutions.dependencyserver.impl;

import java.io.File;

public final class Main {

	public static void main(String[] args) {
		new JarRegistry().start();
		File file = new File(".");
		System.out.println("HINT: Put your bundles in " + file.getAbsolutePath()
				+ " inside the jars/ folder.");
	}

}
