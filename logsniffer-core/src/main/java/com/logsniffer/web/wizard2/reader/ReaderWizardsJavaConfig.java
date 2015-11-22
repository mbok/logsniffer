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
package com.logsniffer.web.wizard2.reader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logsniffer.fields.filter.support.JsonParseFilter;
import com.logsniffer.fields.filter.support.SeverityMappingFilter;
import com.logsniffer.fields.filter.support.TimestampConvertFilter;
import com.logsniffer.reader.log4j.Log4jTextReader;
import com.logsniffer.web.wizard2.ConfigBeanWizard;
import com.logsniffer.web.wizard2.SimpleBeanWizard;

/**
 * Exposes simple reader wizards.
 *
 * @author mbok
 *
 */
@Configuration
public class ReaderWizardsJavaConfig {
	@Bean
	public ConfigBeanWizard<Log4jTextReader> log4jTextReaderWizard() {
		return new SimpleBeanWizard<Log4jTextReader>(
				"logsniffer.wizard.reader.log4j", "wizards/readers/log4j",
				Log4jTextReader.class, new Log4jTextReader());
	}

	@Bean
	public ConfigBeanWizard<SeverityMappingFilter> severityMappingFilterWizard() {
		return new SimpleBeanWizard<SeverityMappingFilter>(
				"logsniffer.wizard.reader.filter.severityMappingFilter",
				"/ng/wizards/reader/filter/severityMapping.html",
				SeverityMappingFilter.class, new SeverityMappingFilter());
	}

	@Bean
	public ConfigBeanWizard<JsonParseFilter> jsonFilterWizard() {
		return new SimpleBeanWizard<JsonParseFilter>(
				"logsniffer.wizard.reader.filter.jsonParseFilter",
				"/ng/wizards/reader/filter/jsonParser.html",
				JsonParseFilter.class, new JsonParseFilter());
	}

	@Bean
	public ConfigBeanWizard<TimestampConvertFilter> timestampConvertFilterWizard() {
		return new SimpleBeanWizard<TimestampConvertFilter>(
				"logsniffer.wizard.reader.filter.timestampConvert",
				"/ng/wizards/reader/filter/timestampConvert.html",
				TimestampConvertFilter.class, new TimestampConvertFilter());
	}

}
