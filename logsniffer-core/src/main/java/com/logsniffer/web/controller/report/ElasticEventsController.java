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
package com.logsniffer.web.controller.report;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.app.ElasticSearchAppConfig.ClientCallback;
import com.logsniffer.app.ElasticSearchAppConfig.ElasticClientTemplate;

/**
 * Provides searching for events persisted in the Elasticsearch index.
 * 
 * @author mbok
 * 
 */
@RestController
public class ElasticEventsController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value(value = "${logsniffer.es.indexName}")
	private String indexName;

	@Autowired
	private ElasticClientTemplate clientTpl;

	@RequestMapping(value = "/reports/eventSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void eventSearch(final HttpEntity<String> httpEntity,
			final HttpServletResponse response) throws IOException {
		long start = System.currentTimeMillis();
		String jsonRequest = httpEntity.getBody();
		final SearchRequest searchRequest = new SearchRequest(indexName);
		try {
			searchRequest.source(jsonRequest);
			searchRequest.types("event");
			SearchResponse r = clientTpl
					.executeWithClient(new ClientCallback<SearchResponse>() {
						@Override
						public SearchResponse execute(final Client client) {
							return client.search(searchRequest).actionGet();
						}
					});
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			OutputStream responseStream = response.getOutputStream();
			XContentBuilder builder = XContentFactory
					.jsonBuilder(responseStream);
			builder.startObject();
			r.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			builder.close();
			responseStream.close();
		} finally {
			logger.debug("Executed search in {}ms: {}",
					System.currentTimeMillis() - start, jsonRequest);
		}
	}
}
