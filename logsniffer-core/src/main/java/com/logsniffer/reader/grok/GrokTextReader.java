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
package com.logsniffer.reader.grok;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.grok.GrokTextReader.GrokTextReaderConstructor;
import com.logsniffer.reader.support.AbstractPatternLineReader;

/**
 * Grok text reader.
 * 
 * @author mbok
 * 
 */
@PostConstructed(constructor = GrokTextReaderConstructor.class)
public class GrokTextReader extends AbstractPatternLineReader<GrokMatcher> {

	@Component
	public static class GrokTextReaderConstructor implements
			BeanPostConstructor<GrokTextReader> {
		@Autowired
		private GroksRegistry groksRegistry;

		@Override
		public void postConstruct(final GrokTextReader bean,
				final BeanConfigFactoryManager configManager)
				throws ConfigException {
			bean.groksRegistry = groksRegistry;
		}
	}

	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@NotEmpty
	@GrokPatternConstraint
	private String grokPattern;

	@JsonProperty
	private String overflowAttribute;

	@JsonProperty
	private boolean multiLine = true;

	@JsonProperty
	private boolean dotAll = true;

	@JsonProperty
	private boolean caseInsensitive = true;

	@JsonIgnore
	private Grok grok;

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return Collections.emptyList();
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes()
			throws FormatException {
		initPattern();
		LinkedHashMap<String, FieldBaseTypes> fields = new LinkedHashMap<String, FieldBaseTypes>();
		for (String attr : grok.getGroupNames().keySet()) {
			fields.put(attr, FieldBaseTypes.STRING);
		}
		if (overflowAttribute != null && !fields.containsKey(overflowAttribute)) {
			fields.put(overflowAttribute, FieldBaseTypes.STRING);
		}
		return fields;
	}

	@Override
	protected void initPattern() throws FormatException {
		try {
			grok = Grok.compile(groksRegistry, grokPattern,
					(isMultiLine() ? Pattern.MULTILINE : 0)
							| (isDotAll() ? Pattern.DOTALL : 0)
							| (isCaseInsensitive() ? Pattern.CASE_INSENSITIVE
									: 0));
		} catch (GrokException e) {
			throw new FormatException("Failed to compile grok pattern: "
					+ grokPattern + " -> " + e.getMessage(), e);
		}
	}

	@Override
	protected GrokMatcher matches(final String line) {
		GrokMatcher m = grok.matcher(line);
		return m.matches() ? m : null;
	}

	@Override
	protected void fillAttributes(final LogEntry entry, final GrokMatcher ctx)
			throws FormatException {
		LinkedHashMap<String, Integer> groups = grok.getGroupNames();
		for (String attrName : groups.keySet()) {
			entry.getFields().put(attrName, ctx.group(groups.get(attrName)));
		}
	}

	@Override
	protected void attachOverflowLine(final LogEntry entry,
			final String overflowLine) {
		if (overflowAttribute != null) {
			FieldsMap fMap = entry.getFields();
			String oldMsg = (String) fMap.get(overflowAttribute);
			if (oldMsg == null) {
				fMap.put(overflowAttribute, overflowLine);
			} else {
				fMap.put(overflowAttribute, oldMsg + "\n" + overflowLine);
			}
		}
	}

	@Override
	protected String getPatternInfo() {
		return grokPattern;
	}

	/**
	 * @return the grokPattern
	 */
	public String getGrokPattern() {
		return grokPattern;
	}

	/**
	 * @param grokPattern
	 *            the grokPattern to set
	 */
	public void setGrokPattern(final String grokPattern) {
		this.grokPattern = grokPattern;
	}

	/**
	 * @return the overflowAttribute
	 */
	public String getOverflowAttribute() {
		return overflowAttribute;
	}

	/**
	 * @param overflowAttribute
	 *            the overflowAttribute to set
	 */
	public void setOverflowAttribute(final String overflowAttribute) {
		this.overflowAttribute = StringUtils.isNotEmpty(overflowAttribute) ? overflowAttribute
				.trim() : null;
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
	}

}
