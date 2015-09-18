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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.logsniffer.aspect.AspectHost;
import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.LogPointerTransfer;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.model.support.JsonLogPointer;
import com.logsniffer.util.sql.QueryBuilderUtils;

/**
 * Persists {@link LogEntryData} entities to {@value #entriesTableName} and
 * their fields to {@value #entriesFieldsTableName}.
 * 
 * @author mbok
 * 
 */
@Component
public class DefaultFlatLogEntryPersistence implements FlatLogEntryPersistence {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private String entriesTableName = "LOG_ENTRIES";
	private String entriesFieldsTableName = "LOG_ENTRIES_FIELDS";

	private String ctxName = "entries";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * @return the entriesTableName
	 */
	public String getEntriesTableName() {
		return entriesTableName;
	}

	/**
	 * @param entriesTableName
	 *            the entriesTableName to set
	 */
	public void setEntriesTableName(final String entriesTableName) {
		this.entriesTableName = entriesTableName;
	}

	/**
	 * @return the entriesFieldsTableName
	 */
	public String getEntriesFieldsTableName() {
		return entriesFieldsTableName;
	}

	/**
	 * @param entriesFieldsTableName
	 *            the entriesFieldsTableName to set
	 */
	public void setEntriesFieldsTableName(final String entriesFieldsTableName) {
		this.entriesFieldsTableName = entriesFieldsTableName;
	}

	/**
	 * @return the ctxName
	 */
	public String getCtxName() {
		return ctxName;
	}

	/**
	 * @param ctxName
	 *            the ctxName to set
	 */
	public void setCtxName(final String ctxName) {
		this.ctxName = ctxName;
	}

	@Override
	@Transactional
	public void persist(final List<LogEntryData> entries, final long ctxId) {
		for (int i = 0; i < entries.size(); i++) {
			LogEntryData entry = entries.get(i);
			GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(new LogEntryStatementCreator(entriesTableName,
					entry, ctxId), keyHolder);
			long id = keyHolder.getKey().longValue();
			BatchFieldsStatementSetter fieldsSetter = new BatchFieldsStatementSetter(
					id, entry.getFields(), LogEntryData.FIELD_RAW_CONTENT,
					LogEntryData.FIELD_SEVERITY_LEVEL,
					LogEntryData.FIELD_TIMESTAMP);
			jdbcTemplate.batchUpdate(
					fieldsSetter.getInsertSql(entriesFieldsTableName),
					fieldsSetter);
		}
	}

	@Override
	public <T extends AspectHost> QueryAdaptor<T, FieldsMap> buildEntryFieldsQueryAdaptor(
			final FieldsProjection[] joinFields, final String adaptorSql,
			final EntriesJoinType joinType) {
		return new QueryAdaptor<T, FieldsMap>() {

			@Override
			public FieldsMap getApsect(final T host) {
				return host.getAspect("entryFields", FieldsMap.class);
			}

			@Override
			public String getQuery(final String innerQuery) {
				String innerTable = "i_" + ctxName;
				String eJoinTable = "e_" + ctxName;
				StringBuilder projSql = new StringBuilder("SELECT "
						+ innerTable + ".*");
				StringBuilder fromSql = new StringBuilder("FROM (" + innerQuery
						+ ") AS " + innerTable + " LEFT JOIN "
						+ entriesTableName + " AS " + eJoinTable + " ON ("
						+ innerTable + ".ID = " + eJoinTable + ".CTX_ID");
				String aggregFunc = null;
				switch (joinType) {
				case FIRST:
					aggregFunc = "MIN";
					break;
				case LAST:
					aggregFunc = "MAX";
					break;
				default:
				}
				if (aggregFunc != null) {
					fromSql.append(" AND " + eJoinTable + ".ID=(SELECT "
							+ aggregFunc + "(ID) FROM " + entriesTableName
							+ " WHERE CTX_ID=" + eJoinTable + ".CTX_ID)");
				}
				fromSql.append(")");
				for (FieldsProjection fp : joinFields) {
					projSql.append(", ");
					// LOG_ENTRIES nested field columns
					if (fp.getFieldName().equals(LogEntryData.FIELD_TIMESTAMP)) {
						projSql.append(eJoinTable + ".TMST AS `"
								+ fp.getSqlName() + "`");
					} else if (fp.getFieldName().equals(
							LogEntryData.FIELD_SEVERITY_LEVEL)) {
						projSql.append(eJoinTable + ".SEVERITY AS `"
								+ fp.getSqlName() + "`");
						projSql.append(", " + eJoinTable + ".SEVERITY_C AS `"
								+ fp.getSqlName() + "_C`");
						projSql.append(", " + eJoinTable + ".SEVERITY_N AS `"
								+ fp.getSqlName() + "_N`");
					} else if (fp.getFieldName().equals(
							LogEntryData.FIELD_RAW_CONTENT)) {
						projSql.append(eJoinTable + ".RAW_CONTENT AS `"
								+ fp.getSqlName() + "`");
					} else {
						// Arbitrary joined field
						String fJoinTable = "f_"
								+ ctxName
								+ "_"
								+ QueryBuilderUtils.normalizeAsName(fp
										.getSqlName());
						projSql.append(fJoinTable + ".");
						switch (fp.getType()) {
						case STRING:
							projSql.append("V_STR AS `" + fp.getSqlName() + "`");
							break;
						case DATE:
							projSql.append("V_DATE AS `" + fp.getSqlName()
									+ "`");
							break;
						case SEVERITY:
							projSql.append("V_INT AS `" + fp.getSqlName() + "`");
							projSql.append(", " + fJoinTable + ".V_JSON AS `"
									+ fp.getSqlName() + "_JSON" + "`");
							break;
						case INTEGER:
							projSql.append("V_INT AS `" + fp.getSqlName() + "`");
							break;
						case FLOAT:
							projSql.append("V_FLOAT AS `" + fp.getSqlName()
									+ "`");
							break;
						case OBJECT:
							projSql.append("V_JSON AS `" + fp.getSqlName()
									+ "`");
							break;
						}
						fromSql.append(" LEFT JOIN " + entriesFieldsTableName
								+ " " + fJoinTable + " ON (" + fJoinTable
								+ ".SUBJ=" + eJoinTable + ".ID AND "
								+ fJoinTable + ".NAME=?)");
					}
				}
				String combinedSql = projSql.toString() + " "
						+ fromSql.toString();
				logger.debug("Log entry sub selection SQL: {}", combinedSql);
				return adaptorSql != null ? MessageFormat.format(adaptorSql,
						combinedSql) : combinedSql;
			}

			@Override
			public List<Object> getQueryArgs(final List<Object> innerArgs) {
				ArrayList<Object> aArgs = new ArrayList<Object>(innerArgs);
				for (FieldsProjection fp : joinFields) {
					if (fp.getFieldName().equals(LogEntryData.FIELD_TIMESTAMP)
							|| fp.getFieldName().equals(
									LogEntryData.FIELD_SEVERITY_LEVEL)
							|| fp.getFieldName().equals(
									LogEntryData.FIELD_RAW_CONTENT)) {
						continue;
					}
					aArgs.add(fp.getFieldName());
				}
				return aArgs;
			}

			@Override
			public RowMapper<T> getRowMapper(
					final RowMapper<? extends T> innerMapper) {
				return new RowMapper<T>() {

					@Override
					public T mapRow(final ResultSet rs, final int rowNum)
							throws SQLException {
						T host = innerMapper.mapRow(rs, rowNum);
						FieldsMap fieldsMap = new FieldsMap();
						host.setAspect("entryFields", fieldsMap);
						for (FieldsProjection fp : joinFields) {
							try {
								mapFieldFromSql(fieldsMap, fp.getFieldName(),
										fp.getType(), fp.getSqlName(), rs);
							} catch (Exception e) {
								throw new SQLException("Failed to map field: "
										+ fp.getFieldName(), e);
							}
						}
						return host;
					}
				};
			}
		};
	}

	public void mapFieldFromSql(final FieldsMap map, final String fName,
			final FieldBaseTypes fType, final String colName, final ResultSet rs)
			throws SQLException, JsonParseException, JsonMappingException,
			IOException {
		switch (fType) {
		case STRING:
			map.put(fName, rs.getString(colName != null ? colName : "V_STR"));
			break;
		case DATE:
			map.put(fName,
					rs.getTimestamp(colName != null ? colName : "V_DATE"));
			break;
		case INTEGER:
			map.put(fName, rs.getLong(colName != null ? colName : "V_INT"));
			break;
		case FLOAT:
			map.put(fName, rs.getDouble(colName != null ? colName : "V_FLOAT"));
			break;
		case SEVERITY:
			if (fName.equals(LogEntryData.FIELD_SEVERITY_LEVEL)) {
				// Direct table mapped severity
				SeverityLevel severity = getDirectMappedSeverity(rs);
				if (severity != null) {
					map.put(fName, severity);
				}
			} else {
				String jsonCol = colName != null ? colName + "_JSON" : "V_JSON";
				String json = rs.getString(jsonCol);
				if (StringUtils.isNotBlank(json)) {
					map.put(fName, BatchFieldsStatementSetter.getObjectMapper()
							.readValue(json, SeverityLevel.class));
				} else {
					int severityNr = rs.getInt(colName != null ? colName
							: "V_INT");
					if (!rs.wasNull()) {
						map.put(fName,
								new SeverityLevel(null, severityNr, null));
					} else {
						map.put(fName, null);
					}
				}
			}
			break;
		case OBJECT:
			String jsonObj = rs.getString(colName != null ? colName : "V_JSON");
			if (StringUtils.isNotBlank(jsonObj)) {
				map.put(fName, BatchFieldsStatementSetter.getObjectMapper()
						.readValue(jsonObj, Object.class));
			}
			break;
		}
	}

	private SeverityLevel getDirectMappedSeverity(final ResultSet rs)
			throws SQLException {
		int severityNr = rs.getInt("SEVERITY");
		if (!rs.wasNull()) {
			int clazzId = rs.getInt("SEVERITY_C");
			SeverityClassification clazz = clazzId < SeverityClassification
					.values().length ? SeverityClassification.values()[clazzId]
					: null;
			return new SeverityLevel(rs.getString("SEVERITY_N"), severityNr,
					clazz);
		} else {
			return null;
		}
	}

	@Override
	public List<LogEntryData> getEntries(final long ctxId) {

		return jdbcTemplate
				.query("SELECT e.*, f.* FROM "
						+ entriesTableName
						+ " e LEFT JOIN "
						+ entriesFieldsTableName
						+ " f ON (e.ID=f.SUBJ) WHERE e.CTX_ID=? ORDER BY e.ID, f.SEQ_NR",
						new ResultSetExtractor<List<LogEntryData>>() {
							@Override
							public List<LogEntryData> extractData(
									final ResultSet rs) throws SQLException,
									DataAccessException {
								ArrayList<LogEntryData> entries = new ArrayList<LogEntryData>();
								LogEntryDataFacade entry = null;
								long entryId = 0;
								while (rs.next()) {
									if (entryId != rs.getLong("ID")) {
										entryId = rs.getLong("ID");
										entry = new LogEntryDataFacade(rs
												.getString("OFFSET_START"), rs
												.getString("OFFSET_END"));
										entries.add(entry);
										if (rs.getTimestamp("TMST") != null) {
											entry.setTimeStamp(rs
													.getTimestamp("TMST"));
										}
										SeverityLevel severity = getDirectMappedSeverity(rs);
										if (severity != null) {
											entry.setSeverity(severity);
										}
										entry.setRawContent(rs
												.getString("RAW_CONTENT"));
									}
									// Fields only
									FieldBaseTypes fType = FieldBaseTypes.values()[rs
											.getInt("TYPE")];
									String fName = rs.getString("NAME");
									try {
										mapFieldFromSql(entry.getFields(),
												fName, fType, null, rs);
									} catch (Exception e) {
										throw new SQLException(
												"Failed to map field: " + fName,
												e);
									}
								}
								return entries;
							}

						}, ctxId);
	}
}

/**
 * {@link LogEntryData} facade.
 * 
 * @author mbok
 * 
 */
class LogEntryDataFacade extends LogEntryData {
	private JsonLogPointer startOffset;
	private String startOffsetStr;
	private JsonLogPointer endOffset;
	private String endOffsetStr;

	public LogEntryDataFacade(final String startOffsetStr,
			final String endOffsetStr) {
		super();
		this.startOffsetStr = startOffsetStr;
		this.endOffsetStr = endOffsetStr;
	}

	@Override
	public LogPointerTransfer getStartOffset() {
		if (startOffset == null) {
			startOffset = new JsonLogPointer(startOffsetStr);
		}
		return startOffset;
	}

	@Override
	public LogPointerTransfer getEndOffset() {
		if (endOffset == null) {
			endOffset = new JsonLogPointer(endOffsetStr);
		}
		return endOffset;
	}

}