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

import java.util.LinkedHashMap;
import java.util.List;

import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.LogEntryReader;

/**
 * Filter for {@link FieldsMap}.
 * 
 * @author mbok
 * 
 */
public interface FieldsFilter extends ConfiguredBean {
	/**
	 * Filters passed fields.
	 * 
	 * @param fields
	 *            fields to filter
	 */
	void filter(FieldsMap fields);

	/**
	 * Filters known fields.
	 * 
	 * @param knownFields
	 *            fields supported and known by a {@link LogEntryReader}
	 */
	void filterKnownFields(LinkedHashMap<String, FieldBaseTypes> knownFields);

	/**
	 * Filters supported severities.
	 * 
	 * @param severities
	 *            fields supported by a {@link LogEntryReader}.
	 */
	void filterSupportedSeverities(List<SeverityLevel> severities);
}
