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

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Configured the live datasource.
 * 
 * @author mbok
 * 
 */
@Configuration
public class DataSourceAppConfig {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value(value = "${logsniffer.h2.user}")
	private String user;

	@Value(value = "${logsniffer.h2.password}")
	private String password;

	@Value(value = "${logsniffer.h2.url}")
	private String url;

	@Value(value = "${logsniffer.h2.maxPoolConnections:5}")
	private final int maxPoolConnections = 5;

	private boolean newSchema = false;

	/**
	 * Used to populate DB settings when DB is initialized first.
	 * 
	 * @author mbok
	 * 
	 */
	public static interface DBInitPopulator {
		/**
		 * Populates DB settings when DB is initialized first.
		 * 
		 * @throws Exception
		 *             in case of errors
		 */
		void populate() throws Exception;
	}

	/**
	 * @return H2 pooled data source
	 * @throws SQLException
	 */
	@Bean(destroyMethod = "dispose")
	public DataSource dataSource() throws SQLException {
		JdbcConnectionPool pool = JdbcConnectionPool.create(url, user, password);
		pool.setMaxConnections(maxPoolConnections);
		Connection con = null;
		con = pool.getConnection();
		JdbcTemplate tpl = new JdbcTemplate(pool);
		if (tpl.queryForInt("select count(*) from information_schema.tables where table_name = 'LOG_SOURCES'") == 0) {
			logger.info("H2 database not found, creating new schema and populate with default data");
			try {
				ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
				dbPopulator.addScript(new ClassPathResource("/sql/quartz/tables_h2.sql"));
				dbPopulator.addScript(new ClassPathResource("/sql/model/schema_h2.sql"));
				// dbPopulator.addScript(new
				// ClassPathResource("/sql/model/schema_h2_data.sql"));
				dbPopulator.populate(con);
				newSchema = true;
				logger.info("Established H2 connection pool with new database");
			} finally {
				if (con != null) {
					con.close();
				}
			}
		} else {
			logger.info("Established H2 connection pool with existing database");
		}
		return pool;
	}

	@PostConstruct
	public void populateNewSchema() throws Exception {
		if (newSchema) {
			for (DBInitPopulator pop : ContextProvider.getContext().getBeansOfType(DBInitPopulator.class).values()) {
				pop.populate();
			}
		}
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
