package lumina;

import junit.framework.TestCase;
import lumina.license.VersionService;

public class TestVersionService extends TestCase {

	/**
	 * Tests the comparison of two version numbers.
	 */
	public void testCompareVersionNumbers() {
		assertEquals(1, VersionService.compareVersionNumber("2.0", "1.0"));
		assertEquals(-1, VersionService.compareVersionNumber("1.0", "2.0"));
		assertEquals(1, VersionService.compareVersionNumber("1.10", "1.9"));
		assertEquals(1, VersionService.compareVersionNumber("1.0.1", "1.0"));
		assertEquals(0, VersionService.compareVersionNumber("1.0.0", "1.0"));
	}
}
