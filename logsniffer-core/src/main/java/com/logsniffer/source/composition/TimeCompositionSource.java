package com.logsniffer.source.composition;

import java.io.IOException;
import java.util.List;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.support.BaseLogsSource;

/**
 * Composes multiple logs into one ordered by the timestamp field.
 * 
 * @author mbok
 *
 */
public class TimeCompositionSource extends BaseLogsSource<LogRawAccess<LogInputStream>> {

	@Override
	public List<Log> getLogs() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Log getLog(final String path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogRawAccess<LogInputStream> getLogAccess(final Log log) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
