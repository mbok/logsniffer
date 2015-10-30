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

import java.util.LinkedHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.event.EventData;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.reader.FormatException;
import com.logsniffer.util.grok.Grok;
import com.logsniffer.util.grok.GrokConsumerConstructor;
import com.logsniffer.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.logsniffer.util.grok.GrokMatcher;
import com.logsniffer.util.grok.GrokPatternBean;
import com.logsniffer.util.grok.GroksRegistry;

/**
 * Scans for log entries matching a regex / Grok pattern.
 * 
 * @author mbok
 *
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class RegexPatternScanner extends SingleEntryIncrementalMatcher implements GrokConsumer {
	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@JsonUnwrapped
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	@NotEmpty
	private String sourceField;

	@Override
	public EventData matches(LogEntry entry) throws FormatException {
		Object value = entry.getFields().get(sourceField);
		if (value instanceof String) {
			Grok grok = grokBean.getGrok(groksRegistry);
			GrokMatcher matcher = grok.matcher((String) value);
			if (matcher.matches()) {
				EventData event = new EventData();
				FieldsMap fields = event.getFields();
				LinkedHashMap<String, Integer> groups = grok.getGroupNames();
				for (String attrName : groups.keySet()) {
					matcher.setToField(attrName, fields);
				}
				return event;
			}
		}
		return null;
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
	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

}
