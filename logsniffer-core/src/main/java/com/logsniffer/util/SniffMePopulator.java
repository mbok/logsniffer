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
package com.logsniffer.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.logsniffer.app.ContextProvider;
import com.logsniffer.app.DataSourceAppConfig.DBInitIndicator;
import com.logsniffer.app.LogSnifferHome;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.file.RollingLogsSource;
import com.logsniffer.reader.filter.FilteredLogEntryReader;
import com.logsniffer.reader.log4j.Log4jTextReader;

/**
 * Registers LogSniffers own logs as source.
 * 
 * @author mbok
 * 
 */
@Component
public class SniffMePopulator implements ApplicationContextAware {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private LogSnifferHome home;

	@Autowired
	private LogSourceProvider sourceProvider;

	@Autowired
	private DBInitIndicator dbInitIndicator;

	@Autowired
	private ContextProvider dummy;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populate() {
		final RollingLogsSource myLogSource = new RollingLogsSource();
		myLogSource.setPattern(new File(home.getHomeDir(), "logs/logsniffer.log").getPath());
		myLogSource.setName("logsniffer's own log");
		final Log4jTextReader reader = new Log4jTextReader();
		reader.setFormatPattern("%d %-5p [%c] %m%n");
		final Map<String, String> specifiersFieldMapping = new HashMap<>();
		specifiersFieldMapping.put("d", "date");
		specifiersFieldMapping.put("p", "priority");
		specifiersFieldMapping.put("c", "category");
		specifiersFieldMapping.put("m", "message");
		reader.setSpecifiersFieldMapping(specifiersFieldMapping);
		myLogSource.setReader(new FilteredLogEntryReader(reader, null));
		sourceProvider.createSource(myLogSource);
		logger.info("Created source for LogSniffer's own log: {}", myLogSource);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		if (dbInitIndicator.isNewSchema()) {
			try {
				populate();
			} catch (final Exception e) {
				logger.error("Failed to create logsniffer's own log", e);
			}
		}
	}
}
