package com.logsniffer.user.profile.support;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.user.profile.ProfileSettingsStorage;

/**
 * Test for {@link H2ProfileSettingsStorage}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { H2ProfileSettingsStorageTest.class, CoreAppConfig.class,
		QaDataSourceAppConfig.class })
@Configuration
public class H2ProfileSettingsStorageTest {
	@Bean
	public H2ProfileSettingsStorage storage() {
		return new H2ProfileSettingsStorage();
	}

	@Autowired
	private ProfileSettingsStorage storage;

	@Test
	public void testA() {
		Assert.assertNull(storage.getSettings("a", "/root/my", false));
		final FieldsMap s1 = new FieldsMap();
		s1.put("key", new Date(123456789));
		storage.storeSettings("a", "/root/my", s1);

		// Insert
		final FieldsMap s1check = storage.getSettings("a", "/root/my", false);
		Assert.assertNotNull(s1check);
		Assert.assertEquals(1, s1check.size());
		Assert.assertEquals(s1.get("key"), s1check.get("key"));

		// Update
		s1check.put("key3", true);
		storage.storeSettings("a", "/root/my", s1check);
		final FieldsMap s1check2 = storage.getSettings("a", "/root/my", false);
		Assert.assertNotNull(s1check2);
		Assert.assertEquals(2, s1check2.size());
		Assert.assertEquals(s1.get("key"), s1check2.get("key"));
		Assert.assertTrue((boolean) s1check2.get("key3"));

		// Insert nested
		final FieldsMap nested = new FieldsMap();
		nested.put("nested", true);
		storage.storeSettings("a", "/root/my/sub", nested);
		final FieldsMap nestedCheck = storage.getSettings("a", "/root/my/sub", false);
		Assert.assertNotNull(nestedCheck);
		Assert.assertEquals(1, nestedCheck.size());
		Assert.assertTrue((boolean) nestedCheck.get("nested"));

		final FieldsMap s1check3 = storage.getSettings("a", "/root/my", false);
		Assert.assertNotNull(s1check3);
		Assert.assertEquals(2, s1check3.size());
		Assert.assertEquals(s1.get("key"), s1check3.get("key"));
		Assert.assertTrue((boolean) s1check3.get("key3"));

		// Delete
		storage.deleteSettings("a", "/root/my", false);
		Assert.assertNull(storage.getSettings("a", "/root/my", false));
		final FieldsMap nestedCheck2 = storage.getSettings("a", "/root/my/sub", false);
		Assert.assertNotNull(nestedCheck2);
		Assert.assertEquals(1, nestedCheck2.size());
		Assert.assertTrue((boolean) nestedCheck2.get("nested"));

		// Delete recursive
		storage.deleteSettings("a", "/root/my", true);
		Assert.assertNull(storage.getSettings("a", "/root/my", false));
		Assert.assertNull(storage.getSettings("a", "/root/my/sub", false));
	}
}
