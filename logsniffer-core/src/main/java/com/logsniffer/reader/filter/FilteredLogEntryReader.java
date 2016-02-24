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
package com.logsniffer.reader.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsHost;
import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.util.json.Views;

/**
 * Proxy {@link LogEntryReader} with filtering support.
 * 
 * @author mbok
 * 
 */
public final class FilteredLogEntryReader<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>>
		implements LogEntryReader<ACCESSTYPE> {
	@JsonProperty
	@Valid
	private List<FieldsFilter> filters = new ArrayList<>();

	@JsonProperty
	@Valid
	private LogEntryReader<ACCESSTYPE> targetReader;

	/**
	 * 
	 */
	public FilteredLogEntryReader() {
		super();
	}

	/**
	 * Returns a {@link FilteredLogEntryReader} wrapping the given reader in
	 * case filters are defined. In case filters are null or empty the current
	 * reader is returned back without wrapping.
	 * 
	 * @param targetReader
	 *            the target reader to wrapp for filtering
	 * @param filters
	 *            filters maybe null or empty
	 * @return a {@link FilteredLogEntryReader} wrapping the given reader in
	 *         case filters are defined. In case filters are null or empty the
	 *         current reader is returned back without wrapping.
	 */
	public static <ACCESSTYPE extends LogRawAccess<? extends LogInputStream>> LogEntryReader<ACCESSTYPE> wrappIfNeeded(
			final LogEntryReader<ACCESSTYPE> targetReader, final List<FieldsFilter> filters) {
		if (filters != null && !filters.isEmpty()) {
			return new FilteredLogEntryReader<>(targetReader, filters);
		}
		return targetReader;
	}

	/**
	 * @param targetReader
	 * @param filters
	 */
	@SuppressWarnings("unchecked")
	public FilteredLogEntryReader(final LogEntryReader<ACCESSTYPE> targetReader, final List<FieldsFilter> filters) {
		super();
		this.targetReader = targetReader;
		this.filters = (List<FieldsFilter>) (filters != null ? new ArrayList<>(filters) : new ArrayList<>());
	}

	/**
	 * @return the targetReader
	 */
	public LogEntryReader<ACCESSTYPE> getTargetReader() {
		return targetReader;
	}

	/**
	 * @return the filters
	 */
	public List<FieldsFilter> getFilters() {
		return filters;
	}

	@Override
	public void readEntries(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException, FormatException {
		targetReader.readEntries(log, logAccess, startOffset, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				filterLogEntry(entry);
				return consumer.consume(log, pointerFactory, entry);
			}
		});

	}

	private void filterLogEntry(final LogEntry entry) throws FormatException {
		for (final FieldsFilter f : filters) {
			f.filter(entry);
		}
	}

	@Override
	@JsonView(Views.Info.class)
	public List<SeverityLevel> getSupportedSeverities() {
		List<SeverityLevel> severities;
		if (targetReader != null) {
			severities = new ArrayList<>(targetReader.getSupportedSeverities());
		} else {
			severities = new ArrayList<>();
		}
		if (filters != null) {
			for (final FieldsFilter f : filters) {
				if (f instanceof LogEntryFilter) {
					((LogEntryFilter) f).filterSupportedSeverities(severities);
				}
			}
			Collections.sort(severities);
		}
		return severities;
	}

	@Override
	@JsonView(Views.Info.class)
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return FieldsHost.FieldHostUtils.getFilteredFieldTypes(targetReader, filters);
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(final List<FieldsFilter> filters) {
		this.filters = filters != null ? new ArrayList<>(filters) : new ArrayList<FieldsFilter>();
	}

	/**
	 * @param targetReader
	 *            the targetReader to set
	 */
	public void setTargetReader(final LogEntryReader<ACCESSTYPE> targetReader) {
		this.targetReader = targetReader;
	}

	@Override
	public void readEntriesReverse(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		targetReader.readEntriesReverse(log, logAccess, startOffset, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				filterLogEntry(entry);
				return consumer.consume(log, pointerFactory, entry);
			}
		});
	}

}
