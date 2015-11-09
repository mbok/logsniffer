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
package com.logsniffer.event.es;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Order;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.logsniffer.app.ElasticSearchAppConfig.ClientCallback;
import com.logsniffer.app.ElasticSearchAppConfig.ElasticClientTemplate;
import com.logsniffer.aspect.AspectProvider;
import com.logsniffer.aspect.PostAspectProvider;
import com.logsniffer.event.Event;
import com.logsniffer.event.EventPersistence;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence;
import com.logsniffer.event.SnifferPersistence.AspectSniffer;
import com.logsniffer.event.SnifferPersistence.SnifferChangedEvent;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.sql.FlatLogEntryPersistence.EntriesJoinType;
import com.logsniffer.model.sql.FlatLogEntryPersistence.FieldsProjection;
import com.logsniffer.reader.FormatException;
import com.logsniffer.util.DataAccessException;

import net.sf.json.util.JSONBuilder;

/**
 * Elastic search event persistence.
 * 
 * @author mbok
 * 
 */
@Component
@Primary
public class EsEventPersistence implements EventPersistence {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String EVENTS_COUNT = "eventsCount";

	@Autowired
	private LogSourceProvider logSourceProvider;

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private ElasticClientTemplate clientTpl;

	@Value(value = "${logsniffer.es.indexName}")
	private String indexName;

	private ObjectMapper jsonMapper;

	@PostConstruct
	private void initJsonMapper() {
		jsonMapper = new ObjectMapper();
		jsonMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.registerSubtypes(LogEntry.class);
	}

	// @PostConstruct
	// protected void initEventMapping() {
	// Event dummyEvent = new Event();
	// LogEntry entry = new LogEntry();
	// entry.setTimeStamp(new Date());
	// dummyEvent.setEntries(Collections.singletonList((LogEntryData) entry));
	// dummyEvent.setPublished(new Date());
	// String id = persist(dummyEvent);
	// delete(new String[] { id });
	// logger.debug("Initiated basic event index mapping");
	// }

	/**
	 * Returns the sniffer related id.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return sniffer related id
	 */
	public static String getSnifferIdAsType(final long snifferId) {
		return "event_" + snifferId;
	}

	@Override
	public String persist(final Event event) {
		// long nextEventId = jTpl
		// .queryForLong("SELECT NEXTVAL('EVENTS_SEQUENCE')");

		try {
			final String evStr = jsonMapper.writeValueAsString(event);
			final IndexRequest indexRequest = Requests.indexRequest(indexName)
					.type(getSnifferIdAsType(event.getSnifferId())).source(evStr);
			final String eventId = clientTpl.executeWithClient(new ClientCallback<IndexResponse>() {
				@Override
				public IndexResponse execute(final Client client) {
					return client.index(indexRequest).actionGet();
				}
			}).getId();
			logger.debug("Persisted event with id: {}", eventId);
			return eventId;
		} catch (final Exception e) {
			throw new DataAccessException("Failed to persiste event: " + event.getId(), e);
		}
	}

	@Override
	public void delete(final long snifferId, final String[] eventIds) {
		clientTpl.executeWithClient(new ClientCallback<Object>() {
			@Override
			public Object execute(final Client client) {
				final BulkRequest deletes = new BulkRequest();
				for (final String id : eventIds) {
					deletes.add(new DeleteRequest(indexName, getSnifferIdAsType(snifferId), id));
				}
				client.bulk(deletes).actionGet();
				logger.info("Deleted events: {}", (Object[]) eventIds);
				return null;
			}
		});
	}

	@Override
	public void deleteAll(final long snifferId) {
		clientTpl.executeWithClient(new ClientCallback<Object>() {
			@Override
			public Object execute(final Client client) {
				logger.debug("Going to delete all events for sniffer: {}", snifferId);
				try {
					client.admin().indices().prepareDeleteMapping(indexName).setType(getSnifferIdAsType(snifferId))
							.execute().actionGet();
				} catch (final TypeMissingException e) {
					logger.info("Catched TypeMissingException when deleting all events of sniffer: {}", snifferId);
				}
				logger.info("Deleted all events for sniffer: {}", snifferId);
				return null;
			}
		});
		prepareMapping(snifferId);
	}

	private abstract class EsBaseEventsNativeQueryBuilder<BuilderType extends BaseEventQueryBuilder<?>>
			implements BaseEventQueryBuilder<BuilderType> {
		private int maxHistogramIntervalSlots = -1;
		protected final long snifferId;
		private final int offset, size;

		/**
		 * @param snifferId
		 */
		public EsBaseEventsNativeQueryBuilder(final long snifferId, final int offset, final int size) {
			super();
			this.snifferId = snifferId;
			this.offset = offset;
			this.size = size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public BuilderType withEventCountTimeHistogram(final int maxHistogramIntervalSlots) {
			this.maxHistogramIntervalSlots = maxHistogramIntervalSlots;
			return (BuilderType) this;
		}

		@Override
		public EventsResult list() {
			return clientTpl.executeWithClient(new ClientCallback<EventsResult>() {
				@Override
				public EventsResult execute(final Client client) {
					return list(client);
				}
			});
		}

		protected SearchRequestBuilder getBaseRequestBuilder(final Client esClient) {
			final SearchRequestBuilder requestBuilder = esClient.prepareSearch(indexName)
					.setTypes(getSnifferIdAsType(snifferId));
			requestBuilder.setFrom(offset).setSize(size)
					.addSort(SortBuilders.fieldSort("occurrence").order(SortOrder.ASC).ignoreUnmapped(true));
			return requestBuilder;
		}

		protected abstract SearchRequestBuilder adaptRequestBuilder(final Client esClient,
				final SearchRequestBuilder requestBuilder);

		private EventsResult list(final Client esClient) {
			final long start = System.currentTimeMillis();
			SearchRequestBuilder requestBuilder = getBaseRequestBuilder(esClient);
			requestBuilder = adaptRequestBuilder(esClient, requestBuilder);
			EventsCountHistogram histogram = null;
			if (maxHistogramIntervalSlots > 0) {
				final StatsBuilder timeRangeAgg = AggregationBuilders.stats("timeRange").field("occurrence");
				final SearchRequestBuilder timeRangeQuery = adaptRequestBuilder(esClient,
						getBaseRequestBuilder(esClient).setSize(0).addAggregation(timeRangeAgg));
				try {
					final Stats timeRangeStats = timeRangeQuery.execute().actionGet().getAggregations()
							.get("timeRange");
					final long timeRange = (long) (timeRangeStats.getMax() - timeRangeStats.getMin());
					logger.debug("Time range query: {}", timeRangeQuery);
					logger.debug("Retrieved time range for events of sniffer={} in {}ms: {}", snifferId,
							System.currentTimeMillis() - start, timeRange);
					histogram = new EventsCountHistogram();
					final Interval interval = getInterval(timeRange, maxHistogramIntervalSlots, histogram);
					requestBuilder.addAggregation(AggregationBuilders.dateHistogram("eventsCount").interval(interval)
							.field("occurrence").order(Order.KEY_ASC));
				} catch (final SearchPhaseExecutionException e) {
					logger.warn("Events histogram disabled because of exceptions (probably no events?)", e);
				}
			}
			final SearchResponse response = requestBuilder.execute().actionGet();
			final ArrayList<AspectEvent> events = new ArrayList<EventPersistence.AspectEvent>();
			for (final SearchHit h : response.getHits().getHits()) {
				try {
					final AspectEventImpl event = jsonMapper.readValue(h.getSourceAsString(), AspectEventImpl.class);
					event.setId(h.getId());
					events.add(event);
				} catch (final Exception e) {
					throw new DataAccessException("Failed to decode event: " + h.getSourceAsString(), e);
				}
			}
			if (histogram != null) {
				histogram.setEntries(new ArrayList<EventPersistence.HistogramEntry>());
				for (final DateHistogram.Bucket e : ((DateHistogram) response.getAggregations().get("eventsCount"))
						.getBuckets()) {
					histogram.getEntries().add(new HistogramEntry(e.getKeyAsNumber().longValue(), e.getDocCount()));
				}
			}
			logger.debug("Retrieved events for sniffer={} in {}ms with query: {}", snifferId,
					System.currentTimeMillis() - start, requestBuilder);
			return new EventsResult(response.getHits().totalHits(), events, histogram);
		}
	}

	private class EsEventsNativeQueryBuilder extends EsBaseEventsNativeQueryBuilder<NativeQueryBuilder>
			implements NativeQueryBuilder {
		private String nativeQuery;

		public EsEventsNativeQueryBuilder(final long snifferId, final int offset, final int size) {
			super(snifferId, offset, size);
		}

		@Override
		public NativeQueryBuilder withNativeQuery(final String nativeQuery) {
			this.nativeQuery = nativeQuery;
			return this;
		}

		@Override
		protected SearchRequestBuilder adaptRequestBuilder(final Client esClient,
				final SearchRequestBuilder requestBuilder) {
			return requestBuilder.setExtraSource(nativeQuery);
		}
	}

	private class EsEventQueryBuilder extends EsBaseEventsNativeQueryBuilder<EventQueryBuilder>
			implements EventQueryBuilder {

		public EsEventQueryBuilder(final long snifferId, final int offset, final int size) {
			super(snifferId, offset, size);
		}

		private Date from;
		private Date to;

		@Override
		public EventQueryBuilder withEntryFieldsMapAspect(final FieldsProjection[] fields,
				final EntriesJoinType joinType) {
			// Not supported
			return this;
		}

		@Override
		protected SearchRequestBuilder adaptRequestBuilder(final Client esClient,
				final SearchRequestBuilder requestBuilder) {
			FilterBuilder filter = null;
			if (from != null || to != null) {
				final RangeFilterBuilder occRange = FilterBuilders.rangeFilter("occurrence");
				if (from != null) {
					occRange.gte(from.getTime());
				}
				if (to != null) {
					occRange.lte(to.getTime());
				}
				filter = occRange;
			}
			if (filter != null) {
				requestBuilder.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter));
			}
			return requestBuilder;
		}

		@Override
		public EventQueryBuilder withOccurrenceFrom(final Date from) {
			this.from = from;
			return this;
		}

		@Override
		public EventQueryBuilder withOccurrenceTo(final Date to) {
			this.to = to;
			return this;
		}

		@Override
		public EventQueryBuilder sortByEntryTimestamp(final boolean desc) {
			// TODO Auto-generated method stub
			return this;
		}
	}

	@Override
	public EventQueryBuilder getEventsQueryBuilder(final long snifferId, final long offset, final int limit) {
		return new EsEventQueryBuilder(snifferId, (int) offset, limit);
	}

	@Override
	public NativeQueryBuilder getEventsNativeQueryBuilder(final long snifferId, final long offset, final int limit) {
		return new EsEventsNativeQueryBuilder(snifferId, (int) offset, limit);
	}

	protected Interval getInterval(final long timeRange, final int maxSlotsCount,
			final EventsCountHistogram histogram) {
		// year, quarter, month, week, day, hour, minute, second
		long dif = timeRange / maxSlotsCount / 1000;
		if (dif <= 0) {
			histogram.setInterval(HistogramInterval.SECOND);
			return Interval.SECOND;
		} else if (dif < 60) {
			histogram.setInterval(HistogramInterval.MINUTE);
			return Interval.MINUTE;
		} else if ((dif = dif / 60) < 60) {
			histogram.setInterval(HistogramInterval.HOUR);
			return Interval.HOUR;
		} else if ((dif = dif / 60) < 24) {
			histogram.setInterval(HistogramInterval.DAY);
			return Interval.DAY;
		} else if ((dif = dif / 24) < 7) {
			histogram.setInterval(HistogramInterval.WEEK);
			return Interval.WEEK;
		} else if ((dif = dif / 7) < 4) {
			histogram.setInterval(HistogramInterval.MONTH);
			return Interval.MONTH;
		}
		histogram.setInterval(HistogramInterval.YEAR);
		return Interval.YEAR;
	}

	@Override
	public Event getEvent(final long snifferId, final String eventId) {
		return clientTpl.executeWithClient(new ClientCallback<Event>() {
			@Override
			public Event execute(final Client client) {
				try {
					final GetResponse r = client.prepareGet(indexName, getSnifferIdAsType(snifferId), eventId).execute()
							.get();
					if (r != null && r.isExists()) {
						final Event event = jsonMapper.readValue(r.getSourceAsString(), Event.class);
						event.setId(r.getId());
						return event;
					} else {
						return null;
					}
				} catch (final Exception e) {
					throw new DataAccessException("Failed to load for sniffer=" + snifferId + " the event: " + eventId,
							e);
				}
			}
		});
	}

	public static class AspectEventImpl extends Event implements AspectEvent {
		@JsonIgnore
		private final HashMap<String, Object> aspects = new HashMap<String, Object>();

		@Override
		public <AspectType> void setAspect(final String aspectKey, final AspectType aspect) {
			aspects.put(aspectKey, aspect);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <AspectType> AspectType getAspect(final String aspectKey, final Class<AspectType> aspectType) {
			return (AspectType) aspects.get(aspectKey);
		}
	}

	@Override
	public AspectProvider<AspectSniffer, Integer> getEventsCounter() {
		return new PostAspectProvider<SnifferPersistence.AspectSniffer, Integer>() {

			@Override
			public Integer getApsect(final AspectSniffer host) {
				return host.getAspect(EVENTS_COUNT, Integer.class);
			}

			@Override
			public void injectAspect(final List<AspectSniffer> hosts) {
				final long start = System.currentTimeMillis();
				final HashMap<String, AspectSniffer> mapHosts = new HashMap<String, SnifferPersistence.AspectSniffer>();
				final long[] hostIds = new long[hosts.size()];
				int i = 0;
				for (final AspectSniffer s : hosts) {
					hostIds[i++] = s.getId();
					mapHosts.put(Long.toString(s.getId()), s);
					s.setAspect(EVENTS_COUNT, 0);
				}
				final TermsFacet countersFacet = clientTpl.executeWithClient(new ClientCallback<TermsFacet>() {
					@Override
					public TermsFacet execute(final Client client) {
						final SearchRequestBuilder requestBuilder = client.prepareSearch(indexName).setSize(0)
								.addFacet(FacetBuilders.termsFacet("eventsCounter").allTerms(true).field("snifferId")
										.facetFilter(FilterBuilders.termsFilter("snifferId", hostIds)));
						final SearchResponse response = requestBuilder.execute().actionGet();
						logger.debug("Performed events counting search {} in {}ms", requestBuilder,
								System.currentTimeMillis() - start);
						return response.getFacets().facet(TermsFacet.class, "eventsCounter");
					}
				});

				for (final org.elasticsearch.search.facet.terms.TermsFacet.Entry e : countersFacet.getEntries()) {
					final String id = e.getTerm().string();
					if (mapHosts.containsKey(id)) {
						mapHosts.get(id).setAspect(EVENTS_COUNT, e.getCount());
					}
				}

			}
		};
	}

	@EventListener
	public void handleOrderCreatedEvent(final SnifferChangedEvent event) {
		prepareMapping(event.getSniffer().getId());
	}

	private void prepareMapping(final long snifferId) {
		logger.info("Rebuilding mapping for sniffer {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		sniffer.getLogSourceId();
		final LinkedHashMap<String, FieldBaseTypes> snifferTypes = new LinkedHashMap<>();

		final LogSource<LogInputStream> source = logSourceProvider.getSourceById(sniffer.getLogSourceId());
		final LinkedHashMap<String, FieldBaseTypes> entriesTypes = new LinkedHashMap<>();
		try {
			entriesTypes.putAll(source.getReader().getFieldTypes());
		} catch (final FormatException e) {
			logger.warn("Failed to access entries fields, these won't be considered", e);
		}

		try {
			clientTpl.executeWithClient(new ClientCallback<Object>() {
				@Override
				public Object execute(final Client client) {
					final StringWriter jsonMapping = new StringWriter();
					final JSONBuilder mappingBuilder = new JSONBuilder(jsonMapping).object();
					final JSONBuilder props = mappingBuilder.key(getSnifferIdAsType(snifferId)).object()
							.key("properties").object();

					// TODO: Map sniffer fields dynamically
					props.key("occurrence").object().key("type").value("date").endObject();
					props.key("published").object().key("type").value("date").endObject();

					for (final String key : entriesTypes.keySet()) {
						mapField(props, "entries.fields." + key, entriesTypes.get(key));
					}

					mappingBuilder.endObject().endObject().endObject();
					logger.info("Creating mapping for sniffer {}: {}", snifferId, jsonMapping);
					client.admin().indices().preparePutMapping(indexName).setType(getSnifferIdAsType(snifferId))
							.setSource(jsonMapping.toString()).get();
					return null;
				}
			});
		} catch (final Exception e) {
			logger.warn("Failed to update mapping for sniffer " + snifferId + ", try to delete all events", e);
		}
	}

	private void mapField(final JSONBuilder props, final String path, final FieldBaseTypes type) {
		if (type == FieldBaseTypes.DATE) {
			props.key(path).object().key("type").value("date").endObject();
		}
	}
}
