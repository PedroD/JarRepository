package simplesolutions.dependencyserver.impl;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import simplesolutions.dependencyserver.impl.JarBundleFile.PackageVersion;
import junit.framework.TestCase;

/**
 * The Class TestJarFile.
 */
public final class TestJarFile extends TestCase {

	/**
	 * Test imports/exports.
	 */
	public void testReadImportsExportsFromJarFile() {
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
		JarBundleFile testFile = new JarBundleFile("testfile.jar",
				importedPackages, exportedPackages);
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
		JarBundleFile testFile = new JarBundleFile("testfile.jar",
				importedPackages, exportedPackages);
		/*
		 * Test imported packages
		 */
		for (String p : importedPackages) {
			String version = TestUtils.getPackageVersion(p);
			String packageName = p.split(";")[0];
			assertEquals(version,
					testFile.getImportedPackages().get(packageName).toString());
		}
		/*
		 * Test exported packages
		 */
		for (String p : exportedPackages) {
			String version = TestUtils.getPackageVersion(p);
			String packageName = p.split(";")[0];
			assertEquals(version,
					testFile.getExportedPackages().get(packageName).toString());
		}
	}

	/**
	 * Test version compatibility.
	 */
	public void testVersionCompatibility() {
		PackageVersion version1;
		PackageVersion version2;
		/*
		 * Test two concrete versions.
		 */
		version1 = new PackageVersion("goo.gle;version=\"1.5\"");
		version2 = new PackageVersion("goo.gle;version=\"1.5\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));
		/*
		 * Test one concrete version.
		 */
		version1 = new PackageVersion("goo.gle;version=\"1.5\"");
		version2 = new PackageVersion("goo.gle;version=\"[1,2)\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));
		/*
		 * Test boundaries.
		 */
		version1 = new PackageVersion("goo.gle;version=\"1.5\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"1.5\"");
		version2 = new PackageVersion("goo.gle;version=\"[1.5,2)\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2.5\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"1\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"0.5\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"1.5\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"1.9\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2\"");
		version2 = new PackageVersion("goo.gle;version=\"[1.5,2]\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2\"");
		version2 = new PackageVersion("goo.gle;version=\"[2,2]\"");
		assertTrue(version1.isCompatible(version2));
		assertTrue(version2.isCompatible(version1));
		/*
		 * Test impossible comparisons.
		 */
		version1 = new PackageVersion("goo.gle;version=\"[2,3]\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"[1.5,2]\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		version2 = new PackageVersion("goo.gle;version=\"(1.5,2]\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));

		version1 = new PackageVersion("goo.gle;version=\"2\"");
		version2 = new PackageVersion("goo.gle;version=\"(2,2)\"");
		assertFalse(version1.isCompatible(version2));
		assertFalse(version2.isCompatible(version1));
	}

	/**
	 * Test the XML file generating functions.
	 */
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
		JarBundleFile testFile = new JarBundleFile("testfile.jar",
				importedPackages, exportedPackages);

		/*
		 * We test for the MD5 hash of the created XML to see if it is the same
		 * as the MD5 of the expected (correct) XML output.
		 */
		String xmlOutput = testFile.toXML();
		String desiredMD5 = "e54b021804f7a4b86cfe8f4cc4a44d13";
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(xmlOutput.getBytes(), 0, xmlOutput.length());
			String resultingMD5 = new BigInteger(1, m.digest()).toString(16);
			assertTrue(resultingMD5.equals(desiredMD5));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test bundle database.
	 * 
	 * @throws InterruptedException
	 */
	public void testBundleDatabase() throws InterruptedException {
		/*
		 * Clean possible conflicting test file.
		 */
		final String testJarName = "test____123__123___12333333___222__111.jar";
		final String expectedFoundBundle = Main.getJarsFolderName()
				+ File.separator + testJarName;
		File f = new File(expectedFoundBundle);
		if (f.exists())
			f.delete();
		/*
		 * Start the database thread.
		 */
		BundleDatabase db = new BundleDatabase();
		db.start();
		/*
		 * Very nice MANIFEST.MF to test.
		 */
		StringBuilder manifest = new StringBuilder();
		manifest.append("Manifest-Version: 1.0\r\n");
		manifest.append("Export-Package: lumina.kernel;uses:=\"org.osgi.service.log,org.osgi.fra\r\n");
		manifest.append(" mework\";version=\"1.2.3\",lumina.kernel.internal;x-internal:=true,lumina.kernel.osgi;us\r\n");
		manifest.append(" es:=\"org.osgi.framework\",lumina.kernel.osgi.factories,lumina.kernel.o\r\n");
		manifest.append(" sgi.registries;uses:=\"lumina.kernel.osgi.factories\",lumina.kernel.osg\r\n");
		manifest.append(" i.shell,lumina.kernel.osgi.trackers;uses:=\"org.osgi.util.tracker,org.\r\n");
		manifest.append(" osgi.framework\",lumina.kernel.sequences,lumina.kernel.util,lumina.ker\r\n");
		manifest.append(" nel.util.xml.banana;uses:=\"org.eclipse.swt.graphics,org.w3c.dom\";version=\"2.3.1\"\r\n");
		manifest.append("Bundle-Version: 0.0.1\r\n");
		manifest.append("Built-By: El Gran Pedro\r\n");
		manifest.append("Build-Jdk: 1.7.0_51\r\n");
		manifest.append("Bundle-Name: lumina.kernel\r\n");
		manifest.append("Bundle-ManifestVersion: 2\r\n");
		manifest.append("Created-By: Apache Maven\r\n");
		manifest.append("Import-Package: lumina.api.properties,org.eclipse.swt.graphics,org.osg\r\n");
		manifest.append(" i.framework;version=\"1.5.0\",org.osgi.service.log;version=\"1.3.0\",org.\r\n");
		manifest.append(" osgi.service.packageadmin;version=\"1.2.0\",org.osgi.util.tracker;versi\r\n");
		manifest.append(" on=\"1.4.0\"\r\n");
		manifest.append("Bundle-SymbolicName: lumina.kernel\r\n");
		manifest.append("Bundle-RequiredExecutionEnvironment: JavaSE-1.6\r\n");
		manifest.append("Archiver-Version: Plexus Archiver\r\n");
		/*
		 * Should not find any bundle exporting this.
		 */
		assertNull(db.getJarProvidingPackage("lumina.kernel.util.xml.banana"));
		/*
		 * Create test bundle and put it in the repo.
		 */
		File tempBundle = TestUtils.createTemporaryBundle(testJarName,
				manifest.toString());
		tempBundle = TestUtils.moveFileToJarsFolder(tempBundle);
		Thread.sleep(500);
		/*
		 * Should find a bundle exporting these.
		 */
		assertEquals(expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana"));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"2.3.1\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"[1,3]\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"[1,3)\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"(1,3]\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"[2,3]\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"0\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;version=\"0.0.0\""));
		assertEquals(
				expectedFoundBundle,
				db.getJarProvidingPackage("lumina.kernel.util.xml.banana;uses=\"fdgfdg.gfdgfd\";version=\"[1,3]\""));
		/*
		 * Delete the bundle.
		 */
		tempBundle.delete();
		Thread.sleep(500);
		/*
		 * Test bundle deleted.
		 */
		assertNull(db.getJarProvidingPackage("lumina.kernel.util.xml.banana"));
		/*
		 * Clean the trash made.
		 */
		TestUtils.deleteTemporaryFolder();
	}
}
