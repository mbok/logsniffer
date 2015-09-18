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
package com.logsniffer.event.processing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.logsniffer.event.SnifferScheduler.ScheduleInfo;

/**
 * Update {@link ScheduleInfo} information for sniffers.
 * 
 * @author mbok
 * 
 */
@Component
public class ScheduleInfoAccess {
	@Autowired
	private JdbcTemplate jTpl;

	public static final RowMapper<ScheduleInfo> SCHEDULE_INFO_MAPPER = new RowMapper<ScheduleInfo>() {
		@Override
		public ScheduleInfo mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			return new ScheduleInfo(rs.getBoolean("SCHEDULED"),
					rs.getTimestamp("LAST_FIRE"));
		}
	};

	public void updateScheduleInfo(final long snifferId,
			final ScheduleInfo info) {
		jTpl.update(
				"MERGE INTO SNIFFERS_SCHEDULE_INFO (SNIFFER,SCHEDULED,LAST_FIRE) KEY(SNIFFER) VALUES(?,?,?)",
				snifferId, info.isScheduled(), info.getLastFireTime());
	}

	public ScheduleInfo getScheduleInfo(final long snifferId) {
		List<ScheduleInfo> info = jTpl.query(
				"SELECT * FROM SNIFFERS_SCHEDULE_INFO WHERE SNIFFER=?",
				SCHEDULE_INFO_MAPPER, snifferId);
		if (info.size() > 0) {
			return info.get(0);
		} else {
			return new ScheduleInfo(false, null);
		}
	}
}
