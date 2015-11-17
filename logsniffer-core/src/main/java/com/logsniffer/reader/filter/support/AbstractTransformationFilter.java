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

import java.util.LinkedHashMap;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.filter.FieldsFilter;

/**
 * Abstract filter class concerning transformation of a source field value into
 * a target field.
 *
 *
 * @author mbok
 *
 */
public abstract class AbstractTransformationFilter<T> implements FieldsFilter {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTransformationFilter.class);
	@JsonProperty
	@NotEmpty
	private String targetField;

	@JsonProperty
	@NotEmpty
	private String sourceField;

	@JsonProperty
	private boolean override = true;

	/**
	 * @return the targetField
	 */
	public String getTargetField() {
		return targetField;
	}

	/**
	 * @param targetField
	 *            the targetField to set
	 */
	public void setTargetField(final String targetField) {
		this.targetField = targetField;
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
	 *
	 * @return the type of the transformed target
	 */
	protected abstract FieldBaseTypes getTargetType();

	/**
	 * Transform the string value to the target representation.
	 *
	 * @param sourceValue
	 *            the not empty source value
	 * @return the transformed value or null if transformation fails
	 */
	protected abstract T transform(String sourceValue);

	/**
	 * Returns the fallback value if transformation fails or the source value is
	 * empty or null.
	 *
	 * @return fallback value if transformation fails or the source value is
	 *         empty or null. The fallback can also be null to skip filtering
	 *         totally..
	 */
	protected abstract T getFallback();

	@Override
	public final void filter(final FieldsMap fields) {
		if (override || !fields.containsKey(getTargetField())) {
			final Object sourceStr = fields.get(getSourceField());
			if (sourceStr != null) {
				try {
					final T transformed = transform(sourceStr.toString());
					if (transformed != null) {
						fields.put(getTargetField(), transformed);
						return;
					}
				} catch (final Exception e) {
					logger.debug("Failed to transform value", e);
					// Fallback
				}
			}
			final T fallback = getFallback();
			if (override || fallback != null) {
				fields.put(getTargetField(), fallback);
			}
		}
	}

	@Override
	public final void filterKnownFields(final LinkedHashMap<String, FieldBaseTypes> knownFields) {
		knownFields.put(targetField, getTargetType());
	}

	@Override
	public void filterSupportedSeverities(final List<SeverityLevel> severities) {
		// Nothing todo
	}

}
