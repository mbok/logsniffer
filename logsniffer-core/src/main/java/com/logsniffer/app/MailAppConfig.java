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

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactory;

import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.Configured;

/**
 * Defines a {@link VelocityEngine} bean and a {@link JavaMailSenderImpl} bean
 * for mail support.
 * 
 * @author mbok
 * 
 */
@Configuration
public class MailAppConfig {
	/**
	 * Warpper for mail settings.
	 * 
	 * @author mbok
	 * 
	 */
	public static class MailSettings {
		public static final String PROP_LOGSNIFFER_MAIL_PORT = "logsniffer.mail.port";

		public static final String PROP_LOGSNIFFER_MAIL_PASSWORD = "logsniffer.mail.password";

		public static final String PROP_LOGSNIFFER_MAIL_USER = "logsniffer.mail.user";

		public static final String PROP_LOGSNIFFER_MAIL_HOST = "logsniffer.mail.host";

		@Configured(PROP_LOGSNIFFER_MAIL_HOST)
		private ConfigValue<String> mailHost;

		@Configured(PROP_LOGSNIFFER_MAIL_USER)
		private ConfigValue<String> mailUser;

		@Configured(PROP_LOGSNIFFER_MAIL_PASSWORD)
		private ConfigValue<String> mailPassword;

		@Configured(PROP_LOGSNIFFER_MAIL_PORT)
		private ConfigValue<Integer> mailPort;

		/**
		 * @return the mailHost
		 */
		public ConfigValue<String> getMailHost() {
			return mailHost;
		}

		/**
		 * @return the mailUser
		 */
		public ConfigValue<String> getMailUser() {
			return mailUser;
		}

		/**
		 * @return the mailPassword
		 */
		public ConfigValue<String> getMailPassword() {
			return mailPassword;
		}

		/**
		 * @return the mailPort
		 */
		public ConfigValue<Integer> getMailPort() {
			return mailPort;
		}
	}

	@Autowired
	@Qualifier(CoreAppConfig.BEAN_LOGSNIFFER_PROPS)
	private Properties props;

	private MailSettings mailSettings;

	private JavaMailSenderImpl mailSender;

	@Bean
	public VelocityEngine velocityEngine() throws VelocityException,
			IOException {
		VelocityEngineFactory factory = new VelocityEngineFactory();
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		factory.setVelocityProperties(props);
		return factory.createVelocityEngine();
	}

	@Bean
	public JavaMailSenderImpl mailSender() {
		mailSettings();
		mailSender = new JavaMailSenderImpl();
		mailSender.setDefaultEncoding("UTF-8");
		refreshMailSenderConfiguration();
		return mailSender;
	}

	@Bean
	MailSettings mailSettings() {
		mailSettings = new MailSettings();
		return mailSettings;
	}

	/**
	 * Refresh settings for mail sender.
	 */
	public void refreshMailSenderConfiguration() {
		mailSender.setJavaMailProperties(props);
		mailSender.setHost(mailSettings.getMailHost().get());
		mailSender.setUsername(mailSettings.getMailUser().get());
		mailSender.setPassword(mailSettings.getMailPassword().get());
		mailSender.setPort(mailSettings.getMailPort().get());
	}
}
