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
package com.logsniffer.settings.http;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.Configured;

/**
 * Settings bean for HTTP related functions.
 * 
 * @author mbok
 * 
 */
@Component
public class HttpSettings {
	public static final String PROP_HTTP_PROXY_PASSWORD = "http.proxyPassword";

	public static final String PROP_HTTP_PROXY_USER = "http.proxyUser";

	public static final String PROP_HTTP_PROXY_PORT = "http.proxyPort";

	public static final String PROP_HTTP_PROXY_SCHEMA = "http.proxySchema";

	public static final String PROP_HTTP_PROXY_HOST = "http.proxyHost";

	private HttpProxy httpProxy;

	@Configured(PROP_HTTP_PROXY_HOST)
	private ConfigValue<String> proxyHost;

	@Configured(value = PROP_HTTP_PROXY_SCHEMA, defaultValue = "http")
	private ConfigValue<String> proxySchema;

	@Configured(value = PROP_HTTP_PROXY_PORT, defaultValue = "8080")
	private ConfigValue<Integer> proxyPort;

	@Configured(value = PROP_HTTP_PROXY_USER)
	private ConfigValue<String> proxyUser;

	@Configured(value = PROP_HTTP_PROXY_PASSWORD)
	private ConfigValue<String> proxyPassword;

	@PostConstruct
	public void refreshProxySettings() {
		HttpProxy proxy = new HttpProxy();
		proxy.setHost(proxyHost.get());
		proxy.setPort(proxyPort.get());
		proxy.setSchema(proxySchema.get());
		proxy.setUser(proxyUser.get());
		proxy.setPassword(proxyPassword.get());
		setHttpProxy(proxy);
	}

	/**
	 * @return the httpProxy
	 */
	public HttpProxy getHttpProxy() {
		return httpProxy;
	}

	/**
	 * @param httpProxy
	 *            the httpProxy to set
	 */
	public void setHttpProxy(final HttpProxy httpProxy) {
		this.httpProxy = httpProxy;
	}

	/**
	 * Creates a {@link HttpClientBuilder} considering current settings.
	 * 
	 * @return a {@link HttpClientBuilder} considering current settings.
	 */
	public HttpClientBuilder createHttpClientBuilder() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		if (getHttpProxy() != null
				&& StringUtils.isNotBlank(getHttpProxy().getHost())) {
			httpClientBuilder = httpClientBuilder.setProxy(new HttpHost(
					getHttpProxy().getHost(), getHttpProxy().getPort(),
					getHttpProxy().getSchema()));
			if (StringUtils.isNotBlank(getHttpProxy().getUser())) {
				Credentials credentials = new UsernamePasswordCredentials(
						getHttpProxy().getUser(), getHttpProxy().getPassword());
				AuthScope authScope = new AuthScope(getHttpProxy().getHost(),
						getHttpProxy().getPort());
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(authScope, credentials);
				httpClientBuilder = httpClientBuilder
						.setDefaultCredentialsProvider(credsProvider);
				httpClientBuilder.useSystemProperties();
			}
		}
		return httpClientBuilder;
	}
}
