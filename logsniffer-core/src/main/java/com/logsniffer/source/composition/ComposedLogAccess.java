package com.logsniffer.source.composition;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;

/**
 * Access to composed logs.
 * 
 * @author mbok
 *
 */
public class ComposedLogAccess implements LogRawAccess<LogInputStream> {

	public ComposedLogAccess(final Log thisLog, final Pair<Log, LogSource<LogRawAccess<LogInputStream>>> toCompose) {

	}

	@Override
	public long getDifference(final LogPointer source, final LogPointer compareTo) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LogPointer createRelative2(final LogPointer source, final long relativeBytePosition) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogPointer getFromJSON(final String data) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogInputStream getInputStream(final LogPointer from) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogPointer end() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogPointer start() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigationFuture refresh(final LogPointer toRefresh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigationFuture absolute(final Long offset) {
		// TODO Auto-generated method stub
		return null;
	}

}
