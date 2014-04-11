package simplesolutions.dependencyserver.impl;

import simplesolutions.dependencyserver.impl.JarBundleFile.PackageVersionRange;
import junit.framework.TestCase;

/**
 * The Class TestJarFile.
 */
public class TestJarFile extends TestCase {

	/**
	 * Test imports/exports.
	 */
	public void testImportsExports() {
		String[] importedPackages = new String[] { "java.io",
				"java.tools;version=\"1.3\"",
				"java.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"java.ui;foo=\"bar\";version=\"[1,1]\"",
				"java.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };

		String[] exportedPackages = new String[] { "test.abc",
				"test.tools;version=\"1.3\"",
				"test.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"test.ui;foo=\"bar\";version=\"[1,1]\"",
				"test.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };
		JarBundleFile testFile = new JarBundleFile("testfile.jar", importedPackages,
				exportedPackages);
		/*
		 * Test imported packages
		 */
		for (String p : importedPackages) {
			String packageName = p.split(";")[0];
			assertTrue(testFile.getImportedPackages().containsKey(packageName));
		}
		/*
		 * Test exported packages
		 */
		for (String p : exportedPackages) {
			String packageName = p.split(";")[0];
			assertTrue(testFile.getExportedPackages().containsKey(packageName));
		}
	}

	/**
	 * Parses the package version.
	 * 
	 * @param packageNameEntry
	 *            the package name entry.
	 * @return the string with the version as it is in the manifest, or "0.0.0"
	 *         if no version is declared.
	 */
	private String getPackageVersion(String packageNameEntry) {
		String[] tmp1 = packageNameEntry.split("version=\"");
		if (tmp1.length != 2)
			return "0.0.0"; // No version declared
		String[] version = tmp1[1].split("\""); // split over the last
												// quotation
												// mark.
		return version[0];
	}

	/**
	 * Test imports/exports versions.
	 */
	public void testImportsExportsVersions() {
		String[] importedPackages = new String[] { "java.io",
				"java.tools;version=\"1.3\"",
				"java.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"java.ui;foo=\"bar\";version=\"[1,1]\"",
				"java.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };

		String[] exportedPackages = new String[] { "test.abc",
				"test.tools;version=\"1.3\"",
				"test.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"test.ui;foo=\"bar\";version=\"[1,1]\"",
				"test.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };
		JarBundleFile testFile = new JarBundleFile("testfile.jar", importedPackages,
				exportedPackages);
		/*
		 * Test imported packages
		 */
		for (String p : importedPackages) {
			String version = getPackageVersion(p);
			String packageName = p.split(";")[0];
			assertEquals(version,
					testFile.getImportedPackages().get(packageName).toString());
		}
		/*
		 * Test exported packages
		 */
		for (String p : exportedPackages) {
			String version = getPackageVersion(p);
			String packageName = p.split(";")[0];
			assertEquals(version,
					testFile.getExportedPackages().get(packageName).toString());
		}
	}

	/**
	 * Test version compatibility.
	 */
	public void testVersionCompatibility() {
		PackageVersionRange versionRange;
		PackageVersionRange concreteVersion;
		/*
		 * Test two concrete versions.
		 */
		versionRange = new PackageVersionRange("goo.gle;version=\"1.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"1.5\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));
		/*
		 * Test one concrete version.
		 */
		versionRange = new PackageVersionRange("goo.gle;version=\"1.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"[1,2)\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));
		/*
		 * Test boundaries.
		 */
		versionRange = new PackageVersionRange("goo.gle;version=\"1.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"1.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"[1.5,2)\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"1\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"0.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"1.5\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"1.9\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"[1.5,2]\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"[2,2]\"");
		assertTrue(versionRange.isCompatible(concreteVersion));
		assertTrue(concreteVersion.isCompatible(versionRange));
		/*
		 * Test impossible comparisons.
		 */
		versionRange = new PackageVersionRange("goo.gle;version=\"[2,3]\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"[1.5,2]\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(1.5,2]\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));

		versionRange = new PackageVersionRange("goo.gle;version=\"2\"");
		concreteVersion = new PackageVersionRange("goo.gle;version=\"(2,2)\"");
		assertFalse(versionRange.isCompatible(concreteVersion));
		assertFalse(concreteVersion.isCompatible(versionRange));
	}

	public void testXMLOutput() {
		String[] importedPackages = new String[] { "java.io",
				"java.tools;version=\"1.3\"",
				"java.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"java.ui;foo=\"bar\";version=\"[1,1]\"",
				"java.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };

		String[] exportedPackages = new String[] { "test.abc",
				"test.tools;version=\"1.3\"",
				"test.hogwarts;foo=\"bar\";atum=\"--\";version=\"1.6\"",
				"test.ui;foo=\"bar\";version=\"[1,1]\"",
				"test.foo.bar;foo=\"bar\";version=\"[1.3,2)\"", };
		JarBundleFile testFile = new JarBundleFile("testfile.jar", importedPackages,
				exportedPackages);
		assertTrue(testFile.toXML().length() == 538);
	}
}
