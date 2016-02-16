package com.logsniffer.model;

import java.io.IOException;

public interface Navigation<M> {

	public static interface OffsetMetric<M> {
		public static final OffsetMetric<Long> BYTE = new OffsetMetric<Long>() {
		};
	}

	public interface NavigationFuture {
		LogPointer get() throws IOException;
	}

	LogPointer end() throws IOException;

	LogPointer start() throws IOException;

	NavigationFuture refresh(LogPointer toRefresh) throws IOException;

	NavigationFuture absolute(M offset);
}