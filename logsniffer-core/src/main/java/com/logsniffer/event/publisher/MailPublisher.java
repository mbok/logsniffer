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

import javax.validation.constraints.NotNull;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.event.Event;
import com.logsniffer.event.Publisher;
import com.logsniffer.model.LogEntry;
import com.logsniffer.validators.MailListConstraint;

/**
 * Publishes an event by sending a mail. The fields subject and text will be
 * rendered by {@link Velocity} with passed {@link Event} as "event" attribute.
 * 
 * @author mbok
 * 
 */
@Component
@PostConstructed(constructor = MailPublisher.class)
public class MailPublisher implements Publisher, BeanPostConstructor<MailPublisher> {
	private static Logger logger = LoggerFactory.getLogger(MailPublisher.class);

	@Autowired
	@JsonIgnore
	private VelocityEngine velocityEngine;

	@Autowired
	@JsonIgnore
	private MailSender mailSender;

	@Autowired
	@JsonIgnore
	private VelocityEventRenderer velocityRenderer;

	@NotEmpty
	@MailListConstraint
	@JsonProperty
	private String to;

	@NotEmpty
	@JsonProperty
	private String subject;

	@NotEmpty
	@org.hibernate.validator.constraints.Email
	@JsonProperty
	private String from;

	@NotNull
	@JsonProperty
	private String textMessage = "Event link: $eventLink\n\nLog entries:\n" + "#foreach( $entry in $event['"
			+ Event.FIELD_ENTRIES + "'] )" + "\n  $entry['" + LogEntry.FIELD_RAW_CONTENT + "']\n" + "#end";

	@Override
	public void publish(final Event event) throws PublishException {
		try {
			final VelocityContext context = velocityRenderer.getContext(event);
			final SimpleMailMessage email = new SimpleMailMessage();
			email.setFrom(getFrom());
			email.setSubject(velocityRenderer.render(getSubject(), context));
			email.setText(velocityRenderer.render(getTextMessage(), context) + " ");
			final String to2 = getTo();
			email.setTo(to2.split(",|\\s"));
			mailSender.send(email);
			logger.info("Sent event notification to: {}", to2);
		} catch (final MailException e) {
			throw new PublishException("Failed to send event notification to mail: " + getTo(), e);
		}
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to
	 *            the to to set
	 */
	public void setTo(final String to) {
		this.to = to;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            the from to set
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * @return the textMessage
	 */
	public String getTextMessage() {
		return textMessage;
	}

	/**
	 * @param textMessage
	 *            the textMessage to set
	 */
	public void setTextMessage(final String textMessage) {
		this.textMessage = textMessage;
	}

	@Override
	public void postConstruct(final MailPublisher bean, final BeanConfigFactoryManager configManager)
			throws ConfigException {
		bean.mailSender = mailSender;
		bean.velocityEngine = velocityEngine;
		bean.velocityRenderer = velocityRenderer;
	}

}
