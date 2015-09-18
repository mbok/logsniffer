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

import javax.validation.constraints.Min;

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
 * Reader strategy that reads at minimum the configured amount of bytes
 * {@link #getMinBytesAmount()} (100k by default) from log during the scan
 * process.
 * 
 * @author mbok
 * 
 */
public class MinBAmountReadStrategy implements LogEntryReaderStrategy {
	private static Logger logger = LoggerFactory
			.getLogger(MinBAmountReadStrategy.class);
	@JsonIgnore
	private LogPointer startedAt;

	/**
	 * 100k by default
	 */
	@JsonProperty
	@Min(value = 4096)
	private long minBytesAmount = 1024 * 100;

	public MinBAmountReadStrategy() {
		super();
	}

	public MinBAmountReadStrategy(final long minBytesAmouunt) {
		super();
		this.minBytesAmount = minBytesAmouunt;
	}

	@Override
	public void reset(final Log log, final LogPointerFactory pointerFactory,
			final LogPointer start) {
		this.startedAt = start;
	}

	@Override
	public boolean continueReading(final Log log,
			final LogPointerFactory pointerFactory,
			final LogEntry currentReadEntry) throws IOException {
		long read = pointerFactory.getDifference(startedAt,
				currentReadEntry.getEndOffset());
		if (read < getMinBytesAmount()) {
			return true;
		} else {
			logger.debug(
					"Interrupt further scanning due to already read the destined min bytes amount: {}",
					read);
			return false;
		}
	}

	/**
	 * @return the minBytesAmouunt
	 */
	public long getMinBytesAmount() {
		return minBytesAmount;
	}

	/**
	 * @param minBytesAmouunt
	 *            the minBytesAmouunt to set
	 */
	public void setMinBytesAmount(final long minBytesAmouunt) {
		this.minBytesAmount = minBytesAmouunt;
	}

}
