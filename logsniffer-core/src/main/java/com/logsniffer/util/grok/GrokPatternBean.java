package com.logsniffer.util.grok;

import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.reader.FormatException;

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
	private boolean subStringSearch = false;

	@JsonProperty
	private boolean multiLine = true;

	@JsonProperty
	private boolean dotAll = true;

	@JsonProperty
	private boolean caseInsensitive = true;

	@JsonIgnore
	private Grok grok;

	public Grok getGrok(final GroksRegistry registry) throws FormatException {
		if (grok == null) {
			try {
				grok = Grok.compile(registry, (subStringSearch ? ".*?" : "") + pattern + (subStringSearch ? ".*?" : ""),
						(multiLine ? Pattern.MULTILINE : 0) | (dotAll ? Pattern.DOTALL : 0)
								| (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
			} catch (final Exception e) {
				throw new FormatException("Failed to compile grok pattern: " + this + " -> " + e.getMessage(), e);
			}
		}
		return grok;
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
		this.grok = null;
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
	public void setMultiLine(final boolean multiLine) {
		this.multiLine = multiLine;
		this.grok = null;
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
	public void setDotAll(final boolean dotAll) {
		this.dotAll = dotAll;
		this.grok = null;
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
	public void setCaseInsensitive(final boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		this.grok = null;
	}

	/**
	 * @return the subStringSearch
	 */
	public boolean isSubStringSearch() {
		return subStringSearch;
	}

	/**
	 * @param subStringSearch
	 *            the subStringSearch to set
	 */
	public void setSubStringSearch(final boolean subStringSearch) {
		this.subStringSearch = subStringSearch;
		this.grok = null;
	}

	@Override
	public String toString() {
		return "GrokPatternBean [pattern=" + pattern + ", subStringSearch=" + subStringSearch + ", multiLine="
				+ multiLine + ", dotAll=" + dotAll + ", caseInsensitive=" + caseInsensitive + ", grok=" + grok + "]";
	}

}
