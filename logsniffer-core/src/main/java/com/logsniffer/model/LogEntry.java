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
package com.logsniffer.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Typing;
import com.logsniffer.model.support.JsonLogPointer;

/**
 * Represents an entry in a log with native pointers.
 * 
 * @author mbok
 * 
 */
public final class LogEntry extends LogEntryData {
	@JsonSerialize(typing = Typing.STATIC)
	@JsonDeserialize(as = JsonLogPointer.class)
	private LogPointer startOffset;
	@JsonSerialize(typing = Typing.STATIC)
	@JsonDeserialize(as = JsonLogPointer.class)
	private LogPointer endOffset;

	/**
	 * @return the startOffset
	 */
	@Override
	public LogPointer getStartOffset() {
		return startOffset;
	}

	/**
	 * @param startOffset
	 *            the startOffset to set
	 */
	public void setStartOffset(final LogPointer startOffset) {
		this.startOffset = startOffset;
	}

	/**
	 * @return the endOffset
	 */
	@Override
	public LogPointer getEndOffset() {
		return endOffset;
	}

	/**
	 * @param endOffset
	 *            the endOffset to set
	 */
	public void setEndOffset(final LogPointer endOffset) {
		this.endOffset = endOffset;
	}
}
