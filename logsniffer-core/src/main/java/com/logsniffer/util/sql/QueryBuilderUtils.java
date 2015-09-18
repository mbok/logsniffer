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
package com.logsniffer.util.sql;

import java.util.List;

public class QueryBuilderUtils {

	/**
	 * Returns the sql filter clause when value to set as prepared argument
	 * isn't null otherwise null is returned.
	 * 
	 * @param sql
	 *            the sql to return
	 * @param value
	 *            the value to bind as prepared arg
	 * @param preparedArgs
	 *            list of globals prepared args
	 * @return sql or null if value is null
	 */
	public static String buildFilterClause(final String sql,
			final Object value, final List<Object> preparedArgs) {
		if (value != null) {
			preparedArgs.add(value);
			return sql;
		} else {
			return null;
		}
	}

	/**
	 * Joins not null clauses to a complete string.
	 * 
	 * @param separator
	 *            the separator for clauses
	 * @param clauses
	 *            null-able clauses
	 * @return joined SQL clauses
	 */
	public static String joinClauses(final String separator,
			final String... clauses) {
		StringBuilder sql = new StringBuilder();
		if (clauses != null) {
			for (String c : clauses) {
				if (c != null) {
					if (sql.length() > 0) {
						sql.append(separator);
					}
					sql.append(c);
				}
			}
		}
		if (sql.length() > 0) {
			return sql.toString();
		} else {
			return null;
		}
	}

	/**
	 * Replaces all characters not matching the pattern [^\\w\\d_]+ by an
	 * underscore.
	 * 
	 * @param sqlName
	 *            string to normalize to use as SQL valid name
	 * @return normalized string
	 */
	public static String normalizeAsName(final String sqlName) {
		return sqlName.replaceAll("[^\\w\\d_]+", "_");
	}
}
