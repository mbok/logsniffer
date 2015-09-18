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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.event.EventPersistence.AspectEvent;
import com.logsniffer.event.es.EsEventPersistence.AspectEventImpl;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.model.SeverityLevelFactory;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.model.sql.FlatLogEntryPersistence.EntriesJoinType;
import com.logsniffer.model.sql.FlatLogEntryPersistence.FieldsProjection;
import com.logsniffer.model.support.DefaultPointer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class,
		QaDataSourceAppConfig.class, DefaultFlatLogEntryPersistenceTest.class })
@Configuration
public class DefaultFlatLogEntryPersistenceTest {
	@Autowired
	private JdbcTemplate jTpl;

	@Bean
	public FlatLogEntryPersistence entryPersistence() {
		return new DefaultFlatLogEntryPersistence();
	}

	@Autowired
	private FlatLogEntryPersistence persister;

	@Test
	@DirtiesContext
	public void testPersistence() {
		LogEntry entry = new LogEntry();
		entry.setSeverity(new SeverityLevel("DEBUG", 5,
				SeverityClassification.DEBUG));
		entry.setTimeStamp(new Date(55000));
		entry.setRawContent("abc");
		entry.setStartOffset(new DefaultPointer(0, 10));
		entry.setEndOffset(new DefaultPointer(5, 10));
		entry.getFields().put("f1", "test");
		entry.getFields().put("f2", new Date(9999000));
		entry.getFields().put(
				"f3",
				new SeverityLevel("INFO", 2,
						SeverityClassification.INFORMATIONAL));
		persister
				.persist(Collections.singletonList((LogEntryData) entry), 123l);
		SeverityLevelFactory slf = Mockito.mock(SeverityLevelFactory.class);
		Mockito.when(slf.resolveFor(2)).thenReturn(
				(SeverityLevel) entry.getFields().get("f3"));
		Mockito.when(slf.resolveFor(5)).thenReturn(entry.getSeverity());
		List<LogEntryData> entries = persister.getEntries(123l);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals("abc", entries.get(0).getRawContent());
		// 3 custom + _raw, _timestamp, _severity
		Assert.assertEquals(6, entries.get(0).getFields().size());
		Iterator<String> fieldKeys = entries.get(0).getFields().keySet()
				.iterator();
		Assert.assertEquals(entry.getStartOffset().getJson(), entries.get(0)
				.getStartOffset().getJson());
		Assert.assertEquals(entry.getEndOffset().getJson(), entries.get(0)
				.getEndOffset().getJson());
		Assert.assertEquals(LogEntryData.FIELD_TIMESTAMP, fieldKeys.next());
		Assert.assertEquals(LogEntryData.FIELD_SEVERITY_LEVEL, fieldKeys.next());
		Assert.assertEquals(LogEntryData.FIELD_RAW_CONTENT, fieldKeys.next());
		Assert.assertEquals("f1", fieldKeys.next());
		Assert.assertEquals("f2", fieldKeys.next());
		Assert.assertEquals("f3", fieldKeys.next());
		Assert.assertEquals("test", entries.get(0).getFields().get("f1"));
		Assert.assertEquals(new Date(55000), entries.get(0).getTimeStamp());
		Assert.assertEquals(new Date(9999000),
				entries.get(0).getFields().get("f2"));
		Assert.assertEquals("INFO", ((SeverityLevel) entries.get(0).getFields()
				.get("f3")).getName());
		Assert.assertEquals("DEBUG", entries.get(0).getSeverity().getName());
		Assert.assertEquals(2,
				((SeverityLevel) entries.get(0).getFields().get("f3"))
						.getOrdinalNumber());
	}

	@Test
	@DirtiesContext
	public void testJoiningEntryFields() {
		LogEntry entry = new LogEntry();
		entry.setSeverity(new SeverityLevel("DEBUG", 5,
				SeverityClassification.DEBUG));
		entry.setTimeStamp(new Date(55000));
		entry.setRawContent("abc");
		entry.setStartOffset(new DefaultPointer(0, 10));
		entry.setEndOffset(new DefaultPointer(5, 10));
		entry.getFields().put("f1", "test");
		entry.getFields().put("f2", new Date(9999000));
		entry.getFields().put(
				"f3",
				new SeverityLevel("INFO", 2,
						SeverityClassification.INFORMATIONAL));
		persister.persist(Collections.singletonList((LogEntryData) entry),
				12345l);
		QueryAdaptor<AspectEvent, FieldsMap> adaptor = persister
				.buildEntryFieldsQueryAdaptor(new FieldsProjection[] {
						new FieldsProjection("f1", "f1", FieldBaseTypes.STRING),
						new FieldsProjection("f3", "f3", FieldBaseTypes.SEVERITY) },
						null, EntriesJoinType.FIRST);
		List<AspectEvent> events = jTpl.query(
				adaptor.getQuery("SELECT 12345 AS ID"),
				adaptor.getQueryArgs(new ArrayList<Object>()).toArray(
						new Object[0]),
				adaptor.getRowMapper(new RowMapper<AspectEvent>() {
					@Override
					public AspectEvent mapRow(final ResultSet rs,
							final int rowNum) throws SQLException {
						return new AspectEventImpl();
					}
				}));
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(2, adaptor.getApsect(events.get(0)).size());
		Assert.assertEquals("test", adaptor.getApsect(events.get(0)).get("f1"));
		Assert.assertEquals("INFO",
				((SeverityLevel) adaptor.getApsect(events.get(0)).get("f3"))
						.getName());
		Assert.assertEquals(SeverityClassification.INFORMATIONAL,
				((SeverityLevel) adaptor.getApsect(events.get(0)).get("f3"))
						.getClassification());
		Assert.assertEquals(2, ((SeverityLevel) adaptor
				.getApsect(events.get(0)).get("f3")).getOrdinalNumber());

		// Additional entry
		LogEntry entry2 = new LogEntry();
		entry2.setSeverity(new SeverityLevel("DEBUG", 5,
				SeverityClassification.DEBUG));
		entry2.setTimeStamp(new Date(5000));
		entry2.setRawContent("abc");
		entry2.setStartOffset(new DefaultPointer(0, 10));
		entry2.setEndOffset(new DefaultPointer(5, 10));
		entry2.getFields().put("f1", "test2");
		entry2.getFields().put("f2", new Date(19999000));
		persister.persist(Collections.singletonList((LogEntryData) entry2),
				12345l);
		events = jTpl.query(adaptor.getQuery("SELECT 12345 AS ID"), adaptor
				.getQueryArgs(new ArrayList<Object>()).toArray(new Object[0]),
				adaptor.getRowMapper(new RowMapper<AspectEvent>() {
					@Override
					public AspectEvent mapRow(final ResultSet rs,
							final int rowNum) throws SQLException {
						return new AspectEventImpl();
					}
				}));
		// Still the same result
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(2, adaptor.getApsect(events.get(0)).size());
		Assert.assertEquals("test", adaptor.getApsect(events.get(0)).get("f1"));

		// New adaptor
		adaptor = persister.buildEntryFieldsQueryAdaptor(
				new FieldsProjection[] {
						new FieldsProjection("f1", "f1", FieldBaseTypes.STRING),
						new FieldsProjection("f3", "f3", FieldBaseTypes.SEVERITY) },
				null, EntriesJoinType.LAST);
		events = jTpl.query(adaptor.getQuery("SELECT 12345 AS ID"), adaptor
				.getQueryArgs(new ArrayList<Object>()).toArray(new Object[0]),
				adaptor.getRowMapper(new RowMapper<AspectEvent>() {
					@Override
					public AspectEvent mapRow(final ResultSet rs,
							final int rowNum) throws SQLException {
						return new AspectEventImpl();
					}
				}));
		// Still the same result
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(2, adaptor.getApsect(events.get(0)).size());
		Assert.assertEquals("test2", adaptor.getApsect(events.get(0)).get("f1"));
		Assert.assertNull(adaptor.getApsect(events.get(0)).get("f3"));

		// Now with aggregation
		adaptor = persister.buildEntryFieldsQueryAdaptor(
				new FieldsProjection[] { new FieldsProjection("f2", "f2",
						FieldBaseTypes.DATE) },
				"SELECT a.ID, MAX(a.f2) AS f2 FROM ({0}) AS a GROUP BY a.ID",
				EntriesJoinType.ALL);
		events = jTpl.query(adaptor.getQuery("SELECT 12345 AS ID"), adaptor
				.getQueryArgs(new ArrayList<Object>()).toArray(new Object[0]),
				adaptor.getRowMapper(new RowMapper<AspectEvent>() {
					@Override
					public AspectEvent mapRow(final ResultSet rs,
							final int rowNum) throws SQLException {
						return new AspectEventImpl();
					}
				}));
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(1, adaptor.getApsect(events.get(0)).size());
		Assert.assertEquals(entry2.getFields().get("f2"),
				adaptor.getApsect(events.get(0)).get("f2"));

		// Embedded timestamp field
		adaptor = persister.buildEntryFieldsQueryAdaptor(
				new FieldsProjection[] { new FieldsProjection("_timestamp",
						"tmst", FieldBaseTypes.DATE) },
				"SELECT a.tmst FROM ({0}) a ORDER BY a.tmst",
				EntriesJoinType.ALL);
		events = jTpl.query(adaptor.getQuery("SELECT 12345 AS ID"), adaptor
				.getQueryArgs(new ArrayList<Object>()).toArray(new Object[0]),
				adaptor.getRowMapper(new RowMapper<AspectEvent>() {
					@Override
					public AspectEvent mapRow(final ResultSet rs,
							final int rowNum) throws SQLException {
						return new AspectEventImpl();
					}
				}));
		Assert.assertEquals(2, events.size());
		Assert.assertEquals(new Date(5000), adaptor.getApsect(events.get(0))
				.get("_timestamp"));
		Assert.assertEquals(new Date(55000), adaptor.getApsect(events.get(1))
				.get("_timestamp"));
	}
}
