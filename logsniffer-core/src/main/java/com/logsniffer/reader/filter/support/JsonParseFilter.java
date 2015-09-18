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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.filter.FieldsFilter;
import com.logsniffer.validators.JsonStringConastraint;

/**
 * Parses the string value of a source field and converts it to an (JSON) object
 * field.
 * 
 * @author mbok
 * 
 */
@PostConstructed(constructor = JsonParseFilter.JsonParseFilterBuilder.class)
public final class JsonParseFilter implements FieldsFilter {
	private static final Logger logger = LoggerFactory
			.getLogger(JsonParseFilter.class);
	private ObjectMapper objectMapper;

	@Component
	public static final class JsonParseFilterBuilder implements
			BeanPostConstructor<JsonParseFilter> {
		@Autowired
		private ObjectMapper objectMapper;

		@Override
		public void postConstruct(final JsonParseFilter bean,
				final BeanConfigFactoryManager configManager)
				throws ConfigException {
			bean.objectMapper = objectMapper;
			bean.parseFallback();
		}

	}

	@JsonProperty
	@NotEmpty
	private String sourceField;

	@JsonProperty
	@NotEmpty
	private String targetField;

	@JsonProperty
	@JsonStringConastraint
	private String fallbackJsonValue;

	private Object fallbackJsonObject;

	@Override
	public void filter(final FieldsMap fields) {
		Object sourceStr = fields.get(sourceField);
		if (sourceStr instanceof String
				&& StringUtils.isNotEmpty((String) sourceStr)) {
			try {
				Object jsonObj = objectMapper.readValue((String) sourceStr,
						Object.class);
				fields.put(targetField, jsonObj);
				return;
			} catch (IOException e) {
				// Fallback
			}
		}
		if (fallbackJsonObject != null) {
			fields.put(targetField, fallbackJsonObject);
		}
	}

	@Override
	public void filterKnownFields(
			final LinkedHashMap<String, FieldBaseTypes> knownFields) {
		knownFields.put(targetField, FieldBaseTypes.OBJECT);
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
	 * @return the fallbackJsonValue
	 */
	public String getFallbackJsonValue() {
		return fallbackJsonValue;
	}

	/**
	 * @param fallbackJsonValue
	 *            the fallbackJsonValue to set
	 */
	public void setFallbackJsonValue(final String fallbackJsonValue) {
		this.fallbackJsonValue = fallbackJsonValue;
		parseFallback();
	}

	private void parseFallback() {
		if (objectMapper != null) {
			if (StringUtils.isNotEmpty(fallbackJsonValue)) {
				try {
					fallbackJsonObject = objectMapper.readValue(
							fallbackJsonValue, Object.class);
				} catch (IOException e) {
					logger.warn("Failed to deserialize fallback JSON string: "
							+ fallbackJsonValue, e);
				}
			} else {
				fallbackJsonObject = null;
			}
		}
	}

	@Override
	public void filterSupportedSeverities(final List<SeverityLevel> severities) {
		// NOP
	}
}
