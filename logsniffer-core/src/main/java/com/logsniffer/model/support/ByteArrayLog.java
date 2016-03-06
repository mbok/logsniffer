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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccessor;
import com.logsniffer.model.Navigation;

/**
 * Byte array log for tests.
 * 
 * @author mbok
 * 
 */
public class ByteArrayLog implements Log, LogRawAccessor<ByteLogAccess, ByteArrayLog>, ByteLogAccess {
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
		this(getPath(data), data);
	}

	private static String getPath(final byte[] data) {
		try {
			return new String(MessageDigest.getInstance("MD5").digest(data));
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
			return UUID.randomUUID().toString();
		}
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

	private LogPointer createRelative2(final LogPointer _source, final long relativeBytePosition) throws IOException {
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
			is.pos = ((DefaultPointer) absolute(((DefaultPointer) from).getOffset()).get()).getOffset();
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
	public ByteLogAccess getLogAccess(final ByteArrayLog log) throws IOException {
		return log;
	}

	@Override
	public SizeMetric getSizeMetric() {
		return SizeMetric.BYTE;
	}

	@Override
	public LogPointer end() {
		return new DefaultPointer(getSize(), getSize());
	}

	@Override
	public LogPointer start() {
		return new DefaultPointer(0, getSize());
	}

	@Override
	public NavigationFuture refresh(final LogPointer toRefresh) {
		return new NavigationFuture() {
			@Override
			public LogPointer get() throws IOException {
				return createRelative2(toRefresh, 0);
			}
		};
	}

	@Override
	public NavigationFuture absolute(final Long offset) {
		return new NavigationFuture() {

			@Override
			public LogPointer get() throws IOException {
				return createRelative2(null, offset);
			}
		};
	}

	@Override
	public LogPointer createRelative(final LogPointer source, final long relativeBytePosition) throws IOException {
		return createRelative2(source, relativeBytePosition);
	}

	@Override
	public Navigation<?> getNavigation() {
		return this;
	}

	@Override
	public String getName() {
		return getPath();
	}

}
