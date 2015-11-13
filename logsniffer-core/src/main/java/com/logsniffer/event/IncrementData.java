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
package com.logsniffer.event;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;

import net.sf.json.JSONObject;

/**
 * Increment data container for continuous event scanning process.
 * 
 * @author mbok
 * 
 */
public final class IncrementData {
	private LogPointer nextOffset;
	private JSONObject data = new JSONObject();

	/**
	 * @return the nextOffset
	 */
	public LogPointer getNextOffset() {
		return nextOffset;
	}

	/**
	 * 
	 * @return the next offset transfered to target pointer, if pointer wasn't
	 *         set before, null is returned that corresponds to the start.
	 * 
	 * @throws IOException
	 */
	public LogPointer getNextOffset(final LogPointerFactory pointerFactory) throws IOException {
		return nextOffset != null && StringUtils.isNotBlank(nextOffset.getJson())
				? pointerFactory.getFromJSON(nextOffset.getJson()) : null;
	}

	/**
	 * @param nextOffset
	 *            the nextOffset to set
	 */
	public void setNextOffset(final LogPointer nextOffset) {
		this.nextOffset = nextOffset;
	}

	/**
	 * @return the data
	 */
	public JSONObject getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(final JSONObject data) {
		this.data = data;
	}

}
