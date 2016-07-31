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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Order;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import com.logsniffer.event.es.EsEventPersistence.AspectEventImpl.AspectEventImplTypeSafeDeserializer;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.support.JsonLogPointer;
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

	@Autowired
	private IndexNamingStrategy indexNamingStrategy;

	private ObjectMapper jsonMapper;

	@PostConstruct
	private void initJsonMapper() {
		jsonMapper = new ObjectMapper();
		jsonMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.registerSubtypes(LogEntry.class);
		final SimpleModule esModule = new SimpleModule();
		esModule.addSerializer(LogPointer.class, new EsLogPointerSerializer());
		esModule.addDeserializer(LogPointer.class, new EsLogPointerDeserializer());
		esModule.addDeserializer(JsonLogPointer.class, new EsLogPointerDeserializer());
		jsonMapper.registerModule(esModule);
	}

	/**
	 * Returns the type for events.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return type
	 */
	public static String getType(final long snifferId) {
		return "event";
	}

	@Override
	public String persist(final Event event) {
		String evStr = null;
		try {
			evStr = jsonMapper.writeValueAsString(event);
			final IndexRequest indexRequest = Requests
					.indexRequest(indexNamingStrategy.buildActiveName(event.getSnifferId()))
					.type(getType(event.getSnifferId())).source(evStr);
			final String eventId = clientTpl.executeWithClient(new ClientCallback<IndexResponse>() {
				@Override
				public IndexResponse execute(final Client client) {
					return client.index(indexRequest).actionGet();
				}
			}).getId();
			logger.debug("Persisted event with id: {}", eventId);
			return eventId;
		} catch (final Exception e) {
			throw new DataAccessException("Failed to persiste event: " + evStr, e);
		}
	}

	@Override
	public void delete(final long snifferId, final String[] eventIds) {
		clientTpl.executeWithClient(new ClientCallback<Object>() {
			@Override
			public Object execute(final Client client) {
				final BulkRequest deletes = new BulkRequest().refresh(true);
				for (final String id : eventIds) {
					for (final String index : indexNamingStrategy.getRetrievalNames(snifferId)) {
						deletes.add(new DeleteRequest(index, getType(snifferId), id));
					}
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
				final String[] indexNames = indexNamingStrategy.getRetrievalNames(snifferId);
				logger.debug("Going to delete all events for sniffer {} by deleting the index(es): {}", snifferId,
						indexNames);
				try {
					client.admin().indices().prepareDelete(indexNames)
							.setIndicesOptions(IndicesOptions.lenientExpandOpen()).execute().actionGet();
				} catch (final IndexNotFoundException e) {
					logger.info("Catched IndexNotFoundException when deleting all events of sniffer: {}", snifferId);
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
			final SearchRequestBuilder requestBuilder = esClient
					.prepareSearch(indexNamingStrategy.getRetrievalNames(snifferId))
					.setIndicesOptions(IndicesOptions.lenientExpandOpen()).setTypes(getType(snifferId));
			requestBuilder.setFrom(offset).setSize(size)
					.addSort(SortBuilders.fieldSort(Event.FIELD_TIMESTAMP).order(SortOrder.ASC).unmappedType("date"));
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
				final StatsBuilder timeRangeAgg = AggregationBuilders.stats("timeRange").field(Event.FIELD_TIMESTAMP);
				final SearchRequestBuilder timeRangeQuery = adaptRequestBuilder(esClient,
						getBaseRequestBuilder(esClient).setSize(0).addAggregation(timeRangeAgg));
				try {
					final Aggregations aggregations = timeRangeQuery.execute().actionGet().getAggregations();
					if (aggregations != null) {
						final Stats timeRangeStats = aggregations.get("timeRange");
						final long timeRange = (long) (timeRangeStats.getMax() - timeRangeStats.getMin());
						logger.debug("Time range query: {}", timeRangeQuery);
						logger.debug("Retrieved time range for events of sniffer={} in {}ms: {}", snifferId,
								System.currentTimeMillis() - start, timeRange);
						histogram = new EventsCountHistogram();
						final DateHistogramInterval interval = getInterval(timeRange, maxHistogramIntervalSlots,
								histogram);
						requestBuilder.addAggregation(AggregationBuilders.dateHistogram("eventsCount")
								.interval(interval).field(Event.FIELD_TIMESTAMP).order(Order.KEY_ASC));
					}
				} catch (final SearchPhaseExecutionException e) {
					logger.warn("Events histogram disabled because of exceptions (probably no events?)", e);
				}
			}
			final SearchResponse response = requestBuilder.execute().actionGet();
			final List<EventPersistence.AspectEvent> events = new ArrayList<>();
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
				if (response.getAggregations() != null) {
					for (final Bucket e : ((Histogram) response.getAggregations().get("eventsCount")).getBuckets()) {
						final DateTime key = (DateTime) e.getKey();
						histogram.getEntries().add(new HistogramEntry(key.getMillis(), e.getDocCount()));
					}
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
		protected SearchRequestBuilder adaptRequestBuilder(final Client esClient,
				final SearchRequestBuilder requestBuilder) {
			QueryBuilder filter = null;
			if (from != null || to != null) {
				final RangeQueryBuilder occRange = QueryBuilders.rangeQuery(Event.FIELD_TIMESTAMP);
				if (from != null) {
					occRange.gte(from.getTime());
				}
				if (to != null) {
					occRange.lte(to.getTime());
				}
				filter = occRange;
			}
			if (filter != null) {
				requestBuilder.setQuery(filter);
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

	protected DateHistogramInterval getInterval(final long timeRange, final int maxSlotsCount,
			final EventsCountHistogram histogram) {
		// year, quarter, month, week, day, hour, minute, second
		long dif = timeRange / maxSlotsCount / 1000;
		if (dif <= 0) {
			histogram.setInterval(HistogramInterval.SECOND);
			return DateHistogramInterval.SECOND;
		} else if (dif < 60) {
			histogram.setInterval(HistogramInterval.MINUTE);
			return DateHistogramInterval.MINUTE;
		} else if ((dif = dif / 60) < 60) {
			histogram.setInterval(HistogramInterval.HOUR);
			return DateHistogramInterval.HOUR;
		} else if ((dif = dif / 60) < 24) {
			histogram.setInterval(HistogramInterval.DAY);
			return DateHistogramInterval.DAY;
		} else if ((dif = dif / 24) < 7) {
			histogram.setInterval(HistogramInterval.WEEK);
			return DateHistogramInterval.WEEK;
		} else if ((dif = dif / 7) < 4) {
			histogram.setInterval(HistogramInterval.MONTH);
			return DateHistogramInterval.MONTH;
		}
		histogram.setInterval(HistogramInterval.YEAR);
		return DateHistogramInterval.YEAR;
	}

	@Override
	public Event getEvent(final long snifferId, final String eventId) {
		return clientTpl.executeWithClient(new ClientCallback<Event>() {
			@Override
			public Event execute(final Client client) {
				try {
					final SearchResponse r = client.prepareSearch(indexNamingStrategy.getRetrievalNames(snifferId))
							.setIndicesOptions(IndicesOptions.lenientExpandOpen())
							.setQuery(QueryBuilders.idsQuery().ids(eventId)).execute().get();
					if (r != null && r.getHits().hits().length > 0) {
						final SearchHit hit = r.getHits().hits()[0];
						final Event event = jsonMapper.readValue(hit.getSourceAsString(), Event.class);
						event.setId(hit.getId());
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

	@JsonDeserialize(using = AspectEventImplTypeSafeDeserializer.class)
	public static class AspectEventImpl extends Event implements EventPersistence.AspectEvent {
		private static final long serialVersionUID = 255582842708979089L;
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

		/**
		 * Type safe deserializer for {@link AspectEventImpl}s.
		 * 
		 * @author mbok
		 *
		 */
		public static class AspectEventImplTypeSafeDeserializer extends FieldsMapTypeSafeDeserializer {

			@Override
			protected FieldsMap create() {
				return new AspectEventImpl();
			}

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
				final HashMap<Long, AspectSniffer> mapHosts = new HashMap<>();
				final long[] hostIds = new long[hosts.size()];
				int i = 0;
				for (final AspectSniffer s : hosts) {
					hostIds[i++] = s.getId();
					mapHosts.put(s.getId(), s);
					s.setAspect(EVENTS_COUNT, 0);
				}
				clientTpl.executeWithClient(new ClientCallback<Object>() {
					@Override
					public Object execute(final Client client) {
						final TermsBuilder terms = AggregationBuilders.terms("eventsCounter")
								.field(Event.FIELD_SNIFFER_ID).include(hostIds).size(hostIds.length);
						final SearchRequestBuilder requestBuilder = client.prepareSearch().setSize(0)
								.addAggregation(terms);
						final SearchResponse response = requestBuilder.execute().actionGet();
						logger.debug("Performed events counting search {} in {}ms", requestBuilder,
								System.currentTimeMillis() - start);
						final Terms aventsCounterAgg = response.getAggregations() != null
								? (Terms) response.getAggregations().get("eventsCounter") : null;
						if (aventsCounterAgg != null) {
							for (final Terms.Bucket entry : aventsCounterAgg.getBuckets()) {
								final long snifferId = entry.getKeyAsNumber().longValue();
								if (mapHosts.containsKey(snifferId)) {
									mapHosts.get(snifferId).setAspect(EVENTS_COUNT, entry.getDocCount());
								}
							}
						}
						return null;
					}
				});
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
		if (sniffer == null) {
			logger.info("Skip rebuilding mapping due to no more existing sniffer: {}", snifferId);
			return;
		}
		final LinkedHashMap<String, FieldBaseTypes> snifferTypes = new LinkedHashMap<>();

		final LogSource<?> source = logSourceProvider.getSourceById(sniffer.getLogSourceId());
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
					final JSONBuilder props = mappingBuilder.key(getType(snifferId)).object().key("properties")
							.object();

					// TODO: Map sniffer fields dynamically
					props.key(Event.FIELD_TIMESTAMP).object().key("type").value("date").endObject();
					props.key(Event.FIELD_PUBLISHED).object().key("type").value("date").endObject();

					for (final String key : entriesTypes.keySet()) {
						mapField(props, Event.FIELD_ENTRIES + "." + key, entriesTypes.get(key));
					}

					mappingBuilder.endObject().endObject().endObject();
					logger.info("Creating mapping for sniffer {}: {}", snifferId, jsonMapping);
					client.admin().indices().preparePutMapping(indexNamingStrategy.buildActiveName(snifferId))
							.setType(getType(snifferId)).setSource(jsonMapping.toString()).get();
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
