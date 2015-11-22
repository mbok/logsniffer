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
package com.logsniffer.reader;

import java.io.IOException;
import java.util.List;

import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.fields.FieldsHost;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;

/**
 * Format dependent log reader. Reading is performed pipeline like.
 * 
 * @author mbok
 * 
 */
public interface LogEntryReader<STREAMTYPE extends LogInputStream> extends ConfiguredBean, FieldsHost {

	/**
	 * Consumer for log entries, called sequentially when a new entry was read.
	 * 
	 * @author mbok
	 * 
	 */
	public static interface LogEntryConsumer {
		/**
		 * Called to consume the new read log entry.
		 * 
		 * @param log
		 *            the log
		 * @param pointerFactory
		 *            the pointer factory
		 * @param entry
		 *            the read entry
		 * @return return true to continue reading (if EOF isn't reached) or
		 *         false to interrupt further reading.
		 * @throws IOException
		 *             in case of any errors
		 */
		boolean consume(Log log, LogPointerFactory pointerFactory, LogEntry entry) throws IOException;
	}

	/**
	 * Reads non-blocking the log entries beginning with the byte offset in log.
	 * The read entries will be propagated sequentially to the given consumer.
	 * The method returns back when {@link LogEntryConsumer#consume(LogEntry)}
	 * returns false or the boundary is reached.
	 * 
	 * @param log
	 *            the log to read
	 * @param logAccess
	 *            the access to the log to read from
	 * @param startOffset
	 *            the offset pointer in the log to start reading on. A null
	 *            value means start from beginning.
	 * 
	 * @param boundary
	 *            if not null then reading will never consume an entry beginning
	 *            after the boundary
	 * @param entriesNumber
	 *            number of entries to read
	 * @return the read entries
	 */
	public void readEntries(Log log, LogRawAccess<STREAMTYPE> logAccess, LogPointer startOffset,
			LogEntryConsumer consumer) throws IOException, FormatException;

	/**
	 * 
	 * @return list of supported and provided severity levels.
	 */
	public List<SeverityLevel> getSupportedSeverities();

	/**
	 * Wrapper for delegated log entry reader e.g. to allow lazy initiating of
	 * readers.
	 * 
	 * @author mbok
	 * 
	 * @param <ContentType>
	 *            the entry type
	 */
	public static abstract class LogEntryReaderWrapper implements LogEntryReader<LogInputStream> {
		private LogEntryReader<LogInputStream> wrapped;

		protected abstract LogEntryReader<LogInputStream> getWrapped() throws IOException, FormatException;

		private LogEntryReader<LogInputStream> getReader() throws IOException, FormatException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void readEntries(final Log log, final LogRawAccess<LogInputStream> logAccess,
				final LogPointer startOffset, final LogEntryConsumer consumer) throws IOException, FormatException {
			getReader().readEntries(log, logAccess, startOffset, consumer);
		}

		@Override
		public List<SeverityLevel> getSupportedSeverities() {
			try {
				return getReader().getSupportedSeverities();
			} catch (final Exception e) {
				throw new RuntimeException("Unexpected", e);
			}
		}

	}
}
