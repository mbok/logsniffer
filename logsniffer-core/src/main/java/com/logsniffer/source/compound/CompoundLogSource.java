package com.logsniffer.source.compound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.model.Log;
import com.logsniffer.model.Log.SizeMetric;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.Navigation.NavigationType;
import com.logsniffer.model.support.BaseLogsSource;
import com.logsniffer.model.support.DefaultLog;
import com.logsniffer.reader.filter.FilteredLogEntryReader;
import com.logsniffer.source.compound.CompoundLogSource.ComposedLogSourceProducer;
import com.logsniffer.validators.NotDefaultPrimitiveValue;

/**
 * Composes multiple logs into one ordered by the timestamp field.
 * 
 * @author mbok
 *
 */
@PostConstructed(constructor = ComposedLogSourceProducer.class)
public class CompoundLogSource extends BaseLogsSource<CompoundLogAccess> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompoundLogSource.class);

	/**
	 * Passes the {@link LogSourceProvider} dependency to the source.
	 * 
	 * @author mbok
	 *
	 */
	@Component
	public static class ComposedLogSourceProducer implements BeanPostConstructor<CompoundLogSource> {
		@Autowired
		private LogSourceProvider logSourceProvider;

		@Override
		public void postConstruct(final CompoundLogSource bean, final BeanConfigFactoryManager configManager)
				throws ConfigException {
			bean.logSourceProvider = logSourceProvider;
		}

	}

	/**
	 * Bean used for storing log part references.
	 * 
	 * @author mbok
	 *
	 */
	public static class LogPartBean {
		@NotDefaultPrimitiveValue
		private long sourceId;

		@NotEmpty
		private String logPath;

		/**
		 * @return the sourceId
		 */
		public long getSourceId() {
			return sourceId;
		}

		/**
		 * @param sourceId
		 *            the sourceId to set
		 */
		public void setSourceId(final long sourceId) {
			this.sourceId = sourceId;
		}

		/**
		 * @return the logPath
		 */
		public String getLogPath() {
			return logPath;
		}

		/**
		 * @param logPath
		 *            the logPath to set
		 */
		public void setLogPath(final String logPath) {
			this.logPath = logPath;
		}

		@Override
		public String toString() {
			return "LogPartBean [sourceId=" + sourceId + ", logPath=" + logPath + "]";
		}

	}

	private LogSourceProvider logSourceProvider;

	@JsonProperty
	@Size(min = 1)
	private List<LogPartBean> parts;

	private List<LogInstance> instances;

	public CompoundLogSource() {
		super();
		readerConfigurable = false;
	}

	protected List<LogInstance> getPartInstances() {
		if (instances == null) {
			instances = new ArrayList<>();
			final Map<Long, LogSource<LogRawAccess<? extends LogInputStream>>> logSources = new HashMap<>();
			for (final LogPartBean part : parts) {
				LogSource<LogRawAccess<? extends LogInputStream>> source = logSources.get(part.sourceId);
				if (source == null) {
					source = logSourceProvider.getSourceById(part.sourceId);
					if (source == null) {
						LOGGER.warn(
								"Part log source with id {} not found, it will be excluded in composition for log: {}",
								part.sourceId, getId());
						continue;
					}
					logSources.put(part.sourceId, source);
				}
				try {
					if (StringUtils.isNotBlank(part.getLogPath())) {
						final Log log = source.getLog(part.getLogPath());
						if (log != null) {
							instances.add(new LogInstance(part.sourceId, log, source));
						} else {
							LOGGER.warn(
									"Part log {} in source {} not found, it will be excluded in composition for log: {}",
									part.sourceId, part.getLogPath(), getId());

						}
					} else {
						// Add all
						for (final Log log : source.getLogs()) {
							instances.add(new LogInstance(part.sourceId, log, source));
						}
					}

				} catch (final IOException e) {
					LOGGER.warn("Failed to load part log " + part.logPath + " in source" + part.sourceId
							+ ", it will be excluded", e);
				}
			}
			LOGGER.debug("Resolved for compound source {} the following log parts: {}", this, instances);
		}
		return instances;
	}

	@Override
	public List<Log> getLogs() throws IOException {
		return Collections.singletonList(getLog(null));
	}

	@Override
	public Log getLog(final String path) throws IOException {
		long totalSize = 0;
		long latestModified = 0;
		for (final LogInstance li : getPartInstances()) {
			totalSize += li.getLog().getSize();
			if (li.getLog().getLastModified() > latestModified) {
				latestModified = li.getLog().getLastModified();
			}
		}
		return new DefaultLog(getName(), "default", latestModified, SizeMetric.BYTE, totalSize);
	}

	@Override
	public CompoundLogAccess getLogAccess(final Log log) throws IOException {
		return new CompoundLogAccess(log, getPartInstances());
	}

	/**
	 * @return the parts
	 */
	public List<LogPartBean> getParts() {
		return parts;
	}

	/**
	 * @param parts
	 *            the parts to set
	 */
	public void setParts(final List<LogPartBean> parts) {
		this.parts = parts;
	}

	@Override
	public FilteredLogEntryReader<CompoundLogAccess> getReader() {
		final FilteredLogEntryReader<CompoundLogAccess> reader = super.getReader();
		if (!(reader.getTargetReader() instanceof CompoundLogReader)) {
			reader.setTargetReader(new CompoundLogReader(getPartInstances()));
		}
		return reader;
	}

	@Override
	public NavigationType getNavigationType() {
		return NavigationType.DATE;
	}

	@Override
	public String toString() {
		return "CompoundLogSource [parts=" + parts + ", " + super.toString() + "]";
	}

}
