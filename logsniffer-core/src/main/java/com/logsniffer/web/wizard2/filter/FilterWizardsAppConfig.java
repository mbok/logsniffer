package com.logsniffer.web.wizard2.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.fields.filter.support.JsonParseFilter;
import com.logsniffer.fields.filter.support.RegexFilter;
import com.logsniffer.fields.filter.support.SeverityMappingFilter;
import com.logsniffer.fields.filter.support.TimestampConvertFilter;
import com.logsniffer.web.wizard2.ConfigBeanWizard;
import com.logsniffer.web.wizard2.SimpleBeanWizard;

/**
 * Exposes wizards for {@link FieldsFilter}s.
 * 
 * @author mbok
 *
 */
@Configuration
public class FilterWizardsAppConfig {
	@Bean
	public ConfigBeanWizard<RegexFilter> regexFilterWizard() {
		return new SimpleBeanWizard<RegexFilter>("logsniffer.wizard.filter.regexFilter",
				"/ng/wizards/filter/regexFilter.html", RegexFilter.class, new RegexFilter());
	}

	@Bean
	public ConfigBeanWizard<SeverityMappingFilter> severityMappingFilterWizard() {
		return new SimpleBeanWizard<SeverityMappingFilter>("logsniffer.wizard.filter.severityMappingFilter",
				"/ng/wizards/filter/severityMapping.html", SeverityMappingFilter.class, new SeverityMappingFilter());
	}

	@Bean
	public ConfigBeanWizard<JsonParseFilter> jsonFilterWizard() {
		return new SimpleBeanWizard<JsonParseFilter>("logsniffer.wizard.filter.jsonParseFilter",
				"/ng/wizards/filter/jsonParser.html", JsonParseFilter.class, new JsonParseFilter());
	}

	@Bean
	public ConfigBeanWizard<TimestampConvertFilter> timestampConvertFilterWizard() {
		return new SimpleBeanWizard<TimestampConvertFilter>("logsniffer.wizard.filter.timestampConvert",
				"/ng/wizards/filter/timestampConvert.html", TimestampConvertFilter.class, new TimestampConvertFilter());
	}

}
