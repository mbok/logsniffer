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
package com.logsniffer.model;

import java.util.List;

import com.logsniffer.util.ReferenceIntegrityException;

/**
 * Provides registered {@link LogSource}s.
 * 
 * @author mbok
 * 
 */
public interface LogSourceProvider {
	/**
	 * Returns a list of registered log sources.
	 * 
	 * @return list of registered log sources
	 */
	public List<LogSource<LogRawAccess<? extends LogInputStream>>> getSources();

	/**
	 * Returns a log source for given id.
	 * 
	 * @param id
	 *            id log source to return
	 * @return the destined log source or null if not found
	 */
	public LogSource<LogRawAccess<? extends LogInputStream>> getSourceById(long id);

	/**
	 * Persists a new log source.
	 * 
	 * @param source
	 *            the source to persist.
	 * @return id of the persisted source.
	 */
	public long createSource(LogSource<? extends LogRawAccess<? extends LogInputStream>> source);

	/**
	 * Updates the given source.
	 * 
	 * @param source
	 *            source to update.
	 */
	public void updateSource(LogSource<? extends LogRawAccess<? extends LogInputStream>> source);

	/**
	 * Deletes the given source.
	 * 
	 * @param source
	 */
	public void deleteSource(LogSource<? extends LogRawAccess<? extends LogInputStream>> source)
			throws ReferenceIntegrityException;
}
