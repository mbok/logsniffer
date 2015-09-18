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

import net.sf.json.JSONObject;

import com.logsniffer.model.LogPointer;

/**
 * Pointer inside one log.
 * 
 * @author mbok
 * 
 */
public class DefaultPointer implements LogPointer {
	private final long offset;
	private final long size;
	private String json;

	public DefaultPointer(final long offset, final long size) {
		this.offset = offset;
		this.size = size;
	}

	public long getOffset() throws IOException {
		return offset;
	}

	@Override
	public boolean isEOF() {
		return offset >= size;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (offset ^ offset >>> 32);
		return result;
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
		DefaultPointer other = (DefaultPointer) obj;
		if (offset != other.offset) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSOF() {
		return offset == 0;
	}

	@Override
	public String toString() {
		return "SingleFilePointer [offset=" + offset + ", size=" + size + "]";
	}

	@Override
	public String getJson() {
		if (json == null) {
			json = "{\"o\":" + offset + ",\"s\":" + size + "}";
		}
		return json;
	}

	public static DefaultPointer fromJSON(final String data) {
		JSONObject json = JSONObject.fromObject(data);
		if (json.has("o") && json.has("s")) {
			return new DefaultPointer(json.getLong("o"), json.getLong("s"));
		} else {
			return null;
		}
	}

}