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
import java.text.ParseException;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence;
import com.logsniffer.event.SnifferPersistence.AspectSniffer;
import com.logsniffer.event.SnifferScheduler;

/**
 * Manages sniffer jobs.
 * 
 * @author mbok
 * 
 */
@Component
public class SnifferJobManager implements SnifferScheduler {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private ScheduleInfoAccess scheduleInfoAccess;

	@Override
	@Transactional(rollbackFor = { SchedulerException.class, ParseException.class })
	public void startSniffing(final long snifferId) throws SchedulerException {
		logger.debug("Starting cron job for sniffer: {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new SchedulerException("Sniffer not found: " + snifferId);
		}
		stopAndDeleteAllSnifferJobs(sniffer.getId());
		MutableTrigger trigger;
		try {
			trigger = CronScheduleBuilder.cronScheduleNonvalidatedExpression(sniffer.getScheduleCronExpression())
					.withMisfireHandlingInstructionDoNothing().build();
		} catch (final ParseException e) {
			throw new SchedulerException("Failed to parse cron expression", e);
		}
		trigger.setKey(getKey(sniffer, sniffer.getLogSourceId()));
		final JobDetail jobDetail = JobBuilder.newJob(SnifferJob.class).requestRecovery()
				.withIdentity(getJobKey(sniffer, sniffer.getLogSourceId())).build();
		scheduler.scheduleJob(jobDetail, trigger);
		final ScheduleInfo scheduleInfo = scheduleInfoAccess.getScheduleInfo(snifferId);
		scheduleInfo.setScheduled(true);
		scheduleInfoAccess.updateScheduleInfo(snifferId, scheduleInfo);
		logger.info("Scheduled cron job for sniffer {} and log source {} with trigger {}", sniffer,
				sniffer.getLogSourceId(), trigger);
	}

	protected TriggerKey getKey(final Sniffer sniffer, final long logSourceId) {
		return TriggerKey.triggerKey(sniffer.getId() + ":" + logSourceId, "SNIFFER:" + sniffer.getId());
	}

	protected JobKey getJobKey(final Sniffer sniffer, final long logSourceId) {
		return JobKey.jobKey(sniffer.getId() + ":" + logSourceId, "SNIFFER:" + sniffer.getId());
	}

	protected static JobKey getJobKey(final long snifferId, final long logSourceId) {
		return JobKey.jobKey(snifferId + ":" + logSourceId, "SNIFFER:" + snifferId);
	}

	protected static long getSnifferId(final JobKey key) {
		return Long.parseLong(key.getName().split(":")[0]);
	}

	protected static long getLogSourceId(final JobKey key) {
		return Long.parseLong(key.getName().split(":")[1]);
	}

	protected void stopAndDeleteAllSnifferJobs(final long snifferId) throws SchedulerException {
		for (final JobKey job : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("SNIFFER:" + snifferId))) {
			logger.info("Deleting scheduled job for sniffer={} and log source={}", snifferId, getLogSourceId(job));
			scheduler.deleteJob(job);
			logger.info("Interrupting job for sniffer={} and log source={}", snifferId, getLogSourceId(job));
			scheduler.interrupt(job);
		}
	}

	@Transactional(rollbackFor = { SchedulerException.class })
	@Override
	public void stopSniffing(final long snifferId) throws SchedulerException {
		logger.debug("Stopping scheduled cron jobs for sniffer: {}", snifferId);
		stopAndDeleteAllSnifferJobs(snifferId);
		final ScheduleInfo scheduleInfo = scheduleInfoAccess.getScheduleInfo(snifferId);
		scheduleInfo.setScheduled(false);
		scheduleInfoAccess.updateScheduleInfo(snifferId, scheduleInfo);
	}

	@Override
	public boolean isScheduled(final long snifferId) throws SchedulerException {
		return scheduleInfoAccess.getScheduleInfo(snifferId).isScheduled();
	}

	@Override
	public QueryAdaptor<AspectSniffer, ScheduleInfo> getScheduleInfoAspectAdaptor() {
		return SCHEDULE_INFO_ADAPTOR;
	}

	private static final QueryAdaptor<SnifferPersistence.AspectSniffer, SnifferScheduler.ScheduleInfo> SCHEDULE_INFO_ADAPTOR = new QueryAdaptor<SnifferPersistence.AspectSniffer, SnifferScheduler.ScheduleInfo>() {

		@Override
		public ScheduleInfo getApsect(final AspectSniffer host) {
			return host.getAspect("scheduleInfo", ScheduleInfo.class);
		}

		@Override
		public RowMapper<AspectSniffer> getRowMapper(final RowMapper<? extends AspectSniffer> innerMapper) {
			return new RowMapper<SnifferPersistence.AspectSniffer>() {
				@Override
				public AspectSniffer mapRow(final ResultSet rs, final int rowNum) throws SQLException {
					final AspectSniffer sniffer = innerMapper.mapRow(rs, rowNum);
					sniffer.setAspect("scheduleInfo", ScheduleInfoAccess.SCHEDULE_INFO_MAPPER.mapRow(rs, rowNum));
					return sniffer;
				}
			};
		}

		@Override
		public List<Object> getQueryArgs(final List<Object> innerArgs) {
			return innerArgs;
		}

		@Override
		public String getQuery(final String innerQuery) {
			return "SELECT oxy.*, ssi.SCHEDULED, ssi.LAST_FIRE FROM (" + innerQuery
					+ ") oxy LEFT JOIN SNIFFERS_SCHEDULE_INFO ssi ON (oxy.ID=ssi.SNIFFER)";
		}
	};

	@Override
	public ScheduleInfo getScheduleInfo(final long snifferId) {
		return scheduleInfoAccess.getScheduleInfo(snifferId);
	}
}
