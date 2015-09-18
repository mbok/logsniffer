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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logsniffer.model.fields.FieldsMap;

/**
 * Represents an entry in a log with transfer oriented pointers.
 * 
 * @author mbok
 * 
 */
public abstract class LogEntryData {

	/**
	 * Field key for convenient method {@link #getSeverity()}.
	 */
	public static final String FIELD_SEVERITY_LEVEL = "_severity";

	/**
	 * Field key for convenient method {@link #getTimeStamp()}.
	 */
	public static final String FIELD_TIMESTAMP = "_timestamp";

	/**
	 * Field key for convenient method {@link #getRawContent()}.
	 */
	public static final String FIELD_RAW_CONTENT = "_raw";

	private FieldsMap fields = new FieldsMap();

	/**
	 * 
	 * @return the fields extracted from {@link #getRawContent()}. Values
	 *         {@link Object#toString()} method reflects the unchanged text part
	 *         as extracted from {@link #getRawContent()}.
	 */
	public final FieldsMap getFields() {
		return this.fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public final void setFields(final FieldsMap fields) {
		this.fields = fields;
	}

	/**
	 * @return the content
	 */
	@JsonIgnore
	public String getRawContent() {
		return (String) fields.get(FIELD_RAW_CONTENT);
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setRawContent(final String content) {
		fields.put(FIELD_RAW_CONTENT, content);
	}

	/**
	 * @return the level
	 */
	@JsonIgnore
	public final SeverityLevel getSeverity() {
		return (SeverityLevel) fields.get(FIELD_SEVERITY_LEVEL);
	}

	/**
	 * @param level
	 *            the severity level to set
	 */
	public void setSeverity(final SeverityLevel level) {
		fields.put(FIELD_SEVERITY_LEVEL, level);
	}

	/**
	 * @return the timeStamp
	 */
	@JsonIgnore
	public final Date getTimeStamp() {
		return (Date) fields.get(FIELD_TIMESTAMP);
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(final Date timeStamp) {
		fields.put(FIELD_TIMESTAMP, timeStamp);
	}

	/**
	 * @return the startOffset
	 */
	public abstract LogPointerTransfer getStartOffset();

	/**
	 * @return the endOffset
	 */
	public abstract LogPointerTransfer getEndOffset();
}