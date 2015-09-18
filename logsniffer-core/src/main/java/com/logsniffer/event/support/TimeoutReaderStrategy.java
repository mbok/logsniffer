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
package com.logsniffer.event.support;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.event.LogEntryReaderStrategy;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;

/**
 * Continue reading log within a specified timeout.
 * 
 * @author mbok
 * 
 */
public class TimeoutReaderStrategy implements LogEntryReaderStrategy {
	private static Logger logger = LoggerFactory
			.getLogger(TimeoutReaderStrategy.class);
	@JsonIgnore
	private long startedAt = -1;

	@JsonProperty
	private long timeout;

	public TimeoutReaderStrategy() {
		super();
	}

	public TimeoutReaderStrategy(final long timeout) {
		this();
		this.timeout = timeout;
	}

	@Override
	public void reset(final Log log, final LogPointerFactory pointerFactory,
			final LogPointer start) throws IOException {
		startedAt = System.currentTimeMillis();
	}

	@Override
	public boolean continueReading(final Log log,
			final LogPointerFactory pointerFactory,
			final LogEntry currentReadEntry) throws IOException {
		if (startedAt < 0) {
			startedAt = System.currentTimeMillis();
		}
		long taken = System.currentTimeMillis() - startedAt;
		if (taken >= timeout) {
			logger.debug(
					"Stop reading {} after {}ms because reached timeout of {}ms",
					log, taken, timeout);
			return false;
		} else {
			return true;
		}
	}
}
