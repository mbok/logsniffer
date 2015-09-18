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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.model.support.DefaultLogEntryData;

/**
 * Pure log event data detected by a scanner.
 * 
 * @author mbok
 * 
 */
public class EventData {
	private List<LogEntryData> entries;
	private FieldsMap fields = new FieldsMap();

	/**
	 * @return the entries
	 */
	@JsonDeserialize(contentAs = DefaultLogEntryData.class)
	public List<LogEntryData> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(final List<LogEntryData> entries) {
		this.entries = entries;
	}

	/**
	 * @return the data
	 */
	public FieldsMap getFields() {
		return fields;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setFields(final FieldsMap fields) {
		this.fields = fields;
	}

}
