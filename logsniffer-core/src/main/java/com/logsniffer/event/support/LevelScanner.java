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
package com.logsniffer.event.support;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.event.Event;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.validators.NotDefaultPrimitiveValue;

/**
 * Scanns for log entries matching the level of interest.
 * 
 * @author mbok
 * 
 */
public class LevelScanner extends SingleEntryIncrementalMatcher {

	/**
	 * Level comparator type.
	 * 
	 * @author mbok
	 * 
	 */
	public enum LevelComparatorType {
		EQ, EQ_OR_GREATER;

		public boolean matches(final int severityOrdinalNumber, final SeverityLevel check) {
			if (this == EQ) {
				return severityOrdinalNumber == check.getOrdinalNumber();
			} else {
				return check.getOrdinalNumber() >= severityOrdinalNumber;
			}
		}
	}

	@JsonProperty
	@JsonInclude(Include.NON_DEFAULT)
	@NotDefaultPrimitiveValue
	private int severityNumber;

	@NotNull
	@JsonProperty
	private LevelComparatorType comparator = LevelComparatorType.EQ_OR_GREATER;

	@Override
	public Event matches(final LogEntry entry) {
		if (entry.getSeverity() != null && comparator.matches(severityNumber, entry.getSeverity())) {
			final Event event = new Event();
			return event;
		}
		return null;
	}

	/**
	 * @return the comparator
	 */
	public LevelComparatorType getComparator() {
		return comparator;
	}

	/**
	 * @param comparator
	 *            the comparator to set
	 */
	public void setComparator(final LevelComparatorType comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the severityNumber
	 */
	public int getSeverityNumber() {
		return severityNumber;
	}

	/**
	 * @param severityNumber
	 *            the severityNumber to set
	 */
	public void setSeverityNumber(final int severityNumber) {
		this.severityNumber = severityNumber;
	}

}
