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

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
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
	private static final String DB_SETUP_VERSION = "0.5.4";

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
	 * Used to indicate if DB is initialized the first time. It helps to drive
	 * further DB initializations.
	 * 
	 * @author mbok
	 * 
	 */
	public static interface DBInitIndicator {
		/**
		 * Returns true if DB was initialized.
		 * 
		 */
		boolean isNewSchema();
	}

	/**
	 * @return H2 pooled data source
	 * @throws SQLException
	 */
	@Bean(destroyMethod = "dispose")
	public DataSource dataSource() throws SQLException {
		final JdbcConnectionPool pool = JdbcConnectionPool.create(url, user, password);
		pool.setMaxConnections(maxPoolConnections);
		Connection con = null;
		con = pool.getConnection();

		final Flyway flyway = new Flyway();
		flyway.setLocations("classpath:sql/migration");
		flyway.setDataSource(pool);
		flyway.setSqlMigrationPrefix("VOS-");

		final JdbcTemplate tpl = new JdbcTemplate(pool);
		if (tpl.queryForObject("select count(*) from information_schema.tables where table_name = 'LOG_SOURCES'",
				int.class) == 0) {
			logger.info("H2 database not found, creating new schema and populate with default data");
			flyway.setBaselineVersion(MigrationVersion.fromVersion(DB_SETUP_VERSION));
			flyway.setBaselineOnMigrate(true);
			try {
				final ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
				dbPopulator.addScript(new ClassPathResource("/sql/quartz/tables_h2.sql"));
				dbPopulator.addScript(new ClassPathResource("/sql/model/schema_h2.sql"));
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
			if (tpl.queryForObject("select count(*) from information_schema.tables where table_name = 'schema_version'",
					int.class) == 0) {
				logger.info("Flyway's DB migration not setup in this version, set baseline version to 0.5.0");
				flyway.setBaselineVersion(MigrationVersion.fromVersion("0.5.0"));
				flyway.setBaselineOnMigrate(true);
			}
		}

		logger.debug("Migrating database, base version is: {}", flyway.getBaselineVersion());
		flyway.migrate();
		logger.debug("Database migrated from base version: {}", flyway.getBaselineVersion());

		return pool;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public DBInitIndicator dbInitPopulate() {
		return new DBInitIndicator() {

			@Override
			public boolean isNewSchema() {
				return newSchema;
			}
		};
	}
}
