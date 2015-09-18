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
package com.logsniffer.web.controller.source;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.logsniffer.event.Scanner;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.web.ViewController;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;
import com.logsniffer.web.wizard2.WizardInfo;
import com.logsniffer.web.wizard2.WizardInfoController;

@ViewController
public class SourcesController {

	public static final int DEFAULT_ENTRIES_COUNT = 50;

	@Autowired
	private LogSourceProvider logsSourceProvider;

	@Autowired
	private WizardInfoController wizardController;

	@ModelAttribute("filterScannerWizards")
	public List<WizardInfo> filterScannerWizards(final Locale locale) {
		return wizardController.getWizardsInfo(Scanner.class, locale);
	}

	private LogSource<LogInputStream> getAndFillActiveLogSource(
			final ModelAndView mv, final long logSourceId)
			throws ResourceNotFoundException {
		LogSource<LogInputStream> activeLogSource = logsSourceProvider
				.getSourceById(logSourceId);
		if (activeLogSource != null) {
			mv.addObject("activeSource", activeLogSource);
			return activeLogSource;
		} else {
			throw new ResourceNotFoundException(LogSource.class, logSourceId,
					"Log source not found for id: " + logSourceId);
		}
	}

	private Log getAndFillLog(final ModelAndView mv,
			final LogSource<LogInputStream> activeLogSource,
			final String logPath) throws IOException, ResourceNotFoundException {
		if (logPath == null) {
			return null;
		}
		Log log = activeLogSource.getLog(logPath);
		if (log != null) {
			mv.addObject("activeLog", log);
			return log;
		} else {
			throw new ResourceNotFoundException(Log.class, logPath,
					"Log not found in source " + activeLogSource + ": "
							+ logPath);
		}
	}

	@RequestMapping(value = "/sources", method = RequestMethod.GET)
	ModelAndView listSources() {
		ModelAndView mv = new ModelAndView("sources/list");
		List<LogSource<LogInputStream>> logSources = logsSourceProvider
				.getSources();
		mv.addObject("logSources", logSources);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSourceId}/logs", method = RequestMethod.GET)
	ModelAndView listSourceLogs(
			@PathVariable("logSourceId") final long logSourceId)
			throws IOException, ResourceNotFoundException {
		ModelAndView mv = new ModelAndView("sources/logs");
		LogSource<LogInputStream> activeSource = getAndFillActiveLogSource(mv,
				logSourceId);
		List<LogSource<LogInputStream>> logSources = logsSourceProvider
				.getSources();
		mv.addObject("logSources", logSources);
		mv.addObject("logs", activeSource.getLogs());
		mv.addObject("defaultCount", DEFAULT_ENTRIES_COUNT);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSource}/info", method = RequestMethod.GET)
	ModelAndView info(@PathVariable("logSource") final long logSource,
			@RequestParam("log") final String logPath) throws IOException,
			ResourceNotFoundException {
		ModelAndView mv = new ModelAndView("sources/info");
		getAndFillLog(mv, getAndFillActiveLogSource(mv, logSource), logPath);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSource}/show", method = RequestMethod.GET)
	ModelAndView showStart(@PathVariable("logSource") final long logSourceId,
			@RequestParam("log") final String logPath) throws IOException,
			ResourceNotFoundException {
		ModelAndView mv = new ModelAndView("sources/show");
		LogSource<LogInputStream> logSource = getAndFillActiveLogSource(mv,
				logSourceId);
		Log log = getAndFillLog(mv, logSource, logPath);
		mv.addObject("defaultCount", DEFAULT_ENTRIES_COUNT);
		mv.addObject("pointerTpl",
				logSource.getLogAccess(log).createRelative(null, 1));
		return mv;
	}

}