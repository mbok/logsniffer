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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.LogEntry;
import com.logsniffer.validators.SimpleDateFormatConstraint;

/**
 * Converts a value in {@link #getSourceField()} into a {@link Date} value
 * regarding the {@link SimpleDateFormat} pattern and stores it in
 * {@link #getTargetField()}.
 *
 * @author mbok
 *
 */
public class TimestampConvertFilter extends AbstractTransformationFilter<Date> {
	private static final Logger logger = LoggerFactory.getLogger(TimestampConvertFilter.class);
	@JsonProperty
	@NotEmpty
	@SimpleDateFormatConstraint
	private String pattern;

	private SimpleDateFormat parsedPattern;

	{
		setTargetField(LogEntry.FIELD_TIMESTAMP);
	}

	@Override
	protected FieldBaseTypes getTargetType() {
		return FieldBaseTypes.DATE;
	}

	@Override
	protected Date transform(final String sourceValue) {
		try {
			if (parsedPattern == null) {
				parsedPattern = new SimpleDateFormat(pattern);
			}
			return parsedPattern.parse(sourceValue);
		} catch (ParseException | IllegalArgumentException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to parse date in format '" + pattern + "' from string: " + sourceValue, e);
			}
		}
		return null;
	}

	@Override
	protected Date getFallback() {
		return null;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(final String pattern) {
		this.pattern = pattern;
		parsedPattern = null;
	}

}
