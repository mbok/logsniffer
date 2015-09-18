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
package com.logsniffer.event.support;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.event.EventData;
import com.logsniffer.model.LogEntry;

/**
 * Test for {@link RegexScanner}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RegexScannerTest.class, CoreAppConfig.class })
public class RegexScannerTest {
	private LogEntry entry;
	private RegexScanner scanner;

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Before
	public void setUp() {
		entry = new LogEntry();
		scanner = new RegexScanner();
	}

	@Test
	public void testVariants() {
		scanner.setDotAll(true);
		scanner.setMultiLine(true);
		scanner.setCaseInsensitive(true);
		scanner.setPattern("^(\\d+).*$.*^(a+)$");
		entry.setRawContent("1234b\nAaA");
		EventData e = scanner.matches(entry);
		assertNotNull(e);
		assertEquals(2, e.getFields().size());
		assertEquals("1234", e.getFields().get("1"));
		assertEquals("AaA", e.getFields().get("2"));

		// Disable multile
		scanner.setMultiLine(false);
		e = scanner.matches(entry);
		assertNull(e);
		scanner.setPattern("^(\\d+).+(a+)$");
		e = scanner.matches(entry);
		assertEquals(2, e.getFields().size());

		// Disable dotall
		scanner.setDotAll(false);
		e = scanner.matches(entry);
		assertNull(e);
		scanner.setPattern("^(\\d+).+\n(a+)$");
		e = scanner.matches(entry);
		assertEquals(2, e.getFields().size());
		assertEquals("1234", e.getFields().get("1"));
		assertEquals("AaA", e.getFields().get("2"));

		// Test serialization
		RegexScanner s2 = configManager.createBeanFromJSON(RegexScanner.class,
				configManager.saveBeanToJSON(scanner));
		e = s2.matches(entry);
		assertEquals(2, e.getFields().size());
		assertEquals("1234", e.getFields().get("1"));
		assertEquals("AaA", e.getFields().get("2"));
	}
}
