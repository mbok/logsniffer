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
package com.logsniffer.app;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.ConfigValueStore;
import com.logsniffer.util.value.Configured;

/**
 * Elasticsearch app config.
 * 
 * @author mbok
 * 
 */
@Configuration
@Import(ConfigValueAppConfig.class)
public class ElasticSearchAppConfig {
	private final static Logger logger = LoggerFactory.getLogger(ElasticSearchAppConfig.class);
	@Autowired
	private LogSnifferHome logSnifferHome;

	@Value(value = "${logsniffer.es.indexName}")
	private String indexName;

	private ClientConnection clientConnection;

	private static interface ClientConnection {

		public Client getClient();

		public void close();
	}

	/**
	 * Indicates whether elasticsearch is operated locally as embedded instance
	 * or by connecting to a remote cluster.
	 * 
	 * @author mbok
	 *
	 */
	public enum EsOperatingType {
		EMBEDDED, REMOTE
	}

	/**
	 * Remote address representation.
	 * 
	 * @author mbok
	 *
	 */
	public static final class RemoteAddress {
		@NotEmpty
		private String host;
		@Min(1)
		private int port = 9300;

		/**
		 * @return the host
		 */
		public String getHost() {
			return host;
		}

		/**
		 * @param host
		 *            the host to set
		 */
		public void setHost(final String host) {
			this.host = host;
		}

		/**
		 * @return the port
		 */
		public int getPort() {
			return port;
		}

		/**
		 * @param port
		 *            the port to set
		 */
		public void setPort(final int port) {
			this.port = port;
		}

		@Override
		public String toString() {
			return host + ":" + port;
		}

	}

	/**
	 * Holds settings for elasticsearch.
	 * 
	 * @author mbok
	 *
	 */
	public static interface EsSettingsHolder {
		/**
		 * Returns current es settings.
		 * 
		 * @return current es settings
		 */
		public EsSettings getSettings();

		/**
		 * Stores new es settings and applies it to the elasticsearch
		 * connection.
		 * 
		 * @param settings
		 * @throws IOException
		 */
		public void storeSettings(final EsSettings settings) throws IOException;
	}

	/**
	 * Default settings holder for elasticsearch.
	 * 
	 * @author mbok
	 *
	 */
	private final class DefaultSettingsHolder implements EsSettingsHolder {
		public static final String PROP_ES_OPS_TYPE = "logsniffer.es.operatingType";
		public static final String PROP_ES_REMOTE_ADDRESSES = "logsniffer.es.remoteAddresses";

		@Configured(value = PROP_ES_OPS_TYPE, defaultValue = "EMBEDDED")
		private ConfigValue<EsOperatingType> operatingType;

		private final Pattern ADDRESS_PATTERN = Pattern.compile("\\s*([^:]+):(\\d+)\\s*,?");
		@Configured(value = PROP_ES_REMOTE_ADDRESSES)
		private ConfigValue<String> remoteAddresses;

		@Autowired
		private ConfigValueStore configValueStore;

		private EsSettings settings;

		@Override
		public EsSettings getSettings() {
			if (settings == null) {
				settings = new EsSettings();
				settings.setOperatingType(operatingType.get());
				final List<RemoteAddress> addresses = new ArrayList<>();
				if (settings.getOperatingType() == EsOperatingType.REMOTE) {
					logger.info("Building remote addresses from config: {}", remoteAddresses.get());
					final Matcher m = ADDRESS_PATTERN.matcher(remoteAddresses.get());
					while (m.find()) {
						final RemoteAddress ra = new RemoteAddress();
						ra.setHost(m.group(1));
						ra.setPort(Integer.parseInt(m.group(2)));
						addresses.add(ra);
					}
					logger.info("Built remote addresses from config: {}", addresses);
					settings.setRemoteAddresses(addresses);
				}
			}
			return settings;
		}

		@Override
		public synchronized void storeSettings(final EsSettings settings) throws IOException {
			configValueStore.store(PROP_ES_OPS_TYPE, settings.getOperatingType().toString());
			if (settings.getOperatingType() == EsOperatingType.REMOTE) {
				final StringBuilder addresses = new StringBuilder();
				for (final RemoteAddress a : settings.getRemoteAddresses()) {
					if (addresses.length() > 0) {
						addresses.append(",");
					}
					addresses.append(a.getHost() + ":" + a.getPort());
				}
				configValueStore.store(PROP_ES_REMOTE_ADDRESSES, addresses.toString());
			}
			this.settings = settings;
			closeCurrentClientConnection();
		}
	}

	/**
	 * Bean for elasticsearch settings.
	 * 
	 * @author mbok
	 *
	 */
	public static final class EsSettings {
		@NotNull
		private EsOperatingType operatingType = EsOperatingType.EMBEDDED;

		@Valid
		private List<RemoteAddress> remoteAddresses;

		/**
		 * @return the operatingType
		 */
		public EsOperatingType getOperatingType() {
			return operatingType;
		}

		/**
		 * @param operatingType
		 *            the operatingType to set
		 */
		public void setOperatingType(final EsOperatingType operatingType) {
			this.operatingType = operatingType;
		}

		/**
		 * @return the remoteAddresses
		 */
		public List<RemoteAddress> getRemoteAddresses() {
			return remoteAddresses;
		}

		/**
		 * @param remoteAddresses
		 *            the remoteAddresses to set
		 */
		public void setRemoteAddresses(final List<RemoteAddress> remoteAddresses) {
			this.remoteAddresses = remoteAddresses;
		}

	}

	public static interface EsClientBuilder {
		Client buildFromSettings(EsSettings settings);
	}

	/**
	 * Client callback.
	 * 
	 * @author mbok
	 * 
	 * @param <T>
	 *            return type
	 */
	public static interface ClientCallback<T> {
		/**
		 * Executes callback code using an acquired client, which is closed
		 * safely after the callback.
		 * 
		 * @param client
		 *            acquired client to use
		 * @return return value
		 */
		public T execute(Client client);
	}

	/**
	 * Template helper class to centralize node/client requests.
	 * 
	 * @author mbok
	 * 
	 */
	public interface ElasticClientTemplate {
		public <T> T executeWithClient(final ClientCallback<T> callback);
	}

	private synchronized ClientConnection getClientConnection(final EsSettings settings) {
		if (clientConnection == null) {
			if (settings.getOperatingType() == EsOperatingType.EMBEDDED) {
				final Node localEmbeddedNode = buildLocalEmbeddedNode();
				final Client client = localEmbeddedNode.client();
				clientConnection = new ClientConnection() {
					@Override
					public Client getClient() {
						return client;
					}

					@Override
					public void close() {
						logger.info("Closing local embedded elasticsearch node");
						client.close();
						localEmbeddedNode.close();
					}
				};
			} else {
				logger.info("Establishing remote elasticsearch connection to: {}", settings.getRemoteAddresses());
				final TransportClient client = TransportClient.builder().build();
				for (final RemoteAddress a : settings.getRemoteAddresses()) {
					try {
						client.addTransportAddress(
								new InetSocketTransportAddress(InetAddress.getByName(a.getHost()), a.getPort()));
					} catch (final UnknownHostException e) {
						logger.warn("Failed to resolve ES host, it'll be ignored: " + a.getHost(), e);
					}
				}
				clientConnection = new ClientConnection() {

					@Override
					public Client getClient() {
						return client;
					}

					@Override
					public void close() {
						client.close();
						logger.info("Closing remote elasticsearch connection to: {}", settings.getRemoteAddresses());
					}
				};
			}

			final Client client = clientConnection.getClient();
			client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
			// if (!client.admin().indices().exists(new
			// IndicesExistsRequest(indexName)).actionGet().isExists()) {
			// logger.info("Created elasticsearch index: {}", indexName);
			// client.admin().indices().create(new
			// CreateIndexRequest(indexName)).actionGet();
			// }

		}
		return clientConnection;
	}

	@PreDestroy
	public void closeCurrentClientConnection() {
		if (clientConnection != null) {
			clientConnection.close();
			clientConnection = null;
		}

	}

	private Node buildLocalEmbeddedNode() {
		final File esHomeDir = new File(logSnifferHome.getHomeDir(), "elasticsearch");
		final File esDataDir = new File(esHomeDir, "data");
		logger.info("Preparing local elasticsearch node on data path: {}", esDataDir.getPath());
		esDataDir.mkdirs();
		final Settings settings = Settings.settingsBuilder().put("node.name", "embedded")
				.put("path.home", esHomeDir.getPath()).put("http.enabled", false).build();
		final Node node = NodeBuilder.nodeBuilder().settings(settings).clusterName("embedded").data(true).local(true)
				.node();
		return node;
	}

	/**
	 * Exposes the elasticsearch settings holder.
	 * 
	 * @return elasticsearch settings holder
	 */
	@Bean
	public EsSettingsHolder esSettingsHolder() {
		return new DefaultSettingsHolder();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public ElasticClientTemplate clientTemplate(final EsSettingsHolder settingsHolder) {
		return new ElasticClientTemplate() {
			@Override
			public <T> T executeWithClient(final ClientCallback<T> callback) {
				final Client c = getClientConnection(settingsHolder.getSettings()).getClient();
				return callback.execute(c);
			}
		};
	}
}
