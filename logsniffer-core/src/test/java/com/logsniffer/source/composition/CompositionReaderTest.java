package com.logsniffer.source.composition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.support.DefaultPointer;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.support.BufferedConsumer;

/**
 * Test for {@link CompositionReader}.
 * 
 * @author mbok
 *
 */
public class CompositionReaderTest {
	private static final class DummySubReader implements LogEntryReader<LogInputStream> {
		private final int maxCount;
		private final int factor;
		private final int start;

		public DummySubReader(final int maxCount, final int factor, final int start) {
			super();
			this.maxCount = maxCount;
			this.factor = factor;
			this.start = start;
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void readEntries(final Log log, final LogRawAccess<LogInputStream> logAccess,
				final LogPointer startOffset, final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer)
						throws IOException, FormatException {
			for (int i = 0; i < maxCount; i++) {
				final LogEntry entry = new LogEntry();
				entry.setStartOffset(new DefaultPointer(i, maxCount));
				entry.setEndOffset(new DefaultPointer(i + 1, maxCount));
				entry.setTimeStamp(new Date(i * factor + start));
				consumer.consume(log, logAccess, entry);
			}
		}

		@Override
		public List<SeverityLevel> getSupportedSeverities() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	@Test
	public void testA() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompositionReader r = new CompositionReader(subLogs);
		subLogs.add(new LogInstance(1, Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class),
				new DummySubReader(200, 2, 0)));
		subLogs.add(new LogInstance(2, Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class),
				new DummySubReader(250, 2, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		r.readEntries(Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class), null, c);

		Assert.assertEquals(450, c.getBuffer().size());
	}
}
