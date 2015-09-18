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
package com.logsniffer.web.controller.settings;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.app.ConfigValueAppConfig;
import com.logsniffer.app.LogSnifferHome;
import com.logsniffer.app.MailAppConfig;
import com.logsniffer.app.MailAppConfig.MailSettings;
import com.logsniffer.settings.http.HttpProxy;
import com.logsniffer.settings.http.HttpSettings;
import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.ConfigValueStore;
import com.logsniffer.util.value.Configured;

@RestController
public class GeneralSettingsResourceController {

	public static class MailSettingsBean {
		private String user;
		private String password;
		@NotNull
		private String host;

		@Min(1)
		private int port;

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
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param user
		 *            the user to set
		 */
		public void setUser(final String user) {
			this.user = user;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(final String password) {
			this.password = password;
		}
	}

	public static class GeneralSettings {
		private String homeDir;
		@NotNull
		@URL
		private String baseUrl;

		@Valid
		private MailSettingsBean mailSettings;

		@Valid
		private HttpProxy httpProxy;

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
		 * @return the mailSettings
		 */
		public MailSettingsBean getMailSettings() {
			return mailSettings;
		}

		/**
		 * @param mailSettings
		 *            the mailSettings to set
		 */
		public void setMailSettings(final MailSettingsBean mailSettings) {
			this.mailSettings = mailSettings;
		}

		/**
		 * @return the homeDir
		 */
		public String getHomeDir() {
			return homeDir;
		}

		/**
		 * @param homeDir
		 *            the homeDir to set
		 */
		private void setHomeDir(final String homeDir) {
			this.homeDir = homeDir;
		}

		/**
		 * @return the baseUrl
		 */
		public String getBaseUrl() {
			return baseUrl;
		}

		/**
		 * @param baseUrl
		 *            the baseUrl to set
		 */
		public void setBaseUrl(final String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GeneralSettingsResourceController.class);

	@Autowired
	private LogSnifferHome home;

	@Autowired
	private ConfigValueStore configStore;

	@Autowired
	private MailAppConfig mailAppConfig;

	@Autowired
	private MailSettings mailSettings;

	@Autowired
	private HttpSettings httpSettings;

	@Configured(ConfigValueAppConfig.LOGSNIFFER_BASE_URL)
	private ConfigValue<String> baseUrl;

	@RequestMapping(value = "/settings/general", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public GeneralSettings getGeneralSettings() {
		GeneralSettings settings = new GeneralSettings();
		settings.setHomeDir(home.getHomeDir().getAbsolutePath());
		settings.setBaseUrl(baseUrl.get());

		MailSettingsBean mailSettingsBean = new MailSettingsBean();
		mailSettingsBean.setHost(mailSettings.getMailHost().get());
		mailSettingsBean.setPort(mailSettings.getMailPort().get());
		mailSettingsBean.setUser(mailSettings.getMailUser().get());
		mailSettingsBean.setPassword(mailSettings.getMailPassword().get());
		settings.setMailSettings(mailSettingsBean);

		settings.setHttpProxy(httpSettings.getHttpProxy());
		return settings;
	}

	@RequestMapping(value = "/settings/general", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public void saveGeneralSettings(
			@RequestBody @Valid final GeneralSettings settings)
			throws IOException {
		configStore.store(ConfigValueAppConfig.LOGSNIFFER_BASE_URL,
				settings.getBaseUrl());

		// Propagate changes to mail sender
		configStore.store(MailSettings.PROP_LOGSNIFFER_MAIL_HOST, settings
				.getMailSettings().getHost());
		configStore.store(MailSettings.PROP_LOGSNIFFER_MAIL_PORT, settings
				.getMailSettings().getPort() + "");
		configStore.store(MailSettings.PROP_LOGSNIFFER_MAIL_USER, settings
				.getMailSettings().getUser());
		configStore.store(MailSettings.PROP_LOGSNIFFER_MAIL_PASSWORD, settings
				.getMailSettings().getPassword());
		LOGGER.info("Propagate mail settings to mail sender");
		mailAppConfig.refreshMailSenderConfiguration();

		// Propagate http proxy settings
		configStore.store(HttpSettings.PROP_HTTP_PROXY_HOST, settings
				.getHttpProxy() != null ? settings.getHttpProxy().getHost()
				: null);
		configStore.store(HttpSettings.PROP_HTTP_PROXY_PORT,
				settings.getHttpProxy() != null ? settings.getHttpProxy()
						.getPort() + "" : null);
		configStore.store(
				HttpSettings.PROP_HTTP_PROXY_USER,
				settings.getHttpProxy() != null
						&& StringUtils.isNotBlank(settings.getHttpProxy()
								.getUser()) ? settings.getHttpProxy().getUser()
						: null);
		configStore.store(
				HttpSettings.PROP_HTTP_PROXY_PASSWORD,
				settings.getHttpProxy() != null
						&& StringUtils.isNotBlank(settings.getHttpProxy()
								.getPassword()) ? settings.getHttpProxy()
						.getPassword() : null);
		LOGGER.info("Propagate HTTP proxy settings");
		httpSettings.refreshProxySettings();
	}
}
