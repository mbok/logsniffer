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
package com.logsniffer.web.controller.sniffer;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logsniffer.event.Event;
import com.logsniffer.event.EventPersistence;
import com.logsniffer.event.EventPersistence.AspectEvent;
import com.logsniffer.event.EventPersistence.EventQueryBuilder;
import com.logsniffer.event.EventPersistence.NativeQueryBuilder;
import com.logsniffer.util.PageableResult;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;

/**
 * REST controller for events.
 * 
 * @author mbok
 * 
 */
@Controller
public class SnifferEventsResourceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SnifferEventsResourceController.class);

	@Autowired
	private EventPersistence eventPersistence;

	@RequestMapping(value = "/sniffers/{snifferId}/events", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	PageableResult<AspectEvent> getEvents(final Model model, @PathVariable("snifferId") final long snifferId,
			@RequestParam(value = "_offset", defaultValue = "0", required = false) final long offset,
			@RequestParam(value = "_size", defaultValue = "25", required = false) final int size,
			@RequestParam(value = "_from", defaultValue = "-1", required = false) final long occurrenceFrom,
			@RequestParam(value = "_to", defaultValue = "-1", required = false) final long occurrenceTo,
			@RequestParam(value = "_histogram", defaultValue = "true", required = false) final boolean withHistogram) {
		EventQueryBuilder qb = eventPersistence.getEventsQueryBuilder(snifferId, offset, size);
		if (withHistogram) {
			qb = qb.withEventCountTimeHistogram(60);
		}
		qb = qb.sortByEntryTimestamp(false);
		if (occurrenceFrom >= 0) {
			qb.withOccurrenceFrom(new Date(occurrenceFrom));
		}
		if (occurrenceTo >= 0) {
			qb.withOccurrenceTo(new Date(occurrenceTo));
		}
		final PageableResult<AspectEvent> events = qb.list();
		return events;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/nativeSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	PageableResult<AspectEvent> getEventsByNativeSearch(@PathVariable("snifferId") final long snifferId,
			@RequestParam(value = "_offset", defaultValue = "0", required = false) final long offset,
			@RequestParam(value = "_size", defaultValue = "25", required = false) final int size,
			@RequestParam(value = "_histogram", defaultValue = "true", required = false) final boolean withHistogram,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String jsonRequest = IOUtils.toString(request.getInputStream());
		NativeQueryBuilder qb = eventPersistence.getEventsNativeQueryBuilder(snifferId, offset, size);
		if (withHistogram) {
			qb = qb.withEventCountTimeHistogram(60);
		}
		qb.withNativeQuery(jsonRequest);
		final PageableResult<AspectEvent> events = qb.list();
		return events;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/{eventId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	Event showEvent(@PathVariable("snifferId") final long snifferId, @PathVariable("eventId") final String eventId)
			throws ResourceNotFoundException {
		final Event event = eventPersistence.getEvent(snifferId, eventId);
		if (event == null) {
			throw new ResourceNotFoundException(Event.class, eventId, "Event not found for id: " + eventId);
		}
		return event;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/{eventId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	void deleteEvent(@PathVariable("snifferId") final long snifferId, @PathVariable("eventId") final String eventId)
			throws ResourceNotFoundException {
		LOGGER.debug("Deleting event {} for sniffer {}", eventId, snifferId);
		// Load event first to check existence
		showEvent(snifferId, eventId);
		// Delete now
		eventPersistence.delete(snifferId, new String[] { eventId });
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	void deleteAllEvents(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException {
		LOGGER.info("Deleting all events of sniffer {}", snifferId);
		eventPersistence.deleteAll(snifferId);
	}
}
