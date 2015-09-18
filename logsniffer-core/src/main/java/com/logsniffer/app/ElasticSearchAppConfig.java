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

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Elasticsearch app config.
 * 
 * @author mbok
 * 
 */
@Configuration
public class ElasticSearchAppConfig {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private LogSnifferHome logSnifferHome;

	@Value(value = "${logsniffer.es.indexName}")
	private String indexName;

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
	public static class ElasticClientTemplate {
		private Node node;

		public ElasticClientTemplate(final Node node) {
			super();
			this.node = node;
		}

		public <T> T executeWithClient(final ClientCallback<T> callback) {
			Client c = node.client();
			try {
				return callback.execute(c);
			} finally {
				c.close();
			}
		}
	}

	@Bean(destroyMethod = "close")
	public Node esNode() {
		File esDataDir = new File(logSnifferHome.getHomeDir(), "elasticsearch");
		logger.info("Preparing local elasticsearch node on data path: {}",
				esDataDir.getPath());
		esDataDir.mkdirs();
		ImmutableSettings.Builder settings = ImmutableSettings
				.settingsBuilder();
		settings.put("node.name", "embedded");
		settings.put("path.data", esDataDir.getPath());
		settings.put("http.enabled", false);
		Node node = NodeBuilder.nodeBuilder().settings(settings)
				.clusterName("embedded").data(true).local(true).node();
		Client client = null;
		try {
			client = node.client();
			// We wait now for the yellow (or green) status
			client.admin().cluster().prepareHealth().setWaitForYellowStatus()
					.execute().actionGet();
			if (!client.admin().indices()
					.exists(new IndicesExistsRequest(indexName)).actionGet()
					.isExists()) {
				logger.info("Created elasticsearch index: {}", indexName);
				client.admin().indices()
						.create(new CreateIndexRequest(indexName)).actionGet();
			}
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return node;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public ElasticClientTemplate clientTemplate(final Node node) {
		return new ElasticClientTemplate(node);
	}
}
