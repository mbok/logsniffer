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
package com.logsniffer.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.file.WildcardLogsSource;

/**
 * Test for serializing / deserializing {@link ConfiguredBean}s.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
@Configuration
public class ConfigBeanJsonTest {
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigBeanJsonTest.class);
	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testSerializing() throws IOException {
		WildcardLogsSource source = new WildcardLogsSource();
		source.setName("Test");
		String json = mapper.writeValueAsString(source);
		logger.info("Serialized bean: {}", json);

		// Deserialize
		LogSource source2 = mapper.readValue(json, LogSource.class);
		Assert.assertEquals(WildcardLogsSource.class, source2.getClass());
	}
}
