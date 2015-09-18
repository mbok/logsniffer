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

import com.logsniffer.model.Log;

/**
 * Implements a file related log.
 * 
 * @author mbok
 * 
 */
public class FileLog implements Log {
	private final long lastModified;
	private final long size;
	private final String path;

	public FileLog(final File file) {
		super();
		this.lastModified = file.lastModified();
		this.path = file.getPath();
		this.size = file.length();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "FileLog [file=" + path + "]";
	}

	@Override
	public int hashCode() {
		return path.hashCode();
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
		FileLog other = (FileLog) obj;
		return path.equals(other.path);
	}

	@Override
	public SizeMetric getSizeMetric() {
		return SizeMetric.BYTE;
	}

}
