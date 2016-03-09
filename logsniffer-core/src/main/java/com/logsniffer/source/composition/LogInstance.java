package com.logsniffer.source.composition;

import java.io.IOException;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;
import com.logsniffer.reader.LogEntryReader;

/**
 * Represents a log instance to be composed.
 * 
 * @author mbok
 *
 */
public class LogInstance {
	private long logSourceId;
	private LogRawAccess<LogInputStream> logAccess;
	private LogEntryReader<LogRawAccess<? extends LogInputStream>> reader;
	private Log log;
	private LogSource<LogRawAccess<? extends LogInputStream>> source;

	@SuppressWarnings("unchecked")
	public LogInstance(final long logSourceId, final Log log, final LogRawAccess<LogInputStream> logAccess,
			final LogEntryReader<? extends LogRawAccess<? extends LogInputStream>> reader) {
		super();
		this.logSourceId = logSourceId;
		this.log = log;
		this.logAccess = logAccess;
		this.reader = (LogEntryReader<LogRawAccess<? extends LogInputStream>>) reader;
	}

	@SuppressWarnings("unchecked")
	public LogInstance(final long sourceId, final Log log,
			final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) {
		this(sourceId, log, null, null);
		this.source = (LogSource<LogRawAccess<? extends LogInputStream>>) source;
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
	 * @return the logAccess
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public LogRawAccess<? extends LogInputStream> getLogAccess() throws IOException {
		if (logAccess == null) {
			logAccess = (LogRawAccess<LogInputStream>) source.getLogAccess(log);
		}
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
	public LogEntryReader<LogRawAccess<? extends LogInputStream>> getReader() {
		if (reader == null) {
			reader = source.getReader();
		}
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	@SuppressWarnings("unchecked")
	public void setReader(final LogEntryReader<? extends LogRawAccess<? extends LogInputStream>> reader) {
		this.reader = (LogEntryReader<LogRawAccess<? extends LogInputStream>>) reader;
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
