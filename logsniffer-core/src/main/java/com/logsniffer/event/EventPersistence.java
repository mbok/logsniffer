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
import java.util.List;

import com.logsniffer.aspect.AspectHost;
import com.logsniffer.aspect.AspectProvider;
import com.logsniffer.event.SnifferPersistence.AspectSniffer;
import com.logsniffer.util.ListQueryBuilder;
import com.logsniffer.util.PageableResult;

/**
 * Persistence for events.
 * 
 * @author mbok
 * 
 */
public interface EventPersistence {
	/**
	 * Aspect extended {@link Event} impl.
	 * 
	 * @author mbok
	 * 
	 */
	public static interface AspectEvent extends EventAbstract, AspectHost {
	}

	/**
	 * A histrogram entry.
	 * 
	 * @author mbok
	 * 
	 */
	public static class HistogramEntry {
		private final long time;
		private final long count;

		public HistogramEntry(final long time, final long count) {
			super();
			this.time = time;
			this.count = count;
		}

		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}

		/**
		 * @return the count
		 */
		public long getCount() {
			return count;
		}

	}

	/**
	 * Valid histogram intervals.
	 * 
	 * @author mbok
	 * 
	 */
	public enum HistogramInterval {
		SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
	}

	public static class EventsCountHistogram {
		private List<HistogramEntry> entries;
		private HistogramInterval interval;

		public EventsCountHistogram() {
			super();
		}

		public EventsCountHistogram(final List<HistogramEntry> entries, final HistogramInterval interval) {
			super();
			this.entries = entries;
			this.interval = interval;
		}

		/**
		 * @return the entries
		 */
		public List<HistogramEntry> getEntries() {
			return entries;
		}

		/**
		 * @param entries
		 *            the entries to set
		 */
		public void setEntries(final List<HistogramEntry> entries) {
			this.entries = entries;
		}

		/**
		 * @return the interval
		 */
		public HistogramInterval getInterval() {
			return interval;
		}

		/**
		 * @param interval
		 *            the interval to set
		 */
		public void setInterval(final HistogramInterval interval) {
			this.interval = interval;
		}

	}

	/**
	 * Event result attached by event histogram.
	 * 
	 * @author mbok
	 * 
	 */
	public static class EventsResult extends PageableResult<AspectEvent> {
		private EventsCountHistogram eventsCountHistogram;

		public EventsResult() {
			super();
		}

		public EventsResult(final long totalCount, final List<AspectEvent> items,
				final EventsCountHistogram histogram) {
			super(totalCount, items);
			this.eventsCountHistogram = histogram;
		}

		/**
		 * @return the eventsCountHistogram
		 */
		public EventsCountHistogram getEventsCountHistogram() {
			return eventsCountHistogram;
		}

		/**
		 * @param eventsCountHistogram
		 *            the eventsCountHistogram to set
		 */
		public void setEventsCountHistogram(final EventsCountHistogram eventsCountHistogram) {
			this.eventsCountHistogram = eventsCountHistogram;
		}

	}

	public static interface BaseEventQueryBuilder<BuilderType> extends ListQueryBuilder<EventsResult> {
		BuilderType withEventCountTimeHistogram(int maxHistogramIntervalSlots);
	}

	public static interface EventQueryBuilder extends BaseEventQueryBuilder<EventQueryBuilder> {

		EventQueryBuilder sortByEntryTimestamp(boolean desc);

		EventQueryBuilder withOccurrenceFrom(Date from);

		EventQueryBuilder withOccurrenceTo(Date to);
	}

	public static interface NativeQueryBuilder extends BaseEventQueryBuilder<NativeQueryBuilder> {
		NativeQueryBuilder withNativeQuery(final String nativeQuery);
	}

	public String persist(Event event);

	public void delete(long snifferId, String[] eventIds);

	public void deleteAll(long snifferId);

	public EventQueryBuilder getEventsQueryBuilder(long snifferId, long offset, int limit);

	public NativeQueryBuilder getEventsNativeQueryBuilder(long snifferId, long offset, int limit);

	public Event getEvent(long snifferId, String eventId);

	public AspectProvider<AspectSniffer, Integer> getEventsCounter();

}
