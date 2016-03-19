package com.logsniffer.system.version;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link VersionInfo}.
 * 
 * @author mbok
 *
 */
public class VersionInfoTest {
	@Test
	public void testPartsExtraction() {
		VersionInfo v = new VersionInfo();
		v.setName("0.5.4");
		Assert.assertEquals(3, v.getVersionParts().length);
		Assert.assertEquals(0, v.getVersionParts()[0]);
		Assert.assertEquals(5, v.getVersionParts()[1]);
		Assert.assertEquals(4, v.getVersionParts()[2]);

		// Test version parts with letters
		v = new VersionInfo("0.6-alpha");
		Assert.assertEquals(2, v.getVersionParts().length);
		Assert.assertEquals(0, v.getVersionParts()[0]);
		Assert.assertEquals(6, v.getVersionParts()[1]);

		// Test empty version
		Assert.assertEquals(0, new VersionInfo("").getVersionParts().length);
		Assert.assertEquals(1, new VersionInfo(".1").getVersionParts().length);
		Assert.assertEquals(1, new VersionInfo(".1").getVersionParts()[0]);
	}

	@Test
	public void testComparison() {
		Assert.assertTrue(new VersionInfo("1.2.3").compareTo(new VersionInfo("0.5")) > 0);
		Assert.assertTrue(new VersionInfo("1.2.3").compareTo(new VersionInfo("1.2.3.0")) == 0);
		Assert.assertTrue(new VersionInfo("0.2.3").compareTo(new VersionInfo("1.2.3.0")) < 0);
	}
}
