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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.logsniffer.model.Log;
import com.logsniffer.model.support.DailyRollingLog;

/**
 * Source for timestamp rolled log files with a live file where the name is
 * dynamic. This source exposes only a single log, because the
 * {@link #getPattern()} is used to detect the live and the rolled over files.
 * 
 * @author mbok
 * 
 */
@Component
public class RollingLogsSourceDynamicLiveName extends
		AbstractTimestampRollingLogsSource {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public List<Log> getLogs() throws IOException {
		List<Log> logs = super.getLogs();
		if (!logs.isEmpty()) {
			Collections
					.sort(logs, getPastLogsType().getPastComparatorForLogs());
			Log liveLog = logs.get(0);
			List<Log> pastLogs = logs.subList(1, logs.size());
			logger.debug(
					"Exposing rolling log with dynamic live file {} and rolled over files: {}",
					liveLog, pastLogs);
			List<Log> rolledLog = new ArrayList<>();
			rolledLog.add(new DailyRollingLog(liveLog, pastLogs));
			return rolledLog;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Log getLog(final String path) throws IOException {
		List<Log> logs = getLogs();
		if (logs.isEmpty()) {
			return null;
		} else {
			return logs.get(0);
		}
	}
}
