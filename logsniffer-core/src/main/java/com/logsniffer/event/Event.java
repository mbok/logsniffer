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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.logsniffer.event.Event.EventTypeSafeDeserializer;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry;

/**
 * An event represents a fact of interest detected from one or multiple
 * {@link LogEntry}s.
 * 
 * @author mbok
 * 
 */
@JsonDeserialize(using = EventTypeSafeDeserializer.class)
public class Event extends FieldsMap implements EventAbstract {

	private static final long serialVersionUID = 3694008717847809694L;

	/**
	 * Field key for convenient method {@link #getEntries()}.
	 */
	public static final String FIELD_ENTRIES = "lf_entries";

	/**
	 * Field key for convenient method {@link #getId()}.
	 */
	public static final String FIELD_ID = "_id";

	/**
	 * Field key for convenient method {@link #getSnifferId()}.
	 */
	public static final String FIELD_SNIFFER_ID = "lf_snifferId";

	/**
	 * Field key for convenient method {@link #getLogSourceId()}.
	 */
	public static final String FIELD_SOURCE_ID = "lf_logSourceId";

	/**
	 * Field key for convenient method {@link #getLogPath()}.
	 */
	public static final String FIELD_LOG_PATH = "lf_logPath";

	/**
	 * Field key for convenient method {@link #getTimestamp()}.
	 */
	public static final String FIELD_TIMESTAMP = LogEntry.FIELD_TIMESTAMP;

	/**
	 * Field key for convenient method {@link #getPublished()}.
	 */
	public static final String FIELD_PUBLISHED = "lf_published";

	/**
	 * @return the entries
	 */
	@SuppressWarnings("unchecked")
	public List<LogEntry> getEntries() {
		return (List<LogEntry>) super.get(FIELD_ENTRIES);
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(final List<LogEntry> entries) {
		super.put(FIELD_ENTRIES, entries);
		getAndSetOccurenceFromEntries();
	}

	/**
	 * @return the data
	 */
	@Deprecated
	public FieldsMap getFields() {
		return this;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return (String) super.get(FIELD_ID);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		super.put(FIELD_ID, id);
	}

	/**
	 * @return the snifferId
	 */
	@Override
	public long getSnifferId() {
		return (long) super.get(FIELD_SNIFFER_ID);
	}

	/**
	 * @param snifferId
	 *            the snifferId to set
	 */
	public void setSnifferId(final long snifferId) {
		super.put(FIELD_SNIFFER_ID, snifferId);
	}

	/**
	 * @return the logSourceId
	 */
	@Override
	public long getLogSourceId() {
		return (long) super.get(FIELD_SOURCE_ID);
	}

	/**
	 * @param logSourceId
	 *            the logSourceId to set
	 */
	public void setLogSourceId(final long logSourceId) {
		super.put(FIELD_SOURCE_ID, logSourceId);
	}

	/**
	 * @return the logPath
	 */
	@Override
	public String getLogPath() {
		return (String) super.get(FIELD_LOG_PATH);
	}

	/**
	 * @param logPath
	 *            the logPath to set
	 */
	public void setLogPath(final String logPath) {
		super.put(FIELD_LOG_PATH, logPath);
	}

	/**
	 * @return the published
	 */
	@Override
	public Date getPublished() {
		return (Date) super.get(FIELD_PUBLISHED);
	}

	/**
	 * @param published
	 *            the published to set
	 */
	public void setPublished(final Date published) {
		super.put(FIELD_PUBLISHED, published);
		if (super.get(FIELD_TIMESTAMP) == null) {
			setTimestamp(published);
		}
	}

	/**
	 * Returns the timestamp of the first log entry as occurrence start point
	 * for this event. In case of empty entries the {@link #getPublished()}
	 * timestamp is returned.
	 * 
	 * @return the occurrence start point for this event
	 */
	public Date getTimestamp() {
		final Date occurrence = (Date) super.get(FIELD_TIMESTAMP);
		if (occurrence == null) {
			return getAndSetOccurenceFromEntries();
		}
		return occurrence;
	}

	private Date getAndSetOccurenceFromEntries() {
		Date occurrence = null;
		if (getEntries() != null && getEntries().size() > 0) {
			occurrence = getEntries().get(0).getTimeStamp();
		}
		if (occurrence == null) {
			return getPublished();
		}
		if (occurrence != null) {
			setTimestamp(occurrence);
		}
		return occurrence;
	}

	/**
	 * @param occurrence
	 *            the occurrence to set
	 */
	public void setTimestamp(final Date occurrence) {
		super.put(FIELD_TIMESTAMP, occurrence);
	}

	/**
	 * Type safe deserializer for {@link Event}s.
	 * 
	 * @author mbok
	 *
	 */
	public static class EventTypeSafeDeserializer extends FieldsMapTypeSafeDeserializer {

		@Override
		protected FieldsMap create() {
			return new Event();
		}

	}

}
