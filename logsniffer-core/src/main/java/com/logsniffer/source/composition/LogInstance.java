package com.logsniffer.source.composition;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.reader.LogEntryReader;

public class LogInstance {
	private long logSourceId;
	private LogRawAccess<LogInputStream> logAccess;
	private LogEntryReader<LogInputStream> reader;
	private Log log;

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
	 * @return the logAccess
	 */
	public LogRawAccess<LogInputStream> getLogAccess() {
		return logAccess;
	}

	/**
	 * @param logAccess
	 *            the logAccess to set
	 */
	public void setLogAccess(final LogRawAccess<LogInputStream> logAccess) {
		this.logAccess = logAccess;
	}

	/**
	 * @return the reader
	 */
	public LogEntryReader<LogInputStream> getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	public void setReader(final LogEntryReader<LogInputStream> reader) {
		this.reader = reader;
	}

	/**
	 * @return the log
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(final Log log) {
		this.log = log;
	}

	@Override
	public String toString() {
		return "LogInstance [logSourceId=" + logSourceId + ", log=" + log + "]";
	}

}
