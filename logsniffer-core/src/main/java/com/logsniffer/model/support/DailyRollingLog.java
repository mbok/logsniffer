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
package com.logsniffer.model.support;

import java.util.List;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.RollingLog;

/**
 * Combines multiple daily rolled logs and the current live log into one
 * representation. The path and last modified attributes are reflected from the
 * live log. The live log is located in {@link DailyRollingLog#logs} at index 0.
 * 
 * @author mbok
 * 
 */
public class DailyRollingLog implements RollingLog {
	private final String name;
	private final String path;
	private final Log[] logs;

	public DailyRollingLog(final String name, final String path, final Log liveLog, final List<Log> pastLogs) {
		this(name, path, liveLog, pastLogs.toArray(new Log[pastLogs.size()]));
	}

	public DailyRollingLog(final String name, final String path, final Log liveLog, final Log... pastLogs) {
		super();
		this.logs = new Log[pastLogs.length + 1];
		int i = 0;
		this.logs[i++] = liveLog;
		for (final Log log : pastLogs) {
			this.logs[i++] = log;
		}
		this.name = name;
		this.path = path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public long getSize() {
		long size = 0;
		for (final Log log : logs) {
			size += log.getSize();
		}
		return size;
	}

	@Override
	public long getLastModified() {
		return logs[0].getLastModified();
	}

	@Override
	public int hashCode() {
		return logs[0].hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DailyRollingLog other = (DailyRollingLog) obj;
		return logs[0].equals(other.logs[0]);
	}

	@Override
	public String toString() {
		return "RollingLog [liveLog=" + logs[0] + "]";
	}

	@Override
	public Log[] getParts() {
		return logs;
	}

	@Override
	public LogPointer getGlobalPointer(final String partPath, final LogPointer partPointer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SizeMetric getSizeMetric() {
		return SizeMetric.BYTE;
	}

}
