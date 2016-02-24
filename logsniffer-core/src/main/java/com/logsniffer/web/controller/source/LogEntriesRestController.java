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
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logsniffer.event.Event;
import com.logsniffer.event.IncrementData;
import com.logsniffer.event.Scanner;
import com.logsniffer.event.Scanner.EventConsumer;
import com.logsniffer.event.support.TimeoutReaderStrategy;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.support.BufferedConsumer;
import com.logsniffer.reader.support.FluentReverseReader;
import com.logsniffer.web.controller.LogEntriesResult;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;

/**
 * REST controller for several log entries purposes.
 * 
 * @author mbok
 * 
 */
@RestController
public class LogEntriesRestController {
	private static Logger logger = LoggerFactory.getLogger(LogEntriesRestController.class);
	@Autowired
	private LogSourceProvider logsSourceProvider;

	private LogSource<LogRawAccess<? extends LogInputStream>> getActiveLogSource(final long logSourceId)
			throws ResourceNotFoundException {
		final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = logsSourceProvider
				.getSourceById(logSourceId);
		if (activeLogSource != null) {
			return activeLogSource;
		} else {
			throw new ResourceNotFoundException(LogSource.class, logSourceId,
					"Log source not found for id: " + logSourceId);
		}
	}

	private int getHighlightEntryIndex(final LogPointer desiredPointer, final int desiredCount,
			final List<LogEntry> entries) {
		if (!entries.isEmpty()) {
			if (desiredCount >= 0) {
				if (desiredPointer == null) {
					return 0;
				} else {
					for (int i = 0; i < entries.size(); i++) {
						if (desiredPointer.equals(entries.get(i).getStartOffset())) {
							return i;
						}
					}
				}

			} else {
				if (desiredPointer == null) {
					return entries.size() - 1;
				} else {
					for (int i = entries.size() - 1; i >= 0; i--) {
						if (desiredPointer.equals(entries.get(i).getEndOffset())) {
							return i;
						}
					}

				}
			}
		}
		return -1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/sources/entries", method = RequestMethod.POST)
	@ResponseBody
	LogEntriesResult getEntries(
			@Valid @RequestBody final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count)
					throws IOException, FormatException, ResourceNotFoundException {
		logger.debug("Start load entries log={} from source={}, mark={}, count={}", logPath, activeLogSource, mark,
				count);
		try {
			final Log log = getLog(activeLogSource, logPath);
			LogPointer pointer = null;
			final LogRawAccess<? extends LogInputStream> logAccess = activeLogSource.getLogAccess(log);
			if (StringUtils.isNotEmpty(mark)) {
				pointer = logAccess.getFromJSON(mark);
			} else {
				if (count < 0) {
					// Tail
					pointer = logAccess.end();
				}
			}
			if (count > 0) {
				final BufferedConsumer bc = new BufferedConsumer(count);
				activeLogSource.getReader().readEntries(log, logAccess, pointer, bc);
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), bc.getBuffer(),
						getHighlightEntryIndex(pointer, count, bc.getBuffer()));
			} else {
				final BufferedConsumer bc = new BufferedConsumer(-count);
				activeLogSource.getReader().readEntriesReverse(log, logAccess, pointer, bc);
				final List<LogEntry> readEntries = bc.getBuffer();
				Collections.reverse(readEntries);
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries,
						getHighlightEntryIndex(pointer, count, readEntries));
			}

		} finally {
			logger.debug("Finished log entries from log={} and source={}", logPath, activeLogSource);
		}
	}

	@RequestMapping(value = "/sources/{logSource}/entries", method = RequestMethod.GET)
	@ResponseBody
	LogEntriesResult getEntries(@PathVariable("logSource") final long logSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count)
					throws IOException, FormatException, ResourceNotFoundException {
		logger.debug("Start load entries log={} from source={}, mark={}, count={}", logPath, logSource, mark, count);
		try {
			final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = getActiveLogSource(logSource);
			return getEntries(activeLogSource, logPath, mark, count);
		} finally {
			logger.debug("Finished log entries from log={} and source={}", logPath, logSource);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/sources/randomAccessEntries", method = RequestMethod.POST)
	@ResponseBody
	LogEntriesResult getRandomAccessEntries(
			@Valid @RequestBody final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource,
			@RequestParam("log") final String logPath, @RequestParam(value = "mark") final String mark,
			@RequestParam(value = "count") final int count)
					throws IOException, FormatException, ResourceNotFoundException {
		logger.debug("Start loading random access entries log={} from source={}, mark={}, count={}", logPath,
				activeLogSource, mark, count);
		try {
			final Log log = getLog(activeLogSource, logPath);
			final LogRawAccess<? extends LogInputStream> logAccess = activeLogSource.getLogAccess(log);
			LogPointer pointer = null;
			if (StringUtils.isNotEmpty(mark)) {
				pointer = logAccess.getFromJSON(mark);
			}
			final BufferedConsumer bc = new BufferedConsumer(count + 1);
			if (pointer != null) {
				pointer = logAccess.refresh(pointer).get();
				if (pointer.isEOF()) {
					// End pointer, return the last 10 simply
					final BufferedConsumer bcLast = new BufferedConsumer(10);
					activeLogSource.getReader().readEntriesReverse(log, logAccess, pointer, bcLast);
					final List<LogEntry> readEntries = bcLast.getBuffer();
					Collections.reverse(readEntries);
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries,
							getHighlightEntryIndex(pointer, -10, readEntries));
				}
			}
			activeLogSource.getReader().readEntries(log, logAccess,
					pointer != null ? logAccess.refresh(pointer).get() : null, bc);
			final List<LogEntry> entries = bc.getBuffer();
			if (entries.size() > 0) {
				final LogEntry first = entries.get(0);
				final LogEntry last = entries.get(entries.size() - 1);
				if (!first.getStartOffset().isSOF() && last.getEndOffset().isEOF() && entries.size() < 10) {
					// Hm, EOF reached
					final BufferedConsumer bcLast = new BufferedConsumer(10);
					activeLogSource.getReader().readEntriesReverse(log, logAccess, last.getEndOffset(), bcLast);
					final List<LogEntry> readEntries = bcLast.getBuffer();
					Collections.reverse(readEntries);
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries, -1);
				} else if (!first.isUnformatted()) {
					// -1 without effect, return from beginning
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), entries,
							getHighlightEntryIndex(pointer, 0, entries));
				} else {
					// Return from the second one
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(),
							entries.subList(1, entries.size()), getHighlightEntryIndex(pointer, 0, entries) - 1);
				}
			} else {
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), entries, -1);
			}
		} finally {
			logger.debug("Finished loading random access entries from log={} and source={}", logPath, activeLogSource);
		}
	}

	@RequestMapping(value = "/sources/{logSource}/randomAccessEntries", method = RequestMethod.GET)
	@ResponseBody
	LogEntriesResult getRandomAccessEntries(@PathVariable("logSource") final long logSource,
			@RequestParam("log") final String logPath, @RequestParam(value = "mark") final String mark,
			@RequestParam(value = "count") final int count)
					throws IOException, FormatException, ResourceNotFoundException {
		logger.debug("Start loading random access entries log={} from source={}, mark={}, count={}", logPath, logSource,
				mark, count);
		try {
			final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = getActiveLogSource(logSource);
			return getRandomAccessEntries(activeLogSource, logPath, mark, count);
		} finally {
			logger.debug("Finished loading random access entries from log={} and source={}", logPath, logSource);
		}
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class SearchResult {
		@JsonIgnore
		private boolean sofReached = false;
		private LogEntriesResult entries;
		private LogPointer lastPointer;
		private long scannedSize;
		private long scannedTime;
		private Event event;

		/**
		 * @return the entries
		 */
		public LogEntriesResult getEntries() {
			return entries;
		}

		/**
		 * @return the lastPointer
		 */
		public LogPointer getLastPointer() {
			return lastPointer;
		}

		/**
		 * @return the scannedSize
		 */
		public long getScannedSize() {
			return scannedSize;
		}

		/**
		 * @return the scannedTime
		 */
		public long getScannedTime() {
			return scannedTime;
		}

		/**
		 * @return the event
		 */
		public Event getEvent() {
			return event;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/sources/{logSource}/search", method = RequestMethod.POST)
	@ResponseBody
	SearchResult searchEntries(@RequestBody @Valid final Scanner scanner,
			@PathVariable("logSource") final long logSource, @RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count, final BindingResult bResult)
					throws IOException, FormatException, ResourceNotFoundException {
		final long start = System.currentTimeMillis();
		logger.debug("Start searching entries log={} from source={}, mark={}, count={}", logPath, logSource, mark,
				count);
		final LogSource<LogRawAccess<? extends LogInputStream>> source = getActiveLogSource(logSource);
		final Log log = getLog(source, logPath);
		final LogRawAccess<LogInputStream> logAccess = (LogRawAccess<LogInputStream>) source.getLogAccess(log);
		final IncrementData incData = new IncrementData();
		LogPointer searchPointer = null;
		if (StringUtils.isNotEmpty(mark)) {
			searchPointer = logAccess.getFromJSON(mark);
		}
		incData.setNextOffset(searchPointer);
		final SearchResult searchResult = new SearchResult();
		LogEntryReader<LogRawAccess<LogInputStream>> reader = null;
		if (count >= 0) {
			reader = ((LogSource) source).getReader();
		} else {
			reader = new FluentReverseReader(source.getReader());
		}
		scanner.find(reader, new TimeoutReaderStrategy(3 * 1000) {
			@Override
			public boolean continueReading(final Log log, final LogPointerFactory pointerFactory,
					final LogEntry currentReadEntry) throws IOException {
				if (count < 0 && currentReadEntry.getStartOffset().isSOF()) {
					// File start reached
					searchResult.sofReached = true;
				}
				return !searchResult.sofReached && searchResult.lastPointer == null
						&& super.continueReading(log, pointerFactory, currentReadEntry);
			}
		}, log, logAccess, incData, new EventConsumer() {
			@Override
			public void consume(final Event eventData) throws IOException {
				searchResult.event = eventData;
				searchResult.lastPointer = eventData.getEntries().get(0).getStartOffset();
			}
		});
		searchResult.scannedTime = System.currentTimeMillis() - start;
		if (searchResult.lastPointer != null) {
			// Found
			logger.debug("Found next entry of interest in {} at: {}", log, searchResult.lastPointer);
			final BufferedConsumer bc = new BufferedConsumer(Math.abs(count));
			source.getReader().readEntries(log, logAccess, searchResult.lastPointer, bc);
			searchResult.entries = new LogEntriesResult(source.getReader().getFieldTypes(), bc.getBuffer(), 0);
		} else if (searchResult.sofReached) {
			// Return start pointer
			searchResult.lastPointer = logAccess.start();
		} else {
			// Nothing found in that round
			searchResult.lastPointer = incData.getNextOffset();
		}
		searchResult.scannedSize = Math.abs(logAccess.getDifference(searchPointer, searchResult.lastPointer));
		return searchResult;
	}

	private Log getLog(final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource, final String logPath)
			throws IOException, ResourceNotFoundException {
		if (logPath == null) {
			return null;
		}
		final Log log = activeLogSource.getLog(logPath);
		if (log != null) {
			return log;
		} else {
			throw new ResourceNotFoundException(Log.class, logPath,
					"Log not found in source " + activeLogSource + ": " + logPath);
		}
	}
}
