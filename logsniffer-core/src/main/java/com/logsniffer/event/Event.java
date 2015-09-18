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

import com.logsniffer.model.LogEntry;

/**
 * An event represents a fact of interest detected from one or multiple
 * {@link LogEntry}s.
 * 
 * @author mbok
 * 
 */
public class Event extends EventData implements EventAbstract {
	private String id;
	private long snifferId;
	private long logSourceId;
	private String logPath;
	private Date published;
	private Date occurrence;

	/**
	 * Default constructor.
	 */
	public Event() {
		super();
	}

	/**
	 * Copies event data.
	 */
	public Event(final EventData ed) {
		super();
		super.setFields(ed.getFields());
		super.setEntries(ed.getEntries());
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @return the snifferId
	 */
	@Override
	public long getSnifferId() {
		return snifferId;
	}

	/**
	 * @param snifferId
	 *            the snifferId to set
	 */
	public void setSnifferId(final long snifferId) {
		this.snifferId = snifferId;
	}

	/**
	 * @return the logSourceId
	 */
	@Override
	public long getLogSourceId() {
		return logSourceId;
	}

	/**
	 * @param logSourceId
	 *            the logSourceId to set
	 */
	public void setLogSourceId(final long logSourceId) {
		this.logSourceId = logSourceId;
	}

	/**
	 * @return the logPath
	 */
	@Override
	public String getLogPath() {
		return logPath;
	}

	/**
	 * @param logPath
	 *            the logPath to set
	 */
	public void setLogPath(final String logPath) {
		this.logPath = logPath;
	}

	/**
	 * @return the published
	 */
	@Override
	public Date getPublished() {
		return published;
	}

	/**
	 * @param published
	 *            the published to set
	 */
	public void setPublished(final Date published) {
		this.published = published;
	}

	/**
	 * Returns the timestamp of the first log entry as occurrence start point
	 * for this event. In case of empty entries the {@link #getPublished()}
	 * timestamp is returned.
	 * 
	 * @return the occurrence start point for this event
	 */
	public Date getOccurrence() {
		if (occurrence == null) {
			if (getEntries() != null && getEntries().size() > 0) {
				occurrence = getEntries().get(0).getTimeStamp();
			}
			if (occurrence == null) {
				return getPublished();
			}
		}
		return occurrence;
	}

	/**
	 * @param occurrence
	 *            the occurrence to set
	 */
	public void setOccurrence(final Date occurrence) {
		this.occurrence = occurrence;
	}

}
