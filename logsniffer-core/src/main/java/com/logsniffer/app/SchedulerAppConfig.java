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
import java.util.UUID;

import javax.sql.DataSource;

import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleInstanceIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static Logger logger = LoggerFactory.getLogger(SchedulerAppConfig.class);

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
		schedulerFactory.setOverwriteExistingJobs(true);
		schedulerFactory.setSchedulerName("LogsnifferScheduler");
		Properties quartzProperties = new Properties(logSnifferProperties);
		quartzProperties.setProperty("org.quartz.scheduler.instanceIdGenerator.class",
				SafeSimpleInstanceIdGenerator.class.getName());
		schedulerFactory.setQuartzProperties(quartzProperties);
		return schedulerFactory;
	}

	/**
	 * Because {@link SimpleInstanceIdGenerator} can fail in case of DNS issues
	 * with the local host name, this class provides in a fallback a UUID.
	 * 
	 * @author mbok
	 *
	 */
	public static final class SafeSimpleInstanceIdGenerator extends SimpleInstanceIdGenerator {
		@Override
		public String generateInstanceId() throws SchedulerException {
			try {
				String name = super.generateInstanceId();
				logger.info("Using host based schedule instance id: {}", name);
				return name;
			} catch (Exception e) {
				String name = UUID.randomUUID().toString();
				logger.info(
						"Using generated UUID '{}' for scheduler instance id, because of errors getting the host name: {}",
						name, (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
				return name;
			}
		}

	}
}
