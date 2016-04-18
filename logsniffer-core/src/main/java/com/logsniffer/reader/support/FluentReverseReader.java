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
package com.logsniffer.reader.support;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.support.ByteLogAccess;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Consumes log entries from a byte log access in a reverse order direction
 * fluently.
 * 
 * @author mbok
 * 
 */
public class FluentReverseReader<ACCESSORTYPE extends ByteLogAccess> implements LogEntryReader<ACCESSORTYPE> {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LogEntryReader<ACCESSORTYPE> forwardReader;

	public FluentReverseReader(final LogEntryReader<ACCESSORTYPE> forwardReader) {
		super();
		this.forwardReader = forwardReader;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void readEntries(final Log log, final ACCESSORTYPE logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException, FormatException {
		logger.debug("Starting fluent backward consumption in {} from: {}", log, startOffset);
		List<LogEntry> entries = new BackwardReader(forwardReader).readEntries(log, logAccess, startOffset, -101);
		logger.debug("Starting fluent backward consumption with first block of {} entries", entries.size());
		while (entries.size() > 0) {
			// Record the pointer to continue from in next iteration due to the
			// log entry can be changed after consumption
			final LogPointer toContinueFrom = entries.get(0).getStartOffset();
			for (int i = entries.size() - 1; i >= 0; i--) {
				if (!consumer.consume(log, logAccess, entries.get(i))) {
					logger.debug("Cancelled fluent backward consumption");
					return;
				}
			}
			entries = new BackwardReader(forwardReader).readEntries(log, logAccess, toContinueFrom, -101);
			logger.debug("Continue fluent backward consumption with next block of {} entries", entries.size());
		}
		logger.debug("Finished fluent backward consumption because of SOF");

	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return forwardReader.getSupportedSeverities();
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return forwardReader.getFieldTypes();
	}

	@Override
	public void readEntriesReverse(final Log log, final ACCESSORTYPE logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		// Reverse
		forwardReader.readEntries(log, logAccess, startOffset, consumer);
	}

}
