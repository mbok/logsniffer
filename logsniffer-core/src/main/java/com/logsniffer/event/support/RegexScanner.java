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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.event.EventData;
import com.logsniffer.model.LogEntry;
import com.logsniffer.validators.RegexPatternConstraint;

/**
 * Scanner looking for entries with sequence in {@link LogEntry#getRawContent()}
 * matching the configured regex pattern. The matching groups of the first
 * subsequence are mapped to {@link EventData#getFields()}.
 * 
 * @author mbok
 * 
 */
public class RegexScanner extends SingleEntryIncrementalMatcher {
	@JsonProperty
	@NotEmpty
	@RegexPatternConstraint
	private String pattern;

	@JsonProperty
	private boolean multiLine = true;

	@JsonProperty
	private boolean dotAll = true;

	@JsonProperty
	private boolean caseInsensitive = true;

	@JsonIgnore
	private boolean compiled = false;

	@JsonIgnore
	private Pattern compiledPattern;

	@Override
	public EventData matches(final LogEntry entry) {
		Matcher m = getCompiledPattern().matcher(entry.getRawContent());
		if (m.find()) {
			EventData event = new EventData();
			for (int i = 0; i < m.groupCount(); i++) {
				event.getFields().put(Integer.toString(i + 1), m.group(i + 1));
			}
			return event;
		} else {
			return null;
		}
	}

	/**
	 * @param multiLine
	 *            the multiLine to set
	 */
	public void setMultiLine(final boolean multiLine) {
		this.multiLine = multiLine;
		compiled = false;
	}

	/**
	 * @param dotAll
	 *            the dotAll to set
	 */
	public void setDotAll(final boolean dotAll) {
		this.dotAll = dotAll;
		compiled = false;
	}

	/**
	 * @param caseInsensitive
	 *            the caseInsensitive to set
	 */
	public void setCaseInsensitive(final boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		compiled = false;
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
		compiled = false;
	}

	/**
	 * @return the multiLine
	 */
	public boolean isMultiLine() {
		return multiLine;
	}

	/**
	 * @return the dotAll
	 */
	public boolean isDotAll() {
		return dotAll;
	}

	/**
	 * @return the caseInsensitive
	 */
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	/**
	 * @return the compiledPattern
	 */
	public Pattern getCompiledPattern() {
		if (compiledPattern == null || !compiled) {
			compiledPattern = Pattern.compile(getPattern(),
					(isMultiLine() ? Pattern.MULTILINE : 0)
							| (isDotAll() ? Pattern.DOTALL : 0)
							| (isCaseInsensitive() ? Pattern.CASE_INSENSITIVE
									: 0));
		}
		return compiledPattern;
	}

}
