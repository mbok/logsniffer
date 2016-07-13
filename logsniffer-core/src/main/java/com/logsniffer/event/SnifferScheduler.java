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
package com.logsniffer.event;

import java.util.Date;

import org.quartz.SchedulerException;

import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.event.SnifferPersistence.AspectSniffer;

/**
 * Scheduler for sniffers.
 * 
 * @author mbok
 * 
 */
public interface SnifferScheduler {
	/**
	 * Schedule info aspect related to a {@link AspectSniffer}.
	 * 
	 * @author mbok
	 * 
	 */
	public static class ScheduleInfo {
		private boolean scheduled;
		private Date lastFireTime;

		public ScheduleInfo(final boolean scheduled, final Date lastFireTime) {
			super();
			this.scheduled = scheduled;
			this.lastFireTime = lastFireTime;
		}

		public ScheduleInfo() {
			super();
		}

		/**
		 * @param scheduled
		 *            the scheduled to set
		 */
		public void setScheduled(final boolean scheduled) {
			this.scheduled = scheduled;
		}

		/**
		 * @param lastFireTime
		 *            the lastFireTime to set
		 */
		public void setLastFireTime(final Date lastFireTime) {
			this.lastFireTime = lastFireTime;
		}

		/**
		 * @return the scheduled
		 */
		public boolean isScheduled() {
			return scheduled;
		}

		/**
		 * @return the lastFireTime
		 */
		public Date getLastFireTime() {
			return lastFireTime;
		}

	}

	public void startSniffing(long snifferId) throws SchedulerException;

	public void stopSniffing(long snifferId) throws SchedulerException;

	public boolean isScheduled(long snifferId) throws SchedulerException;

	/**
	 * Returns null-safe schedule info for given sniffer.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return null-safe schedule info for given sniffer
	 */
	public ScheduleInfo getScheduleInfo(long snifferId);

	/**
	 * 
	 * @return returns aspect adaptor for accessing schedule info
	 * @deprecated aspects no longer on the road map in that way
	 */
	@Deprecated
	public QueryAdaptor<AspectSniffer, ScheduleInfo> getScheduleInfoAspectAdaptor();
}
