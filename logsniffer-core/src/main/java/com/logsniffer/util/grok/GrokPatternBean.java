package com.logsniffer.util.grok;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean for configuring metadata belonging to a Grok pattern presentation.
 * 
 * @author mbok
 *
 */
public final class GrokPatternBean {
	@JsonProperty
	@NotEmpty
	@GrokPatternConstraint
	private String pattern;

	@JsonProperty
	private boolean multiLine = true;

	@JsonProperty
	private boolean dotAll = true;

	@JsonProperty
	private boolean caseInsensitive = true;

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
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the multiLine
	 */
	public boolean isMultiLine() {
		return multiLine;
	}

	/**
	 * @param multiLine
	 *            the multiLine to set
	 */
	public void setMultiLine(boolean multiLine) {
		this.multiLine = multiLine;
	}

	/**
	 * @return the dotAll
	 */
	public boolean isDotAll() {
		return dotAll;
	}

	/**
	 * @param dotAll
	 *            the dotAll to set
	 */
	public void setDotAll(boolean dotAll) {
		this.dotAll = dotAll;
	}

	/**
	 * @return the caseInsensitive
	 */
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	/**
	 * @param caseInsensitive
	 *            the caseInsensitive to set
	 */
	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	@Override
	public String toString() {
		return "GrokPatternBean [pattern=" + pattern + ", multiLine=" + multiLine + ", dotAll=" + dotAll
				+ ", caseInsensitive=" + caseInsensitive + "]";
	}

}
