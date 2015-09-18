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

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.logsniffer.event.EventPersistence;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence;
import com.logsniffer.event.SnifferScheduler;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;

/**
 * Base class for sniffer related controllers.
 * 
 * @author mbok
 * 
 */
public abstract class SniffersBaseController {
	@Autowired
	protected SnifferPersistence snifferPersistence;

	@Autowired
	protected EventPersistence eventPersistence;

	@Autowired
	protected SnifferScheduler snifferScheduler;

	@Autowired
	protected LogSourceProvider sourceProvider;

	protected Sniffer getAndBindActiveSniffer(final Model mv,
			final long snifferId) throws ResourceNotFoundException,
			SchedulerException {
		Sniffer activeSniffer = snifferPersistence.getSniffer(snifferId);
		if (activeSniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId,
					"Sniffer not found for id: " + snifferId);
		}
		mv.addAttribute("activeSniffer", activeSniffer);
		mv.addAttribute("scheduled", snifferScheduler.isScheduled(snifferId));
		return activeSniffer;
	}

	protected LogSource<LogInputStream> getLogSource(final long sourceId)
			throws ResourceNotFoundException {
		LogSource<LogInputStream> source = sourceProvider
				.getSourceById(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(LogSource.class, sourceId,
					"Log source not found for id: " + sourceId);
		}
		return source;
	}
}
