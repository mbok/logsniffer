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

import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.logsniffer.event.Event;
import com.logsniffer.event.IncrementData;
import com.logsniffer.event.LogEntryReaderStrategy;
import com.logsniffer.event.Scanner.EventConsumer;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.model.support.DefaultPointer;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.LogEntryReader.LogEntryConsumer;

/**
 * Test for {@link LevelScanner}.
 * 
 * @author mbok
 * 
 */
public class LevelScannerTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testMatching() throws IOException, FormatException {
		final LevelScanner m = new LevelScanner();
		m.setSeverityNumber(5); // e.g. WARN

		final DefaultPointer p = new DefaultPointer(0, 4);

		final Log log = Mockito.mock(Log.class);
		final LogRawAccess<LogInputStream> logAccess = Mockito.mock(LogRawAccess.class);
		final LogInputStream lis = Mockito.mock(LogInputStream.class);
		Mockito.when(logAccess.getFromJSON(Mockito.anyString())).thenAnswer(new Answer<LogPointer>() {
			@Override
			public LogPointer answer(final InvocationOnMock invocation) throws IOException {
				return DefaultPointer.fromJSON(invocation.getArguments()[0].toString());
			}
		});
		Mockito.when(lis.getPointer()).thenReturn(p);
		Mockito.when(logAccess.getInputStream(null)).thenReturn(lis);
		Mockito.when(log.getSize()).thenReturn(3l);
		final LogEntryReader<LogInputStream> reader = Mockito.mock(LogEntryReader.class);
		final LogEntry entry1 = new LogEntry();
		entry1.setSeverity(new SeverityLevel("INFO", 3, SeverityClassification.INFORMATIONAL));
		entry1.setEndOffset(new DefaultPointer(2, 4));
		final LogEntry entry2 = new LogEntry();
		entry2.setRawContent("entry2-content");
		entry2.setSeverity(new SeverityLevel("WARN", 5, SeverityClassification.NOTICE));
		entry2.setStartOffset(new DefaultPointer(2, 4));
		entry2.setEndOffset(new DefaultPointer(3, 4));
		final LogEntry entry3 = new LogEntry();
		entry3.setSeverity(new SeverityLevel("ERROR", 6, SeverityClassification.ERROR));
		entry3.setEndOffset(new DefaultPointer(4, 4));

		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws IOException {
				((LogEntryConsumer) invocation.getArguments()[3]).consume(log, logAccess, entry1);
				return null;
			}
		}).when(reader).readEntries(Mockito.eq(log), Mockito.eq(logAccess), Mockito.isNull(LogPointer.class),
				Mockito.any(LogEntryConsumer.class));
		final IncrementData idata = new IncrementData();
		EventConsumer eventConsumer = Mockito.mock(EventConsumer.class);
		m.find(reader, Mockito.mock(LogEntryReaderStrategy.class), log, logAccess, idata, eventConsumer);
		Mockito.verifyZeroInteractions(eventConsumer);
		Assert.assertEquals(new DefaultPointer(2, 4), idata.getNextOffset());
		Assert.assertEquals(false, idata.getNextOffset(logAccess).isEOF());

		// Verify minBytesToRead
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws IOException {
				((LogEntryConsumer) invocation.getArguments()[3]).consume(log, logAccess, entry2);
				return null;
			}
		}).when(reader).readEntries(Mockito.eq(log), Mockito.eq(logAccess), Mockito.eq(new DefaultPointer(2, 4)),
				Mockito.any(LogEntryConsumer.class));

		eventConsumer = Mockito.mock(EventConsumer.class);
		final LogEntryReaderStrategy readerStrategy = Mockito.mock(LogEntryReaderStrategy.class);
		Mockito.when(readerStrategy.continueReading(log, logAccess, entry1)).thenReturn(true);
		Mockito.when(readerStrategy.continueReading(log, logAccess, entry2)).thenReturn(false);
		m.find(reader, readerStrategy, log, logAccess, idata, eventConsumer);
		Mockito.verify(eventConsumer, Mockito.times(1)).consume(Mockito.any(Event.class));
		Mockito.verify(eventConsumer, Mockito.times(1)).consume(Mockito.argThat(new BaseMatcher<Event>() {
			@Override
			public boolean matches(final Object arg0) {
				final Event event = (Event) arg0;
				return event.getEntries().size() == 1 && event.getEntries().get(0).equals(entry2);
			}

			@Override
			public void describeTo(final Description arg0) {
				// TODO Auto-generated method stub

			}
		}));
		Assert.assertEquals(false, idata.getNextOffset(logAccess).isEOF());
	}
}
