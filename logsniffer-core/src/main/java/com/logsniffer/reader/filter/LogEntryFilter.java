package com.logsniffer.reader.filter;

import java.util.List;

import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.LogEntryReader;

/**
 * Additional filter capabilities for {@link LogEntry}s.
 * 
 * @author mbok
 *
 */
public interface LogEntryFilter extends FieldsFilter {
	/**
	 * Filters supported severities.
	 * 
	 * @param severities
	 *            fields supported by a {@link LogEntryReader}.
	 */
	void filterSupportedSeverities(List<SeverityLevel> severities);
}
