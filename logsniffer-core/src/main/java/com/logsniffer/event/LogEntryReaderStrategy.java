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

import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.event.Scanner.EventConsumer;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.reader.LogEntryReader;

/**
 * Strategy for log reader describing how long the scanner should read from log.
 * 
 * @author mbok
 * 
 */
public interface LogEntryReaderStrategy extends ConfiguredBean {
	/**
	 * Called each time
	 * {@link Scanner#find(LogEntryReader, LogEntryReaderStrategy, Log, IncrementData, EventConsumer)}
	 * starts scanning to reset values from previous scan run.
	 * 
	 * @param log
	 *            the log
	 * @param pointerFactory
	 *            pointer factory
	 * @param start
	 *            the start position in log when the reader starts reading,
	 *            possibly null
	 */
	public void reset(Log log, LogPointerFactory pointerFactory,
			LogPointer start) throws IOException;

	/**
	 * Returns true to indicate the scanner to continue with scanning. If false
	 * is returned the scanner stops the current scanning step.
	 * 
	 * @param log
	 *            the log
	 * @param pointerFactory
	 *            pointer factory
	 * @param entry
	 *            the last read entry
	 * @return true to indicate the scanner to continue with scanning and false
	 *         to stop.
	 */
	public boolean continueReading(Log log, LogPointerFactory pointerFactory,
			LogEntry currentReadEntry) throws IOException;
}
