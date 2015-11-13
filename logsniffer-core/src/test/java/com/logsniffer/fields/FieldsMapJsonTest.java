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
package com.logsniffer.fields;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;

/**
 * Test public serialization of {@link FieldsMap}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
@Configuration
public class FieldsMapJsonTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testSerialization() throws JsonProcessingException {
		FieldsMap map = new FieldsMap();
		map.put("fa", new Date(0));
		String jsonStr = mapper.writeValueAsString(map);
		LOGGER.info("Serialized {} to: {}", map, jsonStr);
		JSONObject parsedJson = JSONObject.fromObject(jsonStr);
		Assert.assertNotNull(parsedJson.get("@types"));
		Assert.assertEquals(FieldBaseTypes.DATE.name(), parsedJson
				.getJSONObject("@types").getString("fa"));
		Assert.assertEquals(0, parsedJson.getInt("fa"));
	}

	@Test
	public void testSerializationEmpty() throws JsonProcessingException {
		FieldsMap map = new FieldsMap();
		String jsonStr = mapper.writeValueAsString(map);
		LOGGER.info("Serialized {} to: {}", map, jsonStr);
		JSONObject parsedJson = JSONObject.fromObject(jsonStr);
		Assert.assertTrue(parsedJson.isEmpty());
	}

	@Test
	public void testDeserialization() throws IOException {
		FieldsMap map = new FieldsMap();
		map.put("fa", new Date(120));
		String jsonStr = mapper.writeValueAsString(map);
		LOGGER.info("Serialized {} to: {}", map, jsonStr);

		FieldsMap dMap = mapper.readValue(jsonStr, FieldsMap.class);
		Assert.assertNull(dMap.get("@types"));
		Assert.assertEquals(new Date(120), dMap.get("fa"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeserializationOfUnknownFields() throws IOException {
		FieldsMap map = new FieldsMap();
		map.put("fa", new Date(120));
		JSONObject parsedJson = JSONObject.fromObject(mapper
				.writeValueAsString(map));
		parsedJson.element("unknown", Collections.singletonMap("int", 134));
		String jsonStr = parsedJson.toString();
		LOGGER.info("Serialized in extended form to: {}", jsonStr);

		FieldsMap dMap = mapper.readValue(jsonStr, FieldsMap.class);
		Assert.assertNull(dMap.get("@types"));
		Assert.assertEquals(2, dMap.size());
		Assert.assertEquals(new Date(120), dMap.get("fa"));
		Assert.assertTrue(dMap.get("unknown") instanceof Map);
		Assert.assertEquals(134,
				((Map<String, Object>) dMap.get("unknown")).get("int"));
	}

	@Test
	public void testDeserializationSpeed() throws IOException {
		FieldsMap map = new FieldsMap();
		map.put("_severity", new SeverityLevel("DEBUG", 2,
				SeverityClassification.DEBUG));
		map.put("_timestamp", new Date());
		// Ca 1k raw message
		map.put("_raw", StringUtils.repeat("some text with len 23", 50));
		for (int i = 0; i < 7; i++) {
			map.put("somestrfield" + i, StringUtils.repeat("some text " + i, 5));
		}
		for (int i = 0; i < 3; i++) {
			map.put("someintfield" + i, (int) (Math.random() * 1000000));
		}
		for (int i = 0; i < 2; i++) {
			map.put("somedoublefield" + i, (Math.random() * 1000000));
		}
		String jsonStr = mapper.writeValueAsString(map);
		LOGGER.info("Serialized to: {}", jsonStr);
		long start = System.currentTimeMillis();
		int i = 0;
		FieldsMap desrerializedMap = null;
		for (; i < 1000; i++) {
			desrerializedMap = mapper.readValue(jsonStr, FieldsMap.class);
		}
		LOGGER.info("Deserialized fields {} times in {}ms", i,
				System.currentTimeMillis() - start);
		Assert.assertEquals(map, desrerializedMap);
	}
}
