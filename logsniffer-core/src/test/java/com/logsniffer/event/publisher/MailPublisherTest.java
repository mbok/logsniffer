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
package com.logsniffer.event.publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.ConfigValueAppConfig;
import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.MailAppConfig;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.event.Event;
import com.logsniffer.event.Publisher.PublishException;
import com.logsniffer.model.LogEntry;

/**
 * Test for {@link MailPublisher}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MailAppConfig.class, MailPublisherTest.HelperAppConfig.class, CoreAppConfig.class,
		ConfigValueAppConfig.class })
public class MailPublisherTest {
	@Configuration
	public static class HelperAppConfig {
		@Primary
		@Bean
		public MailSender mailSender() {
			return Mockito.mock(MailSender.class);
		}

		@Bean
		public VelocityEventRenderer velocityRenderer() {
			return new VelocityEventRenderer();
		}

		@Bean
		public MailPublisher mailPublisher() {
			return new MailPublisher();
		}

		@Bean
		public ConversionService conversionService() {
			return new DefaultFormattingConversionService();
		}
	}

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private MailPublisher mailPublisher;

	@Test
	public void testMailPublishing() throws PublishException {
		final Event event = new Event();
		event.setId("3");
		event.setLogPath("path.log");
		event.setLogSourceId(7);
		event.setSnifferId(5);
		final Map<String, Serializable> data = new HashMap<String, Serializable>();
		data.put("key1", "abc");
		data.put("key3", "def");
		event.putAll(data);
		final List<LogEntry> entries = new ArrayList<LogEntry>();
		final LogEntry e1 = new LogEntry();
		e1.setRawContent("e1");
		final LogEntry e2 = new LogEntry();
		e2.setRawContent("e2");
		entries.add(e1);
		entries.add(e2);
		event.setEntries(entries);

		mailPublisher.setSubject("Subject: $event.lf_entries[0].lf_raw");
		mailPublisher.setFrom("fromme $event");
		mailPublisher.setTo("tome$event,toyou");

		final MailPublisher clonePublisher = configManager.createBeanFromJSON(MailPublisher.class,
				configManager.saveBeanToJSON(mailPublisher));
		clonePublisher.publish(event);

		Mockito.verify(mailSender).send(Mockito.argThat(new BaseMatcher<SimpleMailMessage>() {

			@Override
			public boolean matches(final Object arg0) {
				final SimpleMailMessage mail = (SimpleMailMessage) arg0;
				if (!mail.getSubject().equals("Subject: " + e1.getRawContent())) {
					return false;
				}
				final Pattern textPattern = Pattern.compile(
						"[^\n]+/c/sniffers/5/events/#/3\n\n" + "Log entries:\n" + "\\s*e1\n" + "\\s*e2\n\\s*",
						Pattern.CASE_INSENSITIVE + Pattern.DOTALL + Pattern.MULTILINE);
				if (!textPattern.matcher(mail.getText()).matches()) {
					return false;
				}
				return mail.getFrom().equals("fromme $event") && mail.getTo().length == 2
						&& mail.getTo()[0].equals("tome$event") && mail.getTo()[1].equals("toyou");
			}

			@Override
			public void describeTo(final Description arg0) {
			}
		}));

	}
}
