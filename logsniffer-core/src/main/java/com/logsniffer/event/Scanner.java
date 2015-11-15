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
package com.logsniffer.event;

import java.io.IOException;

import com.logsniffer.config.ConfigException;
import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.config.WrappedBean;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Incremental event scanner.
 * 
 * @author mbok
 * 
 */
public interface Scanner extends ConfiguredBean {
	/**
	 * Consumes events to allow pipeline processing.
	 * 
	 * @author mbok
	 * 
	 */
	public static interface EventConsumer {
		void consume(Event eventData) throws IOException;
	}

	/**
	 * Incremental routine to search for next log events. The routine should
	 * store in incrementData values to continue the search process in the next
	 * time it's called without the need to start from log's start.
	 * 
	 * @param reader
	 *            reader for the log
	 * @param readerStrategy
	 *            Strategy for log reader describing how long the scanner should
	 *            read from log.
	 * @param log
	 *            the log access to search for events in
	 * @param incrementData
	 *            the persistent data between multiple calls to support
	 *            incremental log scanning for event.
	 * @param eventConsumer
	 *            event consumer
	 */
	public void find(LogEntryReader<LogInputStream> reader, LogEntryReaderStrategy readerStrategy, Log log,
			LogRawAccess<LogInputStream> logAccess, IncrementData incrementData, EventConsumer eventConsumer)
					throws IOException, FormatException;

	/**
	 * Wrapper for delegated strategy e.g. to allow lazy initiation.
	 * 
	 * @author mbok
	 */
	public static abstract class LogEntryReaderStrategyWrapper
			implements LogEntryReaderStrategy, WrappedBean<LogEntryReaderStrategy> {
		private LogEntryReaderStrategy wrapped;

		public static final LogEntryReaderStrategy unwrap(final LogEntryReaderStrategy possiblyWrapped) {
			if (possiblyWrapped instanceof LogEntryReaderStrategyWrapper) {
				return ((LogEntryReaderStrategyWrapper) possiblyWrapped).getWrappedStrategy();
			}
			return possiblyWrapped;
		}

		public final LogEntryReaderStrategy getWrappedStrategy() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void reset(final Log log, final LogPointerFactory pointerFactory, final LogPointer start)
				throws IOException {
			getWrappedStrategy().reset(log, pointerFactory, start);
		}

		@Override
		public boolean continueReading(final Log log, final LogPointerFactory pointerFactory,
				final LogEntry currentReadEntry) throws IOException {
			return getWrappedStrategy().continueReading(log, pointerFactory, currentReadEntry);
		}

	}

	/**
	 * Wrapper for delegated log scanner e.g. to allow lazy initiation.
	 * 
	 * @author mbok
	 */
	public static abstract class ScannerWrapper implements Scanner, WrappedBean<Scanner> {
		private Scanner wrapped;

		public static final Scanner unwrap(final Scanner possiblyWrapped) {
			if (possiblyWrapped instanceof ScannerWrapper) {
				return ((ScannerWrapper) possiblyWrapped).getWrappedScanner();
			}
			return possiblyWrapped;
		}

		public final Scanner getWrappedScanner() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void find(final LogEntryReader<LogInputStream> reader, final LogEntryReaderStrategy readerStrategy,
				final Log log, final LogRawAccess<LogInputStream> logAccess, final IncrementData incrementData,
				final EventConsumer eventConsumer) throws IOException, FormatException {
			try {
				getWrappedScanner().find(reader, readerStrategy, log, logAccess, incrementData, eventConsumer);
			} catch (final ConfigException e) {
				throw new IOException("Failed to create configured scanner", e);
			}
		}

	}
}
