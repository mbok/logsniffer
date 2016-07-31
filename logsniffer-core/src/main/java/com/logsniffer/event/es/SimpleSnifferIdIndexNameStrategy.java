package com.logsniffer.event.es;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This strategy introduces persisting events to sniffer isolated indexes and
 * coexists with previous versions of logsniffer where all events were stored to
 * a sengle index. Active index name is build by adding to the
 * {@link SimpleSnifferIdIndexNameStrategy#indexNamePrefix} the sniffer id.
 * 
 * @author mbok
 *
 */
@Component
public class SimpleSnifferIdIndexNameStrategy implements IndexNamingStrategy {
	static final String PROP_ES_INDEX_NAME_PREFIX = "logsniffer.es.indexNamePrefix";
	@Deprecated
	static final String PROP_LEGACY_ES_INDEX_NAME = "logsniffer.es.indexName";

	@Value(value = "${" + PROP_ES_INDEX_NAME_PREFIX + "}")
	private String indexNamePrefix;

	@Deprecated
	@Value(value = "${" + PROP_LEGACY_ES_INDEX_NAME + "}")
	private String legacyIndexName;

	private boolean applyLegacyIndexName = false;

	@PostConstruct
	public void init() {
		applyLegacyIndexName = StringUtils.isNotBlank(legacyIndexName);
	}

	@Override
	public String buildActiveName(final long snifferId) {
		return indexNamePrefix + snifferId;
	}

	@Override
	public String[] getRetrievalNames(final long snifferId) {
		if (applyLegacyIndexName) {
			return new String[] { buildActiveName(snifferId), legacyIndexName };
		} else {
			return new String[] { buildActiveName(snifferId) };
		}
	};

}
