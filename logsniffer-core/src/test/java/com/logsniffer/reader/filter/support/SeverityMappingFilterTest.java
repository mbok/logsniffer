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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.logsniffer.fields.FieldsMap;
import com.logsniffer.fields.filter.support.SeverityMappingFilter;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;

/**
 * Test for {@link SeverityMappingFilter}.
 * 
 * @author mbok
 * 
 */
public class SeverityMappingFilterTest {
	private SeverityMappingFilter f;
	private Map<String, SeverityLevel> levels;
	private FieldsMap fields;

	@Before
	public void setUp() {
		f = new SeverityMappingFilter();
		f.setOverride(true);
		f.setSourceField("prio");
		levels = new HashMap<>();
		levels.put("1", new SeverityLevel("INFO", 1, SeverityClassification.INFORMATIONAL));
		levels.put("W", new SeverityLevel("WARN", 2, SeverityClassification.WARNING));
		f.setSeverityLevels(levels);

		fields = new FieldsMap();
	}

	@Test
	public void testEmptySourceField() {
		f.filter(fields);
		Assert.assertNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testInfoMapping() {
		fields.put("prio", "1");
		f.filter(fields);
		Assert.assertNotNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
		Assert.assertEquals(levels.get("1"), fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testWarnMapping() {
		fields.put("prio", "w");
		f.filter(fields);
		Assert.assertNotNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
		Assert.assertEquals(levels.get("W"), fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testWarnMappingCaseSensitive() {
		fields.put("prio", "w");
		f.setIgnoreCase(false);
		f.filter(fields);
		Assert.assertNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));

		fields.put("prio", "W");
		f.filter(fields);
		Assert.assertEquals(levels.get("W"), fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testEmptyFallback() {
		fields.put("prio", "UNKNOWN");
		f.filter(fields);
		Assert.assertNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testValidFallback() {
		fields.put("prio", "UNKNOWN");
		final SeverityLevel fallback = new SeverityLevel();
		f.setFallback(fallback);
		f.filter(fields);
		Assert.assertEquals(fallback, fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testOverride() {
		final SeverityLevel preset = new SeverityLevel();
		fields.put(LogEntry.FIELD_SEVERITY_LEVEL, preset);
		// Assert override false
		f.setOverride(false);
		fields.put("prio", "W");
		f.filter(fields);
		Assert.assertEquals(preset, fields.get(LogEntry.FIELD_SEVERITY_LEVEL));

		// Assert override true
		f.setOverride(true);
		f.filter(fields);
		Assert.assertEquals(levels.get("W"), fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

	@Test
	public void testMappingFromNonStringInput() {
		fields.put("prio", 1);
		f.filter(fields);
		Assert.assertNotNull(fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
		Assert.assertEquals(levels.get("1"), fields.get(LogEntry.FIELD_SEVERITY_LEVEL));
	}

}
