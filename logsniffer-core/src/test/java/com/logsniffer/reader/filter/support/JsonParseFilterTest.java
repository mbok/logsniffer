/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.reader.filter.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.reader.filter.support.JsonParseFilter;
import com.logsniffer.reader.filter.support.JsonParseFilter.JsonParseFilterBuilder;

/**
 * Test for {@link JsonParseFilter}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class,
		JsonParseFilterTest.class })
@Configuration
public class JsonParseFilterTest {
	@Bean
	public BeanConfigFactoryManager configManager() {
		return new BeanConfigFactoryManager();
	}

	@Bean
	public JsonParseFilterBuilder filterConstructor() {
		return new JsonParseFilterBuilder();
	}

	@Autowired
	private JsonParseFilterBuilder constructor;

	@Autowired
	private BeanConfigFactoryManager configManager;

	private JsonParseFilter f;
	private FieldsMap fields;

	@Before
	public void setUp() {
		f = new JsonParseFilter();
		f.setSourceField("prio");
		constructor.postConstruct(f, configManager);
		fields = new FieldsMap();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFiltering() {
		f.setTargetField("jsonObj");
		f.setSourceField("jsonStr");
		fields.put("jsonStr", "{'abc':'def'}");

		// Check
		f.filter(fields);
		Assert.assertNotNull(fields.get("jsonObj"));
		Assert.assertEquals("def",
				((Map<String, Object>) fields.get("jsonObj")).get("abc"));

		LinkedHashMap<String, FieldBaseTypes> types = new LinkedHashMap<>();
		f.filterKnownFields(types);
		Assert.assertEquals(1, types.size());
		Assert.assertEquals(FieldBaseTypes.OBJECT, types.get("jsonObj"));
	}

	@Test
	public void testEmptyFallbackEmptySource() {
		f.setTargetField("jsonObj");
		f.setSourceField("jsonStr");

		// Check
		f.filter(fields);
		Assert.assertNull(fields.get("jsonObj"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFallbackEmptySource() {
		f.setTargetField("jsonObj");
		f.setSourceField("jsonStr");
		f.setFallbackJsonValue("[]");

		// Check
		f.filter(fields);
		Assert.assertNotNull(fields.get("jsonObj"));
		Assert.assertTrue(((List<String>) fields.get("jsonObj")).isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFallbackInvalidSource() {
		f.setTargetField("jsonObj");
		f.setSourceField("jsonStr");
		f.setFallbackJsonValue("{'fallback':true}");
		fields.put("jsonStr", "{kaputt");

		// Check
		f.filter(fields);
		Assert.assertNotNull(fields.get("jsonObj"));
		Assert.assertEquals(true, (boolean) ((Map<String, Object>) fields
				.get("jsonObj")).get("fallback"));
	}

	@Test
	public void testFallbackInvalid() {
		f.setTargetField("jsonObj");
		f.setSourceField("jsonStr");
		f.setFallbackJsonValue("{invalid");

		// Check
		f.filter(fields);
		Assert.assertNull(fields.get("jsonObj"));
	}
}
