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
package com.logsniffer.model.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.ByteLogAccess;
import com.logsniffer.model.support.ByteLogInputStream;
import com.logsniffer.model.support.DefaultPointer;

/**
 * Implements a raw file related log.
 * 
 * @author mbok
 * 
 */
public class DirectFileLogAccess implements ByteLogAccess {

	private final FileLog file;

	public DirectFileLogAccess(final FileLog file) {
		super();
		this.file = file;
	}

	@Override
	public String toString() {
		return "FileLog [file=" + file + "]";
	}

	@Override
	public ByteLogInputStream getInputStream(final LogPointer from) throws IOException {
		final RAFInputStream rafi = new RAFInputStream(new RandomAccessFile(new File(file.getPath()), "r"),
				file.getSize());
		try {
			if (from != null) {
				rafi.seek(Math.min(((DefaultPointer) from).getOffset(), file.getSize()));
			}
			return rafi;
		} catch (final IOException e) {
			rafi.close();
			throw e;
		}
	}

	@Override
	public long getDifference(final LogPointer source, final LogPointer compareTo) throws IOException {
		final long start = source != null ? ((DefaultPointer) source).getOffset() : 0;
		return ((DefaultPointer) compareTo).getOffset() - start;
	}

	@Override
	public LogPointer createRelative2(final LogPointer _source, final long relativeBytePosition) throws IOException {
		return null;
	}

	@Override
	public LogPointer createRelative(final LogPointer _source, final long relativeBytePosition) throws IOException {
		final DefaultPointer source = (DefaultPointer) _source;
		final long newOffset = (source != null ? source.getOffset() : 0) + relativeBytePosition;
		return new DefaultPointer(Math.max(0, Math.min(newOffset, file.getSize())), file.getSize());
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DirectFileLogAccess other = (DirectFileLogAccess) obj;
		return file.equals(other.file);
	}

	@Override
	public LogPointer getFromJSON(final String data) throws IOException {
		return DefaultPointer.fromJSON(data);
	}

	@Override
	public LogPointer end() throws IOException {
		return createRelative2(null, file.getSize());
	}

	@Override
	public LogPointer start() throws IOException {
		return createRelative2(null, 0);
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

}
