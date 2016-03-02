package com.logsniffer.model.support;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogPointerFactory.NavigationFuture;
import com.logsniffer.model.Navigation;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.LogEntryReader.LogEntryConsumer;

/**
 * Navigates in a log by log entry timestamps (field
 * {@link LogEntry#FIELD_TIMESTAMP}. This implementation is based on the binary
 * search algorithm with the assumption that the log is ordered by timestamps.
 * 
 * @author mbok
 *
 */
public class TimestampNavigation implements Navigation<Date> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimestampNavigation.class);
	private final ByteLogAccess logAccess;
	private final LogEntryReader<ByteLogAccess> reader;
	private final Log log;

	public TimestampNavigation(final Log log, final ByteLogAccess logAccess,
			final LogEntryReader<ByteLogAccess> reader) {
		this.log = log;
		this.logAccess = logAccess;
		this.reader = reader;
	}

	private LogPointer navigate(final long haystackOffset, long leftBound, long rightBound) throws IOException {
		int i = 0;
		LogEntry lastEntry = null;
		while (leftBound <= rightBound) {
			i++;
			final long mid = leftBound + (rightBound - leftBound) / 2;
			final LogPointer midPointer = logAccess.createRelative(null, mid);
			final LogEntry entry = getEntryNextTo(midPointer);
			if (entry != null) {
				lastEntry = entry;
				final long entryOffset = entry.getTimeStamp().getTime();
				if (entryOffset == haystackOffset) {
					LOGGER.info("Found in {} exactly the desired timestamp {} at position {} after {} iterations", log,
							haystackOffset, entry.getStartOffset(), i);
					return entry.getStartOffset();
				} else if (entryOffset < haystackOffset) {
					leftBound = Math.max(mid + 1, logAccess.getDifference(null, entry.getEndOffset()));
					LOGGER.debug("Adjusting left bound, new bounds: {}-{}", leftBound, rightBound);
				} else {
					rightBound = mid - 1;
					LOGGER.debug("Adjusting right bound, new bounds: {}-{}", leftBound, rightBound);
				}
			} else {
				LOGGER.debug(
						"Failed to determine a log entry with a valid timestamp in {} next to position {}, use this as right bound for the further search",
						log, midPointer);
				rightBound = mid - 1;
			}
		}
		if (lastEntry != null) {
			final long lastEntryOffset = lastEntry.getTimeStamp().getTime();
			if (lastEntryOffset >= haystackOffset) {
				LOGGER.info(
						"Found in {} an entry with timestamp {} next to the desired timestamp {} in {} iterations, returning the start offset: {}",
						log, lastEntryOffset, haystackOffset, i, lastEntry.getStartOffset());
				return lastEntry.getStartOffset();
			} else {
				LOGGER.info(
						"Found in {} an entry with timestamp {} before to desired timestamp {} in {} iterations, returning the end offset: {}",
						log, lastEntry.getTimeStamp(), haystackOffset, i, lastEntry.getEndOffset());
				return lastEntry.getEndOffset();

			}
		} else {
			LOGGER.warn("Failed to find in {} after {} iterations an entry near to {}", log, i, haystackOffset);
		}
		return null;
	}

	private LogEntry getEntryNextTo(final LogPointer p) throws IOException {
		final NextToConsumer c = new NextToConsumer();
		reader.readEntries(log, logAccess, p, c);
		return c.nextToEntry;
	}

	@Override
	public NavigationFuture absolute(final Date offset) throws IOException {
		return new NavigationFuture() {

			@Override
			public LogPointer get() throws IOException {
				return navigate(offset.getTime(), 0, log.getSize());
			}
		};
	}

	private static class NextToConsumer implements LogEntryConsumer {
		private LogEntry nextToEntry;
		private int unformattedCount = 0;

		@Override
		public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
				throws IOException {
			if (entry.getTimeStamp() != null) {
				nextToEntry = entry;
				return false;
			} else {
				unformattedCount++;
			}
			return unformattedCount < 5;
		}

	}
}
