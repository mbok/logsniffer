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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;

/**
 * Prepared statement setter for a flat persisted {@link FieldsMap}.
 * 
 * @author mbok
 * 
 */
public class BatchFieldsStatementSetter implements BatchPreparedStatementSetter {
	private static final Logger logger = LoggerFactory
			.getLogger(BatchFieldsStatementSetter.class);
	private long subjId;
	private List<Entry<String, Object>> fields;
	private Map<String, FieldBaseTypes> types;
	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.WRAPPER_OBJECT);

	}

	public BatchFieldsStatementSetter(final long subjId,
			final FieldsMap fields, final String... keysToIgnore) {
		super();
		this.subjId = subjId;
		this.fields = new ArrayList<Entry<String, Object>>();
		for (Entry<String, Object> entry : fields.entrySet()) {
			if (keysToIgnore == null
					|| !ArrayUtils.contains(keysToIgnore, entry.getKey())) {
				this.fields.add(entry);
			}
		}
		this.types = fields.getTypes();
	}

	protected static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public String getInsertSql(final String tableName) {
		return "INSERT INTO "
				+ tableName
				+ " (SUBJ, NAME, SEQ_NR, TYPE, V_STR, V_DATE, V_INT, V_FLOAT, V_JSON) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	public void setValues(final PreparedStatement ps, final int i)
			throws SQLException {
		Entry<String, Object> fieldEntry = fields.get(i);
		FieldBaseTypes type = types.get(fieldEntry.getKey());
		if (type == null) {
			logger.warn("Unspecified type for field {}, handle as string",
					fieldEntry.getKey());
			type = FieldBaseTypes.STRING;
		}
		int c = 1;
		ps.setLong(c++, subjId);
		ps.setString(c++, fieldEntry.getKey());
		ps.setInt(c++, i);
		ps.setInt(c++, type.ordinal());
		ps.setNull(c++, Types.VARCHAR); // 5
		ps.setNull(c++, Types.TIMESTAMP); // 6
		ps.setNull(c++, Types.BIGINT); // 7
		ps.setNull(c++, Types.DOUBLE); // 8
		ps.setNull(c++, Types.VARCHAR); // 9
		try {
			switch (type) {
			case STRING:
				ps.setString(5, fieldEntry.getValue().toString());
				break;
			case DATE:
				ps.setTimestamp(6,
						new Timestamp(((Date) fieldEntry.getValue()).getTime()));
				break;
			case INTEGER:
				ps.setObject(7, fieldEntry.getValue());
				break;
			case FLOAT:
				ps.setObject(8, fieldEntry.getValue());
				break;
			case SEVERITY:
				ps.setInt(7, ((SeverityLevel) fieldEntry.getValue())
						.getOrdinalNumber());
				ps.setString(9,
						objectMapper.writeValueAsString(fieldEntry.getValue()));
				break;
			case OBJECT:
				ps.setString(9,
						objectMapper.writeValueAsString(fieldEntry.getValue()));
				break;
			}
		} catch (Exception e) {
			throw new SQLException("Failed to map field: " + fieldEntry, e);
		}
	}

	@Override
	public int getBatchSize() {
		return fields.size();
	}

}
