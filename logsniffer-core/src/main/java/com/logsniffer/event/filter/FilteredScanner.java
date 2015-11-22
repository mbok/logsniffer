package com.logsniffer.event.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.WrappedBean;
import com.logsniffer.event.Event;
import com.logsniffer.event.IncrementData;
import com.logsniffer.event.LogEntryReaderStrategy;
import com.logsniffer.event.Scanner;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsHost;
import com.logsniffer.fields.filter.FieldsFilter;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

public class FilteredScanner implements Scanner {
	@JsonProperty
	@Valid
	private List<FieldsFilter> filters = new ArrayList<>();

	@JsonProperty
	@Valid
	private Scanner targetScanner;

	public FilteredScanner() {
		super();
	}

	public FilteredScanner(final Scanner targetScanner, final FieldsFilter... filters) {
		super();
		this.targetScanner = targetScanner;
		for (final FieldsFilter f : filters) {
			this.filters.add(f);
		}
	}

	@Override
	public void find(final LogEntryReader<LogInputStream> reader, final LogEntryReaderStrategy readerStrategy,
			final Log log, final LogRawAccess<LogInputStream> logAccess, final IncrementData incrementData,
			final EventConsumer eventConsumer) throws IOException, FormatException {
		targetScanner.find(reader, readerStrategy, log, logAccess, incrementData, new EventConsumer() {
			@Override
			public void consume(final Event eventData) throws IOException {
				filter(eventData);
				eventConsumer.consume(eventData);
			}
		});

	}

	private void filter(final Event event) {
		for (final FieldsFilter f : filters) {
			f.filter(event);
		}
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return FieldsHost.FieldHostUtils.getFilteredFieldTypes(targetScanner, filters);
	}

	/**
	 * @return the targetScanner
	 */
	public Scanner getTargetScanner() {
		return targetScanner;
	}

	/**
	 * @param targetScanner
	 *            the targetScanner to set
	 */
	public void setTargetScanner(final Scanner targetScanner) {
		this.targetScanner = targetScanner;
	}

	/**
	 * @return the filters
	 */
	public List<FieldsFilter> getFilters() {
		return filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(final List<FieldsFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Wrapper for delegated filtered scanner e.g. to allow lazy initiation.
	 * 
	 * @author mbok
	 */
	public static abstract class FilteredScannerWrapper extends FilteredScanner
			implements WrappedBean<FilteredScanner> {
		private FilteredScanner wrapped;

		public static final FilteredScanner unwrap(final FilteredScanner possiblyWrapped) {
			if (possiblyWrapped instanceof FilteredScannerWrapper) {
				return ((FilteredScannerWrapper) possiblyWrapped).getWrappedScanner();
			}
			return possiblyWrapped;
		}

		public final FilteredScanner getWrappedScanner() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void find(final LogEntryReader<LogInputStream> reader, final LogEntryReaderStrategy readerStrategy,
				final Log log, final LogRawAccess<LogInputStream> logAccess, final IncrementData incrementData,
				final EventConsumer eventConsumer) throws IOException, FormatException {
			try {
				getWrappedScanner().find(reader, readerStrategy, log, logAccess, incrementData, eventConsumer);
			} catch (final ConfigException e) {
				throw new IOException("Failed to create configured scanner", e);
			}
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			try {
				return getWrappedScanner().getFieldTypes();
			} catch (final ConfigException e) {
				throw new FormatException("Failed to create configured scanner", e);
			}
		}

		@Override
		public Scanner getTargetScanner() {
			return getWrappedScanner().getTargetScanner();
		}

		@Override
		public void setTargetScanner(final Scanner targetScanner) {
			getWrappedScanner().setTargetScanner(targetScanner);
		}

		@Override
		public List<FieldsFilter> getFilters() {
			return getWrappedScanner().getFilters();
		}

		@Override
		public void setFilters(final List<FieldsFilter> filters) {
			getWrappedScanner().setFilters(filters);
		}

	}
}
