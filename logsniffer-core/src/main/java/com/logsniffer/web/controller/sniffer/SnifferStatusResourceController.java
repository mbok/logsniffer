package com.logsniffer.web.controller.sniffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.event.IncrementData;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence;
import com.logsniffer.event.SnifferScheduler;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.LogSourceProvider;
import com.logsniffer.model.support.JsonLogPointer;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;
import com.logsniffer.web.controller.sniffer.SnifferStatusController.LogSniffingStatus;

/**
 * REST controller for sniffer status methods.
 * 
 * @author mbok
 *
 */
@RestController
public class SnifferStatusResourceController {
	private static Logger logger = LoggerFactory.getLogger(SnifferStatusResourceController.class);

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private LogSourceProvider sourceProvider;

	@Autowired
	protected SnifferScheduler snifferScheduler;

	@RequestMapping(value = "/sniffers/{snifferId}/status/pointerOffset", method = RequestMethod.POST)
	@ResponseBody
	LogSniffingStatus updateStatusByPointer(@PathVariable("snifferId") final long snifferId,
			@RequestParam("log") final String logPath, @RequestBody final JsonLogPointer pointer)
			throws IOException, ResourceNotFoundException {
		final Sniffer sniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(sniffer);
		final Log log = source.getLog(logPath);
		if (log == null) {
			throw new ResourceNotFoundException(Log.class, logPath);
		}
		final LogRawAccess<? extends LogInputStream> logAccess = source.getLogAccess(log);
		final LogPointer targetPointer = logAccess.getFromJSON(pointer.getJson());
		final LogPointer refreshedPointer = logAccess.refresh(targetPointer).get();
		final long offset = logAccess.getDifference(null, refreshedPointer);
		return new LogSniffingStatus(log, refreshedPointer, offset, log.getSize());
	}

	@RequestMapping(value = "/sniffers/{snifferId}/status/summary", method = RequestMethod.GET)
	@ResponseBody
	Map<String, Object> getStatusSummary(@PathVariable("snifferId") final long snifferId)
			throws IOException, ResourceNotFoundException {
		final Sniffer sniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(sniffer);
		final List<LogSniffingStatus> logsStatus = new ArrayList<LogSniffingStatus>();
		final Map<Log, IncrementData> logsIncData = snifferPersistence.getIncrementDataByLog(sniffer, source);
		for (final Log log : logsIncData.keySet()) {
			final LogRawAccess<?> logAccess = source.getLogAccess(log);
			final IncrementData incData = logsIncData.get(log);
			LogPointer currentPointer = null;
			long currentOffset = 0;
			if (incData.getNextOffset() != null) {
				currentPointer = logAccess.refresh(logAccess.getFromJSON(incData.getNextOffset().getJson())).get();
			}
			if (currentPointer != null) {
				currentOffset = Math.abs(logAccess.getDifference(null, currentPointer));
			}
			logsStatus.add(new LogSniffingStatus(log, currentPointer, currentOffset, log.getSize()));
		}
		final HashMap<String, Object> summary = new HashMap<>();
		summary.put("scheduleInfo", snifferScheduler.getScheduleInfo(snifferId));
		summary.put("logsStatus", logsStatus);
		return summary;
	}

	private LogSource<?> getSource(final Sniffer sniffer) throws ResourceNotFoundException {
		final LogSource<?> source = sourceProvider.getSourceById(sniffer.getLogSourceId());
		if (source == null) {
			throw new ResourceNotFoundException(LogSource.class, sniffer.getLogSourceId());
		}
		return source;
	}

	private Sniffer getSniffer(final long snifferId) throws ResourceNotFoundException {
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId);
		}
		return sniffer;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/status/startFrom", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	void startFrom(@PathVariable("snifferId") final long snifferId, @RequestBody final StartFromBean[] startFromList)
			throws ResourceNotFoundException, IOException, SchedulerException {
		logger.info("Starting sniffer {} from: {}", snifferId, startFromList);
		final Sniffer activeSniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(activeSniffer);
		for (final Log log : source.getLogs()) {
			StartFromBean startFrom = null;
			for (final StartFromBean s : startFromList) {
				if (log.getPath().equals(s.logPath)) {
					startFrom = s;
					break;
				}
			}
			if (startFrom != null) {
				final IncrementData incData = snifferPersistence.getIncrementData(activeSniffer, source, log);
				if (startFrom.startFromHead) {
					incData.setNextOffset(null);
					logger.debug("Setup sniffing {} from head", log);
				} else if (startFrom.startFromTail) {
					final LogRawAccess<?> logAccess = source.getLogAccess(log);
					final LogPointer end = logAccess.end();
					incData.setNextOffset(end);
					logger.debug("Setup sniffing {} from tail: {}", log, end);
				} else if (startFrom.startFromPointer != null && startFrom.startFromPointer.getJson() != null) {
					final LogRawAccess<?> logAccess = source.getLogAccess(log);
					final LogPointer refreshedPointer = logAccess.getFromJSON(startFrom.startFromPointer.getJson());
					incData.setNextOffset(refreshedPointer);
					logger.debug("Setup sniffing {} from pointer: {}", log, refreshedPointer);
				} else {
					logger.warn("Invalid request to setup sniffing {}: {}", log, startFrom);
					continue;
				}
				snifferPersistence.storeIncrementalData(activeSniffer, source, log, incData);
			} else {
				logger.debug("No start-from information received for log, sniffing will be started from head: {}", log);
			}
		}
		snifferScheduler.startSniffing(activeSniffer.getId());
		logger.info("Started sniffer: {}", snifferId);
	}

	/**
	 * Bean for deserializing the start from request.
	 * 
	 * @author mbok
	 *
	 */
	public static class StartFromBean {
		private String logPath;
		private boolean startFromHead;
		private boolean startFromTail;
		private JsonLogPointer startFromPointer;

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

		/**
		 * @return the startFromHead
		 */
		public boolean isStartFromHead() {
			return startFromHead;
		}

		/**
		 * @param startFromHead
		 *            the startFromHead to set
		 */
		public void setStartFromHead(final boolean startFromHead) {
			this.startFromHead = startFromHead;
		}

		/**
		 * @return the startFromTail
		 */
		public boolean isStartFromTail() {
			return startFromTail;
		}

		/**
		 * @param startFromTail
		 *            the startFromTail to set
		 */
		public void setStartFromTail(final boolean startFromTail) {
			this.startFromTail = startFromTail;
		}

		/**
		 * @return the startFromPointer
		 */
		public JsonLogPointer getStartFromPointer() {
			return startFromPointer;
		}

		/**
		 * @param startFromPointer
		 *            the startFromPointer to set
		 */
		public void setStartFromPointer(final JsonLogPointer startFromPointer) {
			this.startFromPointer = startFromPointer;
		}

		@Override
		public String toString() {
			return "StartFromBean [logPath=" + logPath + ", startFromHead=" + startFromHead + ", startFromTail="
					+ startFromTail + ", startFromPointer=" + startFromPointer + "]";
		}

	}
}
