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
package com.logsniffer.app;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Initiates the Quartz scheduler.
 * 
 * @author mbok
 * 
 */
@Configuration
public class SchedulerAppConfig {
	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier(CoreAppConfig.BEAN_LOGSNIFFER_PROPS)
	private Properties logSnifferProperties;

	/**
	 * Defines a clustered Quartz Scheduler configured by properties from
	 * {@link #acBackendProperties}.
	 * 
	 * @return Quart scheduler
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactory() {
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setAutoStartup(true);
		schedulerFactory.setDataSource(dataSource);
		schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
		schedulerFactory.setQuartzProperties(logSnifferProperties);
		schedulerFactory.setOverwriteExistingJobs(true);
		schedulerFactory.setSchedulerName("LogsnifferScheduler");
		return schedulerFactory;
	}
}
