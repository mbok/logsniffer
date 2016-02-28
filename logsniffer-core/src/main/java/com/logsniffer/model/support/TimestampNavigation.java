package com.logsniffer.model.support;

import java.io.IOException;
import java.util.Date;

import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory.NavigationFuture;
import com.logsniffer.model.Navigation;
import com.logsniffer.reader.LogEntryReader;

/**
 * Navigates in a log by log entry timestamps (field
 * {@link LogEntry#FIELD_TIMESTAMP}. This implementation is based on the binary
 * search algorithm with the assumption that the log is ordered by timestamps.
 * 
 * @author mbok
 *
 */
public class TimestampNavigation implements Navigation<Date> {

	private final ByteLogAccess logAccess;
	private final LogEntryReader<ByteLogAccess> reader;

	public TimestampNavigation(final ByteLogAccess logAccess, final LogEntryReader<ByteLogAccess> reader) {
		this.logAccess = logAccess;
		this.reader = reader;
	}

	private LogPointer navigate(final Date offset) throws IOException {
		// TODO implement
		return null;
	}

	@Override
	public NavigationFuture absolute(final Date offset) throws IOException {
		return new NavigationFuture() {

			@Override
			public LogPointer get() throws IOException {
				return navigate(offset);
			}
		};
	}

}
