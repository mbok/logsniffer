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

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry.LogEntryTypeSafeDeserializer;

/**
 * Represents an entry in a log with native pointers.
 * 
 * @author mbok
 * 
 */
@JsonDeserialize(using = LogEntryTypeSafeDeserializer.class)
public final class LogEntry extends FieldsMap {

	private static final long serialVersionUID = 6930083682998388113L;

	/**
	 * Field key for convenient method {@link #getStartOffset()}.
	 */
	public static final String FIELD_START_OFFSET = "lf_startOffset";

	/**
	 * Field key for convenient method {@link #getEndOffset()}.
	 */
	public static final String FIELD_END_OFFSET = "lf_endOffset";

	/**
	 * Field key for convenient method {@link #getSeverity()}.
	 */
	public static final String FIELD_SEVERITY_LEVEL = "lf_severity";

	/**
	 * Field key for convenient method {@link #getTimeStamp()}.
	 */
	public static final String FIELD_TIMESTAMP = "lf_timestamp";

	/**
	 * Field key for convenient method {@link #getRawContent()}.
	 */
	public static final String FIELD_RAW_CONTENT = "lf_raw";

	// @JsonSerialize(typing = Typing.STATIC)
	// @JsonDeserialize(as = JsonLogPointer.class)
	// private LogPointer startOffset;
	// @JsonSerialize(typing = Typing.STATIC)
	// @JsonDeserialize(as = JsonLogPointer.class)
	// private LogPointer endOffset;

	/**
	 * @return the startOffset
	 */
	public LogPointer getStartOffset() {
		return (LogPointer) super.get(FIELD_START_OFFSET);
	}

	/**
	 * @param startOffset
	 *            the startOffset to set
	 */
	public void setStartOffset(final LogPointer startOffset) {
		super.put(FIELD_START_OFFSET, startOffset);
	}

	/**
	 * @return the endOffset
	 */
	public LogPointer getEndOffset() {
		return (LogPointer) super.get(FIELD_END_OFFSET);
	}

	/**
	 * @param endOffset
	 *            the endOffset to set
	 */
	public void setEndOffset(final LogPointer endOffset) {
		super.put(FIELD_END_OFFSET, endOffset);
	}

	/**
	 * 
	 * @return the fields extracted from {@link #getRawContent()}. Values
	 *         {@link Object#toString()} method reflects the unchanged text part
	 *         as extracted from {@link #getRawContent()}.
	 */
	@Deprecated
	public final FieldsMap getFields() {
		return this;
	}

	/**
	 * @return the content
	 */
	public String getRawContent() {
		return (String) super.get(FIELD_RAW_CONTENT);
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setRawContent(final String content) {
		super.put(FIELD_RAW_CONTENT, content);
	}

	/**
	 * @return the level
	 */
	public final SeverityLevel getSeverity() {
		return (SeverityLevel) super.get(FIELD_SEVERITY_LEVEL);
	}

	/**
	 * @param level
	 *            the severity level to set
	 */
	public void setSeverity(final SeverityLevel level) {
		super.put(FIELD_SEVERITY_LEVEL, level);
	}

	/**
	 * @return the timeStamp
	 */
	public final Date getTimeStamp() {
		return (Date) super.get(FIELD_TIMESTAMP);
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(final Date timeStamp) {
		super.put(FIELD_TIMESTAMP, timeStamp);
	}

	/**
	 * Type safe deserializer for {@link LogEntry}s.
	 * 
	 * @author mbok
	 *
	 */
	public static class LogEntryTypeSafeDeserializer extends FieldsMapTypeSafeDeserializer {

		@Override
		protected FieldsMap create() {
			return new LogEntry();
		}

	}
}
