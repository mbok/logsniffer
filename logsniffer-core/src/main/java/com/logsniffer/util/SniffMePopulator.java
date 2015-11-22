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
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logsniffer.app.DataSourceAppConfig.DBInitPopulator;
import com.logsniffer.fields.filter.FilteredLogEntryReader;
import com.logsniffer.app.LogSnifferHome;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.file.RollingLogsSource;
import com.logsniffer.reader.log4j.Log4jTextReader;

/**
 * Registers LogSniffers own logs as source.
 * 
 * @author mbok
 * 
 */
@Component
public class SniffMePopulator implements DBInitPopulator {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private LogSnifferHome home;

	@Autowired
	private LogSourceProvider sourceProvider;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void populate() throws ParseException {
		RollingLogsSource myLogSource = new RollingLogsSource();
		myLogSource.setPattern(new File(home.getHomeDir(), "logs/logsniffer.log").getPath());
		myLogSource.setName("logsniffer");
		Log4jTextReader reader = new Log4jTextReader();
		reader.setFormatPattern("%d{ABSOLUTE} %-5p [%c] %m%n");
		myLogSource.setReader(new FilteredLogEntryReader(reader, null));
		sourceProvider.createSource(myLogSource);
		logger.info("Created source for LogSniffer's own log: {}", myLogSource);
	}
}
