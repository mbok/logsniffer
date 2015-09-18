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
import java.io.IOException;
import java.util.Comparator;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogRawAccessor;
import com.logsniffer.model.support.ByteLogInputStream;
import com.logsniffer.model.support.DailyRollingLog;
import com.logsniffer.model.support.DailyRollingLogAccess;

/**
 * Source for timestamp based rolled log files based on
 * {@link WildcardLogsSource}.
 * 
 * @author mbok
 * 
 */
public abstract class AbstractTimestampRollingLogsSource extends
		WildcardLogsSource {

	@JsonProperty
	private PastLogsType pastLogsType = PastLogsType.NAME;

	/**
	 * @return the pastLogsType
	 */
	public PastLogsType getPastLogsType() {
		return pastLogsType;
	}

	/**
	 * @param pastLogsType
	 *            the pastLogsType to set
	 */
	public void setPastLogsType(final PastLogsType pastLogsType) {
		this.pastLogsType = pastLogsType;
	}

	public enum PastLogsType {
		NAME, LAST_MODIFIED;

		public Comparator<File> getPastComparator() {
			if (this == NAME) {
				return new Comparator<File>() {
					@Override
					public int compare(final File o1, final File o2) {
						return -FilenameUtils.getName(o1.getPath()).compareTo(
								FilenameUtils.getName(o2.getPath()));
					}
				};
			} else {
				return new Comparator<File>() {
					@Override
					public int compare(final File o1, final File o2) {
						return -(int) (o1.lastModified() - o2.lastModified());
					}
				};
			}
		}

		public Comparator<Log> getPastComparatorForLogs() {
			if (this == NAME) {
				return new Comparator<Log>() {
					@Override
					public int compare(final Log o1, final Log o2) {
						return -FilenameUtils.getName(o1.getPath()).compareTo(
								FilenameUtils.getName(o2.getPath()));
					}
				};
			} else {
				return new Comparator<Log>() {
					@Override
					public int compare(final Log o1, final Log o2) {
						return -(int) (o1.getLastModified() - o2
								.getLastModified());
					}
				};
			}
		}
	}

	@Override
	public LogRawAccess<ByteLogInputStream> getLogAccess(final Log origLog)
			throws IOException {
		DailyRollingLog log = (DailyRollingLog) getLog(origLog.getPath());
		if (log != null) {
			return new DailyRollingLogAccess(
					new LogRawAccessor<ByteLogInputStream, Log>() {
						@Override
						public LogRawAccess<ByteLogInputStream> getLogAccess(
								final Log log) throws IOException {
							return getLogAccessAdapter() != null ? getLogAccessAdapter()
									.getLogAccess((FileLog) log)
									: new DirectFileLogAccess((FileLog) log);
						}
					}, log);
		} else {
			return null;
		}
	}

}
