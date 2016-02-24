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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.ByteLogAccess;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Implements common backward reader.
 * 
 * @author mbok
 * 
 */
class BackwardReader<ACCESSORTYPE extends ByteLogAccess> {
	private static final Logger logger = LoggerFactory.getLogger(BackwardReader.class);

	private final LogEntryReader<ACCESSORTYPE> forwardReader;

	public BackwardReader(final LogEntryReader<ACCESSORTYPE> forwardReader) {
		super();
		this.forwardReader = forwardReader;
	}

	public List<LogEntry> readEntries(final Log log, final ACCESSORTYPE logAccess, LogPointer startOffset,
			int entriesNumber) throws IOException, FormatException {
		int avgSizePerEntry = 255;
		entriesNumber = -entriesNumber;
		final int origNumber = entriesNumber;
		final ArrayList<LogEntry> revEntries = new ArrayList<LogEntry>();
		LogPointer revPointer = null;
		while (revEntries.size() < origNumber && !startOffset.isSOF() && (revPointer == null || !revPointer.isSOF())) {
			revPointer = logAccess
					.absolute(logAccess.getDifference(null, startOffset) - (entriesNumber + 1) * avgSizePerEntry).get();
			final BufferedConsumer bufferedConsumer = new BufferedConsumer(Integer.MAX_VALUE);
			final BoundedConsumerProxy boundedConsumer = new BoundedConsumerProxy(bufferedConsumer, startOffset);
			forwardReader.readEntries(log, logAccess, revPointer, boundedConsumer);
			final List<LogEntry> entries = bufferedConsumer.getBuffer();
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Found {} entries by reverse read iteration from={} and to={} to fill remaining {} entries with avg-size/entry {}",
						entries.size(), revPointer, startOffset, entriesNumber, avgSizePerEntry);
			}
			if (entries.size() > 0) {
				if (entries.get(0).getStartOffset().isSOF()) {
					// Start reached
					logger.debug("Start reached, cancel reverse reading with {} found entries and {} wanted",
							revEntries.size(), origNumber);
					final int found = Math.min(entries.size(), entriesNumber);
					revEntries.addAll(0, entries.subList(entries.size() - found, entries.size()));
					break;
				} else {
					// Cut off first
					final int found = Math.min(entries.size() - 1, entriesNumber);
					revEntries.addAll(0, entries.subList(entries.size() - found, entries.size()));
					entriesNumber -= found;
				}
			}
			if (entries.size() > 1) {
				// If at least two entries were found in current cycle, then we
				// recalculate the avg size per entry
				startOffset = revEntries.get(0).getStartOffset();
				avgSizePerEntry = (int) (logAccess.getDifference(revEntries.get(0).getStartOffset(),
						revEntries.get(revEntries.size() - 1).getEndOffset()) / revEntries.size()) + 1;
			} else {
				// Empty search, double avg size
				avgSizePerEntry = Math.min(avgSizePerEntry * 2, Integer.MAX_VALUE);
			}
		}
		return revEntries;
	}

}
