package com.logsniffer.event.filter.support;

import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.event.Event;
import com.logsniffer.event.filter.EventFilter;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry;
import com.logsniffer.reader.FormatException;

/**
 * Maps fields of first entry in {@link Event#getEntries()} directly as fields
 * to {@link Event}.
 * 
 * @author mbok
 *
 */
public class EntriesFieldsDumper implements EventFilter {
	@JsonProperty
	private boolean deleteEntries;

	@JsonProperty
	private boolean excludeRaw = true;

	@Override
	public void filter(final FieldsMap fields) throws FormatException {
		if (fields instanceof Event) {
			final Event event = (Event) fields;
			final List<LogEntry> entries = event.getEntries();
			if (entries != null && entries.size() > 0) {
				final LogEntry entry = entries.get(0);
				event.putAll(entry);
				if (excludeRaw && entry.containsKey(LogEntry.FIELD_RAW_CONTENT)) {
					event.remove(LogEntry.FIELD_RAW_CONTENT);
				}
			}
			if (deleteEntries) {
				event.remove(Event.FIELD_ENTRIES);
			}
		}

	}

	@Override
	public void filterKnownFields(final LinkedHashMap<String, FieldBaseTypes> knownFields) throws FormatException {
		// Not predictable
	}

	/**
	 * @return the deleteEntries
	 */
	public boolean isDeleteEntries() {
		return deleteEntries;
	}

	/**
	 * @param deleteEntries
	 *            the deleteEntries to set
	 */
	public void setDeleteEntries(final boolean deleteEntries) {
		this.deleteEntries = deleteEntries;
	}

	/**
	 * @return the excludeRaw
	 */
	public boolean isExcludeRaw() {
		return excludeRaw;
	}

	/**
	 * @param excludeRaw
	 *            the excludeRaw to set
	 */
	public void setExcludeRaw(final boolean excludeRaw) {
		this.excludeRaw = excludeRaw;
	}

}
