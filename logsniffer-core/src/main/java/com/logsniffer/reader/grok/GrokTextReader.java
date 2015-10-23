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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.support.AbstractPatternLineReader;
import com.logsniffer.util.grok.Grok;
import com.logsniffer.util.grok.GrokConsumerConstructor;
import com.logsniffer.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.logsniffer.util.grok.GrokException;
import com.logsniffer.util.grok.GrokMatcher;
import com.logsniffer.util.grok.GrokPatternBean;
import com.logsniffer.util.grok.GroksRegistry;

/**
 * Grok text reader.
 * 
 * @author mbok
 * 
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class GrokTextReader extends AbstractPatternLineReader<GrokMatcher>implements GrokConsumer {

	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@JsonUnwrapped
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	private String overflowAttribute;

	@JsonIgnore
	private Grok grok;

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return Collections.emptyList();
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
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
			grok = Grok.compile(groksRegistry, grokBean.getPattern(),
					(grokBean.isMultiLine() ? Pattern.MULTILINE : 0) | (grokBean.isDotAll() ? Pattern.DOTALL : 0)
							| (grokBean.isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0));
		} catch (GrokException e) {
			throw new FormatException("Failed to compile grok pattern: " + grokBean + " -> " + e.getMessage(), e);
		}
	}

	@Override
	protected GrokMatcher matches(final String line) {
		GrokMatcher m = grok.matcher(line);
		return m.matches() ? m : null;
	}

	@Override
	protected void fillAttributes(final LogEntry entry, final GrokMatcher ctx) throws FormatException {
		LinkedHashMap<String, Integer> groups = grok.getGroupNames();
		for (String attrName : groups.keySet()) {
			entry.getFields().put(attrName, ctx.group(groups.get(attrName)));
		}
	}

	@Override
	protected void attachOverflowLine(final LogEntry entry, final String overflowLine) {
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

	/**
	 * @return the grokPattern
	 */
	@Deprecated
	public String getGrokPattern() {
		return grokBean.getPattern();
	}

	/**
	 * @param grokPattern
	 *            the grokPattern to set
	 */
	@Deprecated
	public void setGrokPattern(final String grokPattern) {
		grokBean.setPattern(grokPattern);
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
		this.overflowAttribute = StringUtils.isNotEmpty(overflowAttribute) ? overflowAttribute.trim() : null;
	}

	@Override
	protected String getPatternInfo() {
		return grokBean.getPattern();
	}

	/**
	 * @return the grokBean
	 */
	public GrokPatternBean getGrokBean() {
		return grokBean;
	}

	/**
	 * @param grokBean
	 *            the grokBean to set
	 */
	public void setGrokBean(GrokPatternBean grokBean) {
		this.grokBean = grokBean;
	}

	@Override
	public void initGrokFactory(GroksRegistry groksRegistry) {
		this.groksRegistry = groksRegistry;
	}

}
