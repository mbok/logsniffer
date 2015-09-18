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
package com.logsniffer.model.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.ConfigException;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSource.LogSourceWrapper;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.support.BaseLogsSource;
import com.logsniffer.util.ReferenceIntegrityException;

/**
 * H2 based source provider.
 * 
 * @author mbok
 * 
 */
@Component
public class H2LogSourceProvider implements LogSourceProvider {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BeanConfigFactoryManager configManager;

	private class LogSourceCreator implements PreparedStatementCreator {
		private static final String SQL_INSERT = "INSERT INTO LOG_SOURCES (NAME, CONFIG) VALUES(?,?)";
		private static final String SQL_UPDATE = "UPDATE LOG_SOURCES SET NAME=?, CONFIG=? WHERE ID=?";
		private final LogSource<? extends LogInputStream> source;
		private final boolean insert;

		private LogSourceCreator(
				final LogSource<? extends LogInputStream> source,
				final boolean insert) {
			this.source = source;
			this.insert = insert;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con)
				throws SQLException {
			try {
				PreparedStatement ps = con.prepareStatement(insert ? SQL_INSERT
						: SQL_UPDATE);
				int c = 1;
				ps.setString(c++, source.getName());
				ps.setString(c++, configManager.saveBeanToJSON(source));
				if (!insert) {
					ps.setLong(c++, source.getId());
				}
				return ps;
			} catch (ConfigException e) {
				throw new SQLException("Not able to serialize config data", e);
			}
		}
	}

	/**
	 * Row mapper for log sources.
	 * 
	 * @author mbok
	 * 
	 */
	private class SourceRowMapper implements
			RowMapper<LogSource<LogInputStream>> {
		private static final String SQL_PROJECTION = "SELECT ID, NAME, CONFIG FROM LOG_SOURCES";

		@Override
		public LogSource<LogInputStream> mapRow(final ResultSet rs,
				final int rowNum) throws SQLException {
			final long id = rs.getLong("ID");
			final String name = rs.getString("NAME");
			final String config = rs.getString("CONFIG");
			LogSource<LogInputStream> source = new LogSourceWrapper() {
				@SuppressWarnings("unchecked")
				@Override
				public LogSource<LogInputStream> getWrapped() {
					try {
						BaseLogsSource<LogInputStream> wrapped = (BaseLogsSource<LogInputStream>) configManager
								.createBeanFromJSON(LogSource.class, config);
						wrapped.setId(id);
						return wrapped;
					} catch (ConfigException e) {
						logger.error("Failed to deserialize log source: " + id,
								e);
						return LogSource.NULL_SOURCE;
					}
				}

				@Override
				public long getId() {
					return id;
				}

				@Override
				public String getName() {
					return name;
				}
			};
			return source;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public long createSource(final LogSource<? extends LogInputStream> source) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new LogSourceCreator(source, true), keyHolder);
		long id = keyHolder.getKey().longValue();
		return id;
	}

	@Override
	public List<LogSource<LogInputStream>> getSources() {
		return jdbcTemplate.query(SourceRowMapper.SQL_PROJECTION
				+ " ORDER BY NAME", new SourceRowMapper());
	}

	@Override
	public LogSource<LogInputStream> getSourceById(final long id) {
		List<LogSource<LogInputStream>> sources = jdbcTemplate.query(
				SourceRowMapper.SQL_PROJECTION + " WHERE ID=?",
				new Object[] { id }, new SourceRowMapper());
		return sources.size() > 0 ? sources.get(0) : null;
	}

	@Override
	public void updateSource(final LogSource<? extends LogInputStream> source) {
		jdbcTemplate.update(new LogSourceCreator(source, false));
	}

	@Override
	public void deleteSource(final LogSource<? extends LogInputStream> source)
			throws ReferenceIntegrityException {
		try {
			jdbcTemplate.update("DELETE FROM LOG_SOURCES WHERE ID=?",
					source.getId());
			logger.info("Deleted source with id: {}", source.getId());
		} catch (DataIntegrityViolationException e) {
			logger.info("Deleting source with id {} failed due to references",
					source.getId());
			throw new ReferenceIntegrityException(LogSource.class, e);
		}
	}

}
