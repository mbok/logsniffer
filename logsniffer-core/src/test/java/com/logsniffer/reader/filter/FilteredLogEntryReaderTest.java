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
package com.logsniffer.reader.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.LogEntryReader.LogEntryConsumer;

/**
 * Test for {@link FilteredLogEntryReader}.
 * 
 * @author mbok
 * 
 */
public class FilteredLogEntryReaderTest {
	private LogEntryReader<LogInputStream> targetReader;
	private List<FieldsFilter> filters;
	private LogEntryFilter f1;
	private LogEntryFilter f2;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		targetReader = Mockito.mock(LogEntryReader.class);
		filters = new ArrayList<>();
		f1 = Mockito.mock(LogEntryFilter.class);
		f2 = Mockito.mock(LogEntryFilter.class);
		filters.add(f1);
		filters.add(f2);
	}

	@Test
	public void testNotWrapping() {
		Assert.assertEquals(targetReader, FilteredLogEntryReader.wrappIfNeeded(targetReader, null));
		Assert.assertEquals(targetReader,
				FilteredLogEntryReader.wrappIfNeeded(targetReader, new ArrayList<FieldsFilter>()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFiltering() throws IOException, FormatException {
		final LogEntryReader<LogInputStream> r = FilteredLogEntryReader.wrappIfNeeded(targetReader, filters);
		Assert.assertNotEquals(r, targetReader);
		final Log log = Mockito.mock(Log.class);
		final LogRawAccess<LogInputStream> logAccess = Mockito.mock(LogRawAccess.class);
		final LogPointer startOffset = Mockito.mock(LogPointer.class);
		final LogEntryConsumer consumer = Mockito.mock(LogEntryConsumer.class);

		final LogEntry logEntry = new LogEntry();
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				final LogEntryConsumer consumer = (LogEntryConsumer) invocation.getArguments()[3];
				consumer.consume(log, Mockito.mock(LogPointerFactory.class), logEntry);
				return null;
			}
		}).when(targetReader).readEntries(Mockito.eq(log), Mockito.eq(logAccess), Mockito.eq(startOffset),
				Mockito.any(LogEntryConsumer.class));

		// Call
		r.readEntries(log, logAccess, startOffset, consumer);

		// Verify
		Mockito.verify(consumer).consume(Mockito.eq(log), Mockito.any(LogPointerFactory.class), Mockito.eq(logEntry));
		Mockito.verify(f1).filter(logEntry);
		Mockito.verify(f2).filter(logEntry);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilteringSupportedSeverities() {
		final LogEntryReader<LogInputStream> r = FilteredLogEntryReader.wrappIfNeeded(targetReader, filters);
		Assert.assertNotEquals(r, targetReader);
		// Use notModifieable list in relation to issue #10
		final List<SeverityLevel> sevs = Collections.emptyList();
		Mockito.when(targetReader.getSupportedSeverities()).thenReturn(sevs);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				final List<SeverityLevel> sevs2Filter = (List<SeverityLevel>) invocation.getArguments()[0];
				sevs2Filter.add(new SeverityLevel());
				return null;
			}
		}).when(f1).filterSupportedSeverities(Mockito.anyList());

		// Call
		Assert.assertEquals(sevs.size() + 1, r.getSupportedSeverities().size());

		// Verify
		Mockito.verify(f1).filterSupportedSeverities(Mockito.anyList());
		Mockito.verify(f2).filterSupportedSeverities(Mockito.anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilteringKnownFields() throws FormatException {
		final LogEntryReader<LogInputStream> r = FilteredLogEntryReader.wrappIfNeeded(targetReader, filters);
		Assert.assertNotEquals(r, targetReader);
		final LinkedHashMap<String, FieldBaseTypes> types = new LinkedHashMap<>();
		Mockito.when(targetReader.getFieldTypes()).thenReturn(types);

		// Call
		Assert.assertFalse(types == r.getFieldTypes());

		// Verify
		// Custom map should be created in relation to issue #10
		Mockito.verify(f1).filterKnownFields(Mockito.any(LinkedHashMap.class));
		Mockito.verify(f2).filterKnownFields(Mockito.any(LinkedHashMap.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOrderingOfSeverities() {
		final LogEntryReader<LogInputStream> r = FilteredLogEntryReader.wrappIfNeeded(targetReader, filters);
		final List<SeverityLevel> sevs = new ArrayList<>();
		sevs.add(new SeverityLevel("5", 5, SeverityClassification.EMERGENCY));
		Mockito.when(targetReader.getSupportedSeverities()).thenReturn(sevs);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				final List<SeverityLevel> sevs2Filter = (List<SeverityLevel>) invocation.getArguments()[0];
				sevs2Filter.add(new SeverityLevel("1", 1, SeverityClassification.INFORMATIONAL));
				return null;
			}
		}).when(f1).filterSupportedSeverities(Mockito.anyList());

		// Verify
		final List<SeverityLevel> supportedSeverities = r.getSupportedSeverities();
		Assert.assertEquals(1, supportedSeverities.get(0).getOrdinalNumber());
		Assert.assertEquals(5, supportedSeverities.get(1).getOrdinalNumber());
	}
}
