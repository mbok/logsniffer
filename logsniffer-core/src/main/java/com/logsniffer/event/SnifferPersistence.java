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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.aspect.AspectHost;
import com.logsniffer.aspect.AspectProvider;
import com.logsniffer.aspect.sql.QueryAdaptor;
import com.logsniffer.event.SnifferScheduler.ScheduleInfo;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.util.ListQueryBuilder;
import com.logsniffer.util.PageableResult;

/**
 * Persistence for observers.
 * 
 * @author mbok
 * 
 */
public interface SnifferPersistence {
	public static class AspectSniffer extends Sniffer implements AspectHost {
		@JsonProperty
		private final HashMap<String, Object> aspects = new HashMap<String, Object>();

		@Override
		public <AspectType> void setAspect(final String key,
				final AspectType aspect) {
			aspects.put(key, aspect);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <AspectType> AspectType getAspect(final String key,
				final Class<AspectType> aspectType) {
			return (AspectType) aspects.get(key);
		}
	}

	public static interface SnifferListBuilder extends
			ListQueryBuilder<PageableResult<AspectSniffer>> {
		SnifferListBuilder withEventsCounter(
				AspectProvider<AspectSniffer, Integer> eventsCounter);

		SnifferListBuilder withScheduleInfo(
				QueryAdaptor<AspectSniffer, ScheduleInfo> adaptor);
	}

	public SnifferListBuilder getSnifferListBuilder();

	public long createSniffer(Sniffer sniffer);

	public void updateSniffer(Sniffer sniffer);

	public Sniffer getSniffer(long id);

	public void deleteSniffer(Sniffer sniffer);

	public IncrementData getIncrementData(Sniffer sniffer,
			LogSource<? extends LogInputStream> source, Log log)
			throws IOException;

	public Map<Log, IncrementData> getIncrementDataByLog(Sniffer sniffer,
			LogSource<? extends LogInputStream> source) throws IOException;

	public void storeIncrementalData(Sniffer observer,
			LogSource<? extends LogInputStream> source, Log log,
			IncrementData data);

}
