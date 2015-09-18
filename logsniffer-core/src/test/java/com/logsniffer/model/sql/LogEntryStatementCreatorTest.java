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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.support.DefaultPointer;

/**
 * Test for {@link LogEntryStatementCreator}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class,
		QaDataSourceAppConfig.class })
public class LogEntryStatementCreatorTest {
	@Autowired
	private JdbcTemplate jTpl;

	@Test
	public void testLogEntryCreation() {
		LogEntry entry = new LogEntry();
		entry.setRawContent("abc");
		entry.setStartOffset(new DefaultPointer(0, 10));
		entry.setEndOffset(new DefaultPointer(5, 10));
		jTpl.update(new LogEntryStatementCreator(
				new DefaultFlatLogEntryPersistence().getEntriesTableName(),
				entry, 2));
	}
}
