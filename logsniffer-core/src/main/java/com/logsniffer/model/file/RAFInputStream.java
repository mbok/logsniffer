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

import java.io.IOException;
import java.io.RandomAccessFile;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.ByteLogInputStream;
import com.logsniffer.model.support.DefaultPointer;

/**
 * Log input stream based on {@link RandomAccessFile}.
 * 
 * @author mbok
 * 
 */
public class RAFInputStream extends ByteLogInputStream {
	private final RandomAccessFile file;
	private final long size;

	public RAFInputStream(final RandomAccessFile file, final long size) {
		this.file = file;
		this.size = size;
	}

	@Override
	public LogPointer getPointer() throws IOException {
		return new DefaultPointer(file.getFilePointer(), size);
	}

	public void seek(final long pos) throws IOException {
		file.seek(pos);
	}

	@Override
	public int read() throws IOException {
		if (file.getFilePointer() < size) {
			return file.read();
		} else {
			return -1;
		}
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final long p = file.getFilePointer();
		if (p >= size) {
			return -1;
		} else if (p + len > size) {
			return file.read(b, off, (int) (size - p));
		} else {
			return file.read(b, off, len);
		}
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			this.file.close();
		}
	}

}
