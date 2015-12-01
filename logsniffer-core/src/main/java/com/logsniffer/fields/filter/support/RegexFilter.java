package com.logsniffer.fields.filter.support;

import java.util.LinkedHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.reader.FormatException;
import com.logsniffer.util.grok.Grok;
import com.logsniffer.util.grok.GrokConsumerConstructor;
import com.logsniffer.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.logsniffer.util.grok.GrokMatcher;
import com.logsniffer.util.grok.GrokPatternBean;
import com.logsniffer.util.grok.GroksRegistry;

/**
 * Extracts data from an input field using regex.
 * 
 * @author mbok
 *
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class RegexFilter implements FieldsFilter, GrokConsumer {
	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	@NotEmpty
	private String sourceField;

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
	 * @return the grokBean
	 */
	public GrokPatternBean getGrokBean() {
		return grokBean;
	}

	/**
	 * @param grokBean
	 *            the grokBean to set
	 */
	public void setGrokBean(final GrokPatternBean grokBean) {
		this.grokBean = grokBean;
	}

	@Override
	public void filter(final FieldsMap fields) throws FormatException {
		final Object value = fields.get(sourceField);
		if (value != null) {
			final Grok grok = grokBean.getGrok(groksRegistry);
			final GrokMatcher matcher = grok.matcher(value.toString());
			if (matcher.matches()) {
				final LinkedHashMap<String, Integer> groups = grok.getGroupNames();
				for (final String attrName : groups.keySet()) {
					matcher.setToField(attrName, fields);
				}
			}
		}

	}

	@Override
	public void filterKnownFields(final LinkedHashMap<String, FieldBaseTypes> knownFields) throws FormatException {
		knownFields.putAll(grokBean.getGrok(groksRegistry).getFieldTypes());

	}

	@Override
	public void initGrokFactory(final GroksRegistry groksRegistry) {
		this.groksRegistry = groksRegistry;
	}

}
