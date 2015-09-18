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
package com.logsniffer.model.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.model.Log;
import com.logsniffer.model.support.DailyRollingLog;

/**
 * Source for timestamp rolled log files with a live file where the name is
 * static. This source supports exposing of multiple logs, because logs are
 * mainly referenced by the matching live files and combined with the
 * appropriated rolled over files.
 * 
 * @author mbok
 * 
 */
@Component
public class RollingLogsSource extends
		AbstractTimestampRollingLogsSource {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@JsonProperty
	private String pastLogsSuffixPattern = ".*";

	/**
	 * @return the pastLogsSuffixPattern
	 */
	public String getPastLogsSuffixPattern() {
		return pastLogsSuffixPattern;
	}

	/**
	 * @param pastLogsSuffixPattern
	 *            the pastLogsSuffixPattern to set
	 */
	public void setPastLogsSuffixPattern(final String pastLogsSuffixPattern) {
		this.pastLogsSuffixPattern = pastLogsSuffixPattern;
	}

	@Override
	public List<Log> getLogs() throws IOException {
		List<Log> logs = super.getLogs();
		List<Log> rollingLogs = new ArrayList<Log>(logs.size());
		for (int i = 0; i < logs.size(); i++) {
			Log liveLog = logs.get(i);
			logger.debug("Adapting live log to rolling log: {}", liveLog);
			rollingLogs.add(new DailyRollingLog(liveLog, getPastLogs(liveLog
					.getPath())));
		}
		return rollingLogs;
	}

	protected Log[] getPastLogs(final String liveLog) throws IOException {
		File dir = new File(FilenameUtils.getFullPathNoEndSeparator(liveLog));
		String pastPattern = FilenameUtils.getName(liveLog)
				+ getPastLogsSuffixPattern();
		FileFilter fileFilter = new WildcardFileFilter(pastPattern);
		File[] files = dir.listFiles(fileFilter);
		FileLog[] logs = new FileLog[files.length];
		Arrays.sort(files, getPastLogsType().getPastComparator());
		int i = 0;
		for (File file : files) {
			// TODO Decouple direct file log association
			logs[i++] = new FileLog(file);
		}
		logger.debug("Found {} past logs for {} with pattern {}", logs.length,
				liveLog, pastPattern);
		return logs;
	}

	@Override
	public Log getLog(final String path) throws IOException {
		Log liveLog = super.getLog(path);
		if (liveLog != null) {
			logger.debug("Adapting live log to rolling log: {}", liveLog);
			return new DailyRollingLog(liveLog, getPastLogs(liveLog.getPath()));
		} else {
			return null;
		}
	}
}
