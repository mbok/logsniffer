package com.logsniffer.util.grok;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON model for {@link GrokPatternBean}.
 * 
 * @author mbok
 *
 */
public interface GrokPatternBeanJsonModel {
	/**
	 * @return the pattern
	 */
	@JsonProperty
	public String getPattern();

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	@JsonProperty
	public void setPattern(final String pattern);

	/**
	 * @return the multiLine
	 */
	@JsonProperty
	public boolean isMultiLine();

	/**
	 * @param multiLine
	 *            the multiLine to set
	 */
	@JsonProperty
	public void setMultiLine(final boolean multiLine);

	/**
	 * @return the dotAll
	 */
	@JsonProperty
	public boolean isDotAll();

	/**
	 * @param dotAll
	 *            the dotAll to set
	 */
	@JsonProperty
	public void setDotAll(final boolean dotAll);

	/**
	 * @return the caseInsensitive
	 */
	@JsonProperty
	public boolean isCaseInsensitive();

	/**
	 * @param caseInsensitive
	 *            the caseInsensitive to set
	 */
	@JsonProperty
	public void setCaseInsensitive(final boolean caseInsensitive);

	/**
	 * @return the subStringSearch
	 */
	@JsonProperty
	public boolean isSubStringSearch();

	/**
	 * @param subStringSearch
	 *            the subStringSearch to set
	 */
	@JsonProperty
	public void setSubStringSearch(final boolean subStringSearch);
}
