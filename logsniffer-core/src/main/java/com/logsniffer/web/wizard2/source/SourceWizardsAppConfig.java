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
package com.logsniffer.web.wizard2.source;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logsniffer.model.file.RollingLogsSource;
import com.logsniffer.model.file.RollingLogsSourceDynamicLiveName;
import com.logsniffer.model.file.WildcardLogsSource;
import com.logsniffer.web.wizard2.ConfigBeanWizard;
import com.logsniffer.web.wizard2.SimpleBeanWizard;

/**
 * Exposes standard wizards for log sources.
 * 
 * @author mbok
 * 
 */
@Configuration
public class SourceWizardsAppConfig {
	@Bean
	public ConfigBeanWizard<WildcardLogsSource> wildcardFileSourceWizard() {
		return new SimpleBeanWizard<WildcardLogsSource>(
				"logsniffer.wizard.source.file.wildcard",
				"/wizards/source/file.wildcard", WildcardLogsSource.class,
				new WildcardLogsSource());
	}

	@Bean
	public ConfigBeanWizard<RollingLogsSource> rollingFileStaticLiveSourceWizard() {
		return new SimpleBeanWizard<RollingLogsSource>(
				"logsniffer.wizard.source.file.timestampRollingStaticLiveName",
				"/ng/wizards/source/timestampRollingFileStaticLiveName.html",
				RollingLogsSource.class, new RollingLogsSource());
	}

	@Bean
	public ConfigBeanWizard<RollingLogsSourceDynamicLiveName> rollingFileDynamicLiveSourceWizard() {
		return new SimpleBeanWizard<RollingLogsSourceDynamicLiveName>(
				"logsniffer.wizard.source.file.timestampRollingDynamicLiveName",
				"/ng/wizards/source/timestampRollingFileDynamicLiveName.html",
				RollingLogsSourceDynamicLiveName.class,
				new RollingLogsSourceDynamicLiveName());
	}
}
