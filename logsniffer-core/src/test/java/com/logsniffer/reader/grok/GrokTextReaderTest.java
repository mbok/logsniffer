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
package com.logsniffer.reader.grok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

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
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.support.ByteArrayLog;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.log4j.Log4jTextReaderTest;
import com.logsniffer.util.grok.GrokAppConfig;
import com.logsniffer.util.grok.GrokConsumerConstructor;

/**
 * Test for {@link GrokTextReader}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, GrokTextReaderTest.class, CoreAppConfig.class })
@Configuration
public class GrokTextReaderTest {

	@Bean
	public GrokConsumerConstructor grokReaderConstructor() {
		return new GrokConsumerConstructor();
	}

	@Autowired
	private BeanConfigFactoryManager configManager;

	private GrokTextReader grokReader;

	@Before
	public void initReader() {
		grokReader = new GrokTextReader();
		grokReader.getGrokBean().setPattern("%{SYSLOGBASE}-my");
		grokReader.setOverflowAttribute("msg");
		grokReader = configManager.createBeanFromJSON(GrokTextReader.class, configManager.saveBeanToJSON(grokReader));
	}

	@Test
	public void testReader() throws IOException, FormatException {
		assertEquals("%{SYSLOGBASE}-my", grokReader.getGrokBean().getPattern());
		assertEquals("msg", grokReader.getOverflowAttribute());
		String logLine1 = "Nov  1 21:14:23 <1.3> localhost kernel:-my";
		ByteArrayLog log = Log4jTextReaderTest.createLog(0, logLine1 + "\noverflow");
		grokReader.setOverflowAttribute(null);
		LogEntry[] entries = Log4jTextReaderTest.readEntries(grokReader, log, null, 1);
		assertEquals(1, entries.length);
		assertEquals("Nov  1 21:14:23", entries[0].getFields().get("timestamp"));
		assertEquals(1, entries[0].getFields().get("facility"));
		assertEquals(3, entries[0].getFields().get("priority"));
		assertEquals("localhost", entries[0].getFields().get("logsource"));
		assertEquals("kernel", entries[0].getFields().get("program"));
		assertNull(entries[0].getFields().get("msg"));

		// Test overflow attachment to "msg" field
		grokReader.setOverflowAttribute("msg");
		entries = Log4jTextReaderTest.readEntries(grokReader,
				Log4jTextReaderTest.createLog(0, logLine1 + "\noverflow1\noverflow2"), null, 1);
		assertEquals(1, entries.length);
		assertEquals("Nov  1 21:14:23", entries[0].getFields().get("timestamp"));
		assertEquals(1, entries[0].getFields().get("facility"));
		assertEquals(3, entries[0].getFields().get("priority"));
		assertEquals("localhost", entries[0].getFields().get("logsource"));
		assertEquals("kernel", entries[0].getFields().get("program"));
		assertEquals("overflow1\noverflow2", entries[0].getFields().get("msg"));

		// Test overflow to existing field "program"
		grokReader.setOverflowAttribute("program");
		entries = Log4jTextReaderTest.readEntries(grokReader, Log4jTextReaderTest.createLog(0, logLine1 + "\noverflow"),
				null, 1);
		assertEquals(1, entries.length);
		assertEquals("kernel\noverflow", entries[0].getFields().get("program"));

	}
}
