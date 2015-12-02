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
package com.logsniffer.model.support;

import java.io.IOException;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogRawAccessor;

import wiremock.org.mortbay.jetty.security.Credential.MD5;

/**
 * Byte array log for tests.
 * 
 * @author mbok
 * 
 */
public class ByteArrayLog
		implements Log, LogRawAccessor<ByteLogInputStream, ByteArrayLog>, LogRawAccess<ByteLogInputStream> {
	/**
	 * Byte array input stream in memory for tests.
	 * 
	 * @author mbok
	 * 
	 */
	public class LogByteArrayInputStream extends ByteLogInputStream {
		private final byte[] input;
		private long pos = 0;

		public LogByteArrayInputStream(final byte[] input) {
			this.input = input;
		}

		@Override
		public LogPointer getPointer() throws IOException {
			return new DefaultPointer(pos, input.length);
		}

		@Override
		public int read() throws IOException {
			return pos < input.length ? input[(int) pos++] : -1;
		}

	}

	private final byte[] data;
	private final String path;

	public ByteArrayLog(final byte[] data) {
		this(MD5.digest(new String(data)), data);
	}

	public ByteArrayLog(final String path, final byte[] data) {
		super();
		this.path = path;
		this.data = data;
	}

	@Override
	public long getDifference(final LogPointer source, final LogPointer compareTo) throws IOException {
		final long start = source != null ? ((DefaultPointer) source).getOffset() : 0;
		return ((DefaultPointer) compareTo).getOffset() - start;
	}

	@Override
	public LogPointer createRelative(final LogPointer _source, final long relativeBytePosition) throws IOException {
		final DefaultPointer source = (DefaultPointer) _source;
		final long newOffset = (source != null ? source.getOffset() : 0) + relativeBytePosition;
		final long size = data.length;
		return new DefaultPointer(Math.max(0, Math.min(newOffset, size)), size);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public long getSize() {
		return data.length;
	}

	@Override
	public ByteLogInputStream getInputStream(final LogPointer from) throws IOException {
		final LogByteArrayInputStream is = new LogByteArrayInputStream(data);
		if (from != null) {
			is.pos = ((DefaultPointer) createRelative(null, ((DefaultPointer) from).getOffset())).getOffset();
		}
		return is;
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public String toString() {
		return "ByteArrayLog [path=" + path + "]";
	}

	@Override
	public LogPointer getFromJSON(final String data) {
		return DefaultPointer.fromJSON(data);
	}

	@Override
	public LogRawAccess<ByteLogInputStream> getLogAccess(final ByteArrayLog log) throws IOException {
		return log;
	}

	@Override
	public SizeMetric getSizeMetric() {
		return SizeMetric.BYTE;
	}

}
