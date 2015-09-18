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
package com.logsniffer.reader.filter.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.filter.FieldsFilter;

/**
 * Maps a source string field to a severity object.
 * 
 * @author mbok
 * 
 */
public final class SeverityMappingFilter implements FieldsFilter {
	@JsonProperty
	@Valid
	private Map<String, SeverityLevel> severityLevels = new HashMap<>();

	private Map<String, SeverityLevel> _levels = null;

	@JsonProperty
	private boolean override = true;

	@JsonProperty
	private boolean ignoreCase = true;

	@JsonProperty
	@NotEmpty
	private String sourceField;

	@JsonProperty
	@Valid
	private SeverityLevel fallback;

	@Override
	public void filter(final FieldsMap fields) {
		if (_levels == null) {
			resetInternalLevels();
		}
		if (override || !fields.containsKey(LogEntryData.FIELD_SEVERITY_LEVEL)) {
			Object source = fields.get(sourceField);
			if (source != null) {
				String strSource = source.toString().trim();
				if (ignoreCase) {
					strSource = strSource.toLowerCase();
				}
				SeverityLevel mapping = _levels.get(strSource);
				if (mapping != null) {
					fields.put(LogEntryData.FIELD_SEVERITY_LEVEL, mapping);
				} else if (fallback != null) {
					fields.put(LogEntryData.FIELD_SEVERITY_LEVEL, fallback);
				}
			}
		}
	}

	private void resetInternalLevels() {
		_levels = new HashMap<>();
		if (severityLevels != null) {
			if (ignoreCase) {
				for (String s : severityLevels.keySet()) {
					_levels.put(s.toLowerCase().trim(), severityLevels.get(s));
				}
			} else {
				for (String s : severityLevels.keySet()) {
					_levels.put(s.trim(), severityLevels.get(s));
				}
			}
		}
	}

	/**
	 * @return the severityLevels
	 */
	public Map<String, SeverityLevel> getSeverityLevels() {
		return severityLevels;
	}

	/**
	 * @param severityLevels
	 *            the severityLevels to set
	 */
	public void setSeverityLevels(
			final Map<String, SeverityLevel> severityLevels) {
		this.severityLevels = severityLevels;
		resetInternalLevels();
	}

	/**
	 * @return the override
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * @param override
	 *            the override to set
	 */
	public void setOverride(final boolean override) {
		this.override = override;
	}

	/**
	 * @return the sourceField
	 */
	public String getSourceField() {
		return sourceField;
	}

	/**
	 * @param sourceField
	 *            the sourceField to set
	 */
	public void setSourceField(final String sourceField) {
		this.sourceField = sourceField;
	}

	/**
	 * @return the fallback
	 */
	public SeverityLevel getFallback() {
		return fallback;
	}

	/**
	 * @param fallback
	 *            the fallback to set
	 */
	public void setFallback(final SeverityLevel fallback) {
		this.fallback = fallback;
	}

	/**
	 * @return the ignoreCase
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * @param ignoreCase
	 *            the ignoreCase to set
	 */
	public void setIgnoreCase(final boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		resetInternalLevels();
	}

	@Override
	public void filterKnownFields(
			final LinkedHashMap<String, FieldBaseTypes> knownFields) {
		if (!knownFields.containsKey(LogEntryData.FIELD_SEVERITY_LEVEL)) {
			knownFields.put(LogEntryData.FIELD_SEVERITY_LEVEL,
					FieldBaseTypes.SEVERITY);
		}
	}

	@Override
	public void filterSupportedSeverities(final List<SeverityLevel> severities) {
		severities.addAll(new HashSet<>(severityLevels.values()));
		if (fallback != null) {
			severities.add(fallback);
		}
	}

}
