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
package com.logsniffer.model.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.springframework.jdbc.core.PreparedStatementCreator;

import com.logsniffer.model.LogEntryData;

/**
 * {@link PreparedStatementCreator} for {@link LogEntryData} entities persisted
 * in LOG_ENTRIES table for a given context.
 * 
 * @author mbok
 * 
 */
public class LogEntryStatementCreator implements PreparedStatementCreator {
	private long ctxId;
	private LogEntryData logEntry;
	private String tableName;

	public LogEntryStatementCreator(final String tableName,
			final LogEntryData logEntry, final long ctxId) {
		super();
		this.tableName = tableName;
		this.ctxId = ctxId;
		this.logEntry = logEntry;
	}

	@Override
	public PreparedStatement createPreparedStatement(final Connection con)
			throws SQLException {
		PreparedStatement stmt = con
				.prepareCall("INSERT INTO "
						+ tableName
						+ " (CTX_ID, TMST, SEVERITY, SEVERITY_C, SEVERITY_N, OFFSET_START, OFFSET_END, RAW_CONTENT) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		int c = 1;
		stmt.setLong(c++, ctxId);
		if (logEntry.getTimeStamp() != null) {
			stmt.setTimestamp(c++, new Timestamp(logEntry.getTimeStamp()
					.getTime()));
		} else {
			stmt.setTimestamp(c++, null);
		}
		if (logEntry.getSeverity() != null) {
			stmt.setInt(c++, logEntry.getSeverity().getOrdinalNumber());
			stmt.setInt(c++, logEntry.getSeverity().getClassification()
					.ordinal());
			stmt.setString(c++, logEntry.getSeverity().getName());
		} else {
			stmt.setNull(c++, Types.INTEGER);
			stmt.setNull(c++, Types.INTEGER);
			stmt.setNull(c++, Types.VARCHAR);
		}
		stmt.setString(c++, logEntry.getStartOffset().getJson());
		stmt.setString(c++, logEntry.getEndOffset().getJson());
		stmt.setString(c++, logEntry.getRawContent());
		return stmt;
	}
}
