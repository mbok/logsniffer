package com.logsniffer.source.composition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.event.Event;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.support.ByteArrayLog;
import com.logsniffer.model.support.DefaultPointer;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.LogEntryReader.LogEntryConsumer;
import com.logsniffer.reader.support.BufferedConsumer;

/**
 * Test for {@link CompositionReader}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
public class CompositionReaderTest {
	private static Logger logger = LoggerFactory.getLogger(CompositionReaderTest.class);

	private static final class DummySubReader implements LogEntryReader<LogInputStream> {
		private final int maxCount;
		private final int factor;
		private final int start;
		private final int exceptionAt;

		public DummySubReader(final int maxCount, final int factor, final int start) {
			this(maxCount, factor, start, -1);
		}

		public DummySubReader(final int maxCount, final int factor, final int start, final int exceptionAt) {
			super();
			this.maxCount = maxCount;
			this.factor = factor;
			this.start = start;
			this.exceptionAt = exceptionAt;
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			return null;
		}

		@Override
		public void readEntries(final Log log, final LogRawAccess<LogInputStream> logAccess,
				final LogPointer startOffset, final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer)
						throws IOException, FormatException {
			for (int i = 0; i < maxCount; i++) {
				if (exceptionAt == i) {
					throw new IOException("Throw error at " + i);
				}
				final LogEntry entry = new LogEntry();
				entry.setStartOffset(new DefaultPointer(i, maxCount));
				entry.setEndOffset(new DefaultPointer(i + 1, maxCount));
				entry.setTimeStamp(new Date(i * factor + start));
				consumer.consume(log, logAccess, entry);
			}
		}

		@Override
		public List<SeverityLevel> getSupportedSeverities() {
			return null;
		}

	};

	@Test
	public void testCorrectComposition() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompositionReader r = new CompositionReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(200, 2, 0)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(250, 2, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		r.readEntries(Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class), null, c);

		Assert.assertEquals(450, c.getBuffer().size());
		for (int i = 0; i < 400; i++) {
			final LogEntry e = c.getBuffer().get(i);
			Assert.assertEquals(i, e.getTimeStamp().getTime());
			if (i % 2 == 0) {
				Assert.assertEquals("Error at entry " + i, 1l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log1", e.get(Event.FIELD_LOG_PATH));
			} else if (i % 2 == 1) {
				Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
			}
		}
		for (int i = 400; i < c.getBuffer().size(); i++) {
			final LogEntry e = c.getBuffer().get(i);
			Assert.assertEquals((i - 400) * 2 + 401, e.getTimeStamp().getTime());
			Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
			Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
		}
	}

	@Test
	public void testErrorInOneSource() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompositionReader r = new CompositionReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(200, 2, 0, 100)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(250, 2, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		try {
			r.readEntries(Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class), null, c);
		} catch (final IOException e) {
			Assert.assertTrue(200 - CompositionReader.BUFFER_SIZE_PER_THREAD <= c.getBuffer().size());
			return;
		}
		Assert.fail("Exception expected");
	}

	/**
	 * Composes 45 million entries.
	 * 
	 * @throws FormatException
	 * @throws IOException
	 */
	@Test(timeout = 1000 * 100 * 100)
	@Repeat(20)
	public void testLongComposition() throws FormatException, IOException {
		final long start = System.currentTimeMillis();
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompositionReader r = new CompositionReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(20000000, 2, 0)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(25000000, 2, 1)));
		final AtomicInteger count = new AtomicInteger();
		r.readEntries(Mockito.mock(Log.class), Mockito.mock(LogRawAccess.class), null, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				count.incrementAndGet();
				return true;
			}
		});
		final long end = System.currentTimeMillis() - start;
		logger.info("Read composed {} entries in {}ms, throughput: {} entries/s", count.get(), end,
				Math.round((double) count.get() / (end / 1000)));
		Assert.assertEquals(45000000, count.get());

	}
}
