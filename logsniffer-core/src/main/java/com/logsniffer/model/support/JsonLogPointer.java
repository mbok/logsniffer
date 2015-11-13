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

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.logsniffer.model.LogPointer;

/**
 * JSON nested transferable log pointer impl.
 * 
 * @author mbok
 * 
 */
public class JsonLogPointer implements LogPointer {
	@JsonRawValue
	private String json;

	private boolean sof;
	private boolean eof;

	public JsonLogPointer(final String json) {
		super();
		this.json = json;
	}

	public JsonLogPointer() {
		super();
	}

	@Override
	public String getJson() {
		return json;
	}

	/**
	 * @param json
	 *            the json to set
	 */
	public void setJson(final JsonNode node) {
		this.json = node.toString();
	}

	@Override
	public boolean isSOF() {
		return sof;
	}

	@Override
	public boolean isEOF() {
		return eof;
	}

	/**
	 * @param sof
	 *            the sof to set
	 */
	public void setSOF(final boolean sof) {
		this.sof = sof;
	}

	/**
	 * @param eof
	 *            the eof to set
	 */
	public void setEOF(final boolean eof) {
		this.eof = eof;
	}

}
