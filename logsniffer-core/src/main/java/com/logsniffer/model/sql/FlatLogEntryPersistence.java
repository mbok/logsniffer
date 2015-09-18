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

import java.util.List;

import com.logsniffer.aspect.AspectHost;
import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;

/**
 * Persists log entries flat related to its fields to allow SQL retrieval.
 * 
 * @author mbok
 * 
 */
public interface FlatLogEntryPersistence {

	public void persist(List<LogEntryData> entries, long ctxId);

	public List<LogEntryData> getEntries(long ctxId);

	/**
	 * Describes the projection for a field.
	 * 
	 * @author mbok
	 * 
	 */
	public static class FieldsProjection {
		private String fieldName;
		private String sqlName;
		private FieldBaseTypes type;

		public FieldsProjection(final String fieldName, final String sqlName,
				final FieldBaseTypes type) {
			super();
			this.fieldName = fieldName;
			this.sqlName = sqlName;
			this.type = type;
		}

		public FieldsProjection(final String fieldName, final FieldBaseTypes type) {
			this(fieldName, fieldName, type);
		}

		/**
		 * @return the fieldName
		 */
		public String getFieldName() {
			return fieldName;
		}

		/**
		 * @return the sqlName
		 */
		public String getSqlName() {
			return sqlName;
		}

		/**
		 * @return the type
		 */
		public FieldBaseTypes getType() {
			return type;
		}

	}

	public enum EntriesJoinType {
		FIRST, LAST, ALL;
	}

	public <T extends AspectHost> QueryAdaptor<T, FieldsMap> buildEntryFieldsQueryAdaptor(
			FieldsProjection[] joinFields, String adaptorSql,
			EntriesJoinType joinType);
}
