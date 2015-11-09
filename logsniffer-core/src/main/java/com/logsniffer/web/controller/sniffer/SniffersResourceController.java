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

import java.text.ParseException;

import javax.validation.Valid;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.event.EventPersistence;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence;
import com.logsniffer.event.SnifferScheduler;
import com.logsniffer.web.controller.exception.ActionViolationException;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;

/**
 * REST resource for sniffers.
 * 
 * @author mbok
 * 
 */
@RestController
public class SniffersResourceController {
	private static Logger logger = LoggerFactory.getLogger(SniffersResourceController.class);

	@Autowired
	protected SnifferPersistence snifferPersistence;

	@Autowired
	private EventPersistence eventPersistence;

	@Autowired
	protected SnifferScheduler snifferScheduler;

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	protected Sniffer getSniffer(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException {
		final Sniffer activeSniffer = snifferPersistence.getSniffer(snifferId);
		if (activeSniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId, "Sniffer not found for id: " + snifferId);
		}
		return activeSniffer;
	}

	@RequestMapping(value = "/sniffers", method = RequestMethod.POST)
	@ResponseBody
	long createSniffer(@Valid @RequestBody final Sniffer newSniffer)
			throws ResourceNotFoundException, SchedulerException {
		final long snifferId = snifferPersistence.createSniffer(newSniffer);
		logger.info("Created new Sniffer with id: {}", snifferId);
		eventPersistence.prepareMapping(snifferId);
		return snifferId;
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.PUT)
	@Transactional(rollbackFor = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	void updateSniffer(@PathVariable("snifferId") final long snifferId, @Valid @RequestBody final Sniffer sniffer)
			throws ResourceNotFoundException, SchedulerException {
		snifferPersistence.updateSniffer(sniffer);
		logger.info("Updated sniffer with id: {}", snifferId);
		eventPersistence.prepareMapping(snifferId);
	}

	@Transactional(rollbackFor = Exception.class)
	@RequestMapping(value = "/sniffers/{snifferId}/start", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void start(@PathVariable("snifferId") final long snifferId)
			throws ResourceNotFoundException, SchedulerException, ParseException {
		logger.info("Starting sniffer: {}", snifferId);
		snifferScheduler.startSniffing(snifferId);
		logger.info("Started sniffer: {}", snifferId);
	}

	@RequestMapping(value = "/sniffers/{snifferId}/stop", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void stop(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException, SchedulerException {
		logger.info("Stopping sniffer: {}", snifferId);
		snifferScheduler.stopSniffing(snifferId);
		logger.info("Stopped sniffer: {}", snifferId);
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@Transactional(rollbackFor = Exception.class)
	void deleteSniffer(@PathVariable("snifferId") final long snifferId)
			throws ResourceNotFoundException, SchedulerException, ActionViolationException {
		logger.info("Deleting sniffer: {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId, "Sniffer not found for id: " + snifferId);
		} else if (snifferScheduler.isScheduled(snifferId)) {
			throw new ActionViolationException("Can't delete a running sniffer");
		}
		snifferPersistence.deleteSniffer(sniffer);
		logger.info("Deleting all sniffer events: {}", snifferId);
		eventPersistence.deleteAll(snifferId);
		logger.info("Deleted sniffer: {}", snifferId);

	}
}
