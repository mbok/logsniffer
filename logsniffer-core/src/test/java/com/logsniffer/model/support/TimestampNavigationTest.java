package com.logsniffer.model.support;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Test for {@link TimestampNavigation}.
 * 
 * @author mbok
 *
 */
public class TimestampNavigationTest {
	private ByteLogAccess mockAccess;

	@Before
	public void setUp() throws IOException {
		mockAccess = Mockito.mock(ByteLogAccess.class);
		Mockito.when(mockAccess.createRelative(Mockito.any(LogPointer.class), Mockito.anyLong()))
				.thenAnswer(new Answer<LogPointer>() {
					@Override
					public LogPointer answer(final InvocationOnMock invocation) throws Throwable {
						final DefaultPointer from = (DefaultPointer) invocation.getArguments()[0];
						final long offset = (long) invocation.getArguments()[1];
						return new DefaultPointer(from == null ? offset : from.getOffset() + offset, Long.MAX_VALUE);
					}
				});
		Mockito.doAnswer(new Answer<Long>() {
			@Override
			public Long answer(final InvocationOnMock invocation) throws Throwable {
				return ((DefaultPointer) invocation.getArguments()[1]).getOffset();
			}
		}).when(mockAccess).getDifference(Mockito.any(LogPointer.class), Mockito.any(LogPointer.class));
	}

	@Test
	public void testTrivialCases() throws IOException {
		final Log log = buildLog(0, 10, 1000);
		final LogEntryReader<ByteLogAccess> reader = buildReader();
		final TimestampNavigation nav = new TimestampNavigation(log, mockAccess, reader);

		// Direct hit in the middle
		DefaultPointer target = (DefaultPointer) nav.absolute(new Date(5)).get();
		Assert.assertEquals(5, target.getOffset());

		// Worst case in the left boundary
		target = (DefaultPointer) nav.absolute(new Date(0)).get();
		Assert.assertEquals(0, target.getOffset());

		// Worst case in the right boundary
		target = (DefaultPointer) nav.absolute(new Date(9)).get();
		Assert.assertEquals(9, target.getOffset());

		// Worst case with haystack now()
		target = (DefaultPointer) nav.absolute(new Date()).get();
		Assert.assertEquals(10, target.getOffset());

		// Worst case with haystack before the first entry
		final Log log2 = buildLog(10, 10, 1000);
		final TimestampNavigation nav2 = new TimestampNavigation(log2, mockAccess, reader);
		target = (DefaultPointer) nav2.absolute(new Date(1)).get();
		Assert.assertEquals(0, target.getOffset());
	}

	@Test
	public void testBigLog() throws IOException {
		final Log log = buildLog(0, 1000000, 10000000);
		final LogEntryReader<ByteLogAccess> reader = buildReader();
		final TimestampNavigation nav = new TimestampNavigation(log, mockAccess, reader);

		// Worst case with haystack now()
		final DefaultPointer target = (DefaultPointer) nav.absolute(new Date()).get();
		Assert.assertEquals(log.getSize(), target.getOffset());

	}

	private Log buildLog(final int startAt, final long size, final int unformattedRatio) {
		final LogEntry[] entries = new LogEntry[(int) size];
		long lastFormatedIndex = -1;
		for (int i = 0; i < size; i++) {
			if (i % unformattedRatio != 0) {
				final LogEntry entry = new LogEntry();
				entry.setTimeStamp(new Date(startAt + i));
				entry.setStartOffset(new DefaultPointer(lastFormatedIndex + 1, size));
				entry.setEndOffset(new DefaultPointer(lastFormatedIndex + 1 + (i - lastFormatedIndex), size));
				entries[i] = entry;
				lastFormatedIndex = i;
			}
		}
		return new TestLog(null, size, entries);
	}

	private static class TestLog implements Log {
		private final String path;
		private final long size;
		private final LogEntry[] entries;

		public TestLog(final String path, final long size, final LogEntry[] entries) {
			super();
			this.path = path;
			this.size = size;
			this.entries = entries;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public long getSize() {
			return size;
		}

		@Override
		public SizeMetric getSizeMetric() {
			return SizeMetric.BYTE;
		}

		@Override
		public long getLastModified() {
			return 0;
		}

		@Override
		public String getName() {
			return "abc";
		}

	}

	private LogEntryReader<ByteLogAccess> buildReader() {
		return new LogEntryReader<ByteLogAccess>() {

			@Override
			public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void readEntries(final Log log, final ByteLogAccess logAccess, final LogPointer startOffset,
					final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
				final TestLog tlog = (TestLog) log;
				int offset = (int) ((DefaultPointer) startOffset).getOffset();
				for (; offset < tlog.size; offset++) {
					if (tlog.entries[offset] != null) {
						if (!consumer.consume(tlog, logAccess, tlog.entries[offset])) {
							return;
						}
					} else {
						final LogEntry unformatted = new LogEntry();
						unformatted.setUnformatted(true);
						if (!consumer.consume(tlog, logAccess, unformatted)) {
							return;
						}
					}
				}
			}

			@Override
			public void readEntriesReverse(final Log log, final ByteLogAccess logAccess, final LogPointer startOffset,
					final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public List<SeverityLevel> getSupportedSeverities() {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}
}
