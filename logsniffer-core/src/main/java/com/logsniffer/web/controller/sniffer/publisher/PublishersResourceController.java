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
package com.logsniffer.web.controller.sniffer.publisher;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.event.Event;
import com.logsniffer.event.Publisher;
import com.logsniffer.event.Publisher.PublishException;
import com.logsniffer.web.controller.sniffer.SniffersResourceController;

/**
 * {@link Publisher} related REST controller.
 * 
 * @author mbok
 * 
 */
@RestController
public class PublishersResourceController {
	private static Logger logger = LoggerFactory.getLogger(SniffersResourceController.class);

	/**
	 * Request bean for publisher test.
	 * 
	 * @author mbok
	 * 
	 */
	public static class PublisherTestRequest {
		private Publisher publisher;
		private Event event;
		private long snifferId;
		private long logSourceId;
		private String logPath;

		/**
		 * @return the publisher
		 */
		public Publisher getPublisher() {
			return publisher;
		}

		/**
		 * @param publisher
		 *            the publisher to set
		 */
		public void setPublisher(final Publisher publisher) {
			this.publisher = publisher;
		}

		/**
		 * @return the event
		 */
		public Event getEvent() {
			return event;
		}

		/**
		 * @param event
		 *            the event to set
		 */
		public void setEvent(final Event event) {
			this.event = event;
		}

		/**
		 * @return the snifferId
		 */
		public long getSnifferId() {
			return snifferId;
		}

		/**
		 * @param snifferId
		 *            the snifferId to set
		 */
		public void setSnifferId(final long snifferId) {
			this.snifferId = snifferId;
		}

		/**
		 * @return the logSourceId
		 */
		public long getLogSourceId() {
			return logSourceId;
		}

		/**
		 * @param logSourceId
		 *            the logSourceId to set
		 */
		public void setLogSourceId(final long logSourceId) {
			this.logSourceId = logSourceId;
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

	}

	@RequestMapping(value = "/publishers/test", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void testPublishing(@RequestBody final PublisherTestRequest request) throws PublishException {
		final Event event = request.getEvent();
		event.setId("publisherTest");
		event.setPublished(new Date());
		event.setSnifferId(request.getSnifferId());
		event.setLogPath(request.getLogPath());
		event.setLogSourceId(request.getLogSourceId());
		logger.info("Test publishing by {} of : {}", request.getPublisher(), event);
		request.getPublisher().publish(event);
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	public void handleAllExceptions(final Throwable ex, final HttpServletResponse response) throws IOException {
		logger.info("Failed to test event publishing", ex);
		response.setStatus(HttpStatus.CONFLICT.value());
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		final String stackTrace = ExceptionUtils.getStackTrace(ex);
		IOUtils.write(stackTrace, response.getOutputStream());
	}
}
