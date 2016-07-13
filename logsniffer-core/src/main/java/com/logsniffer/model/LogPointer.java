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
package com.logsniffer.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Abstraction of pointing a byte position inside a log. The
 * {@link #equals(Object)} method has to be implemented properly to compare
 * pointers.
 * 
 * @author mbok
 * 
 */
@JsonSerialize(as = LogPointer.class)
public interface LogPointer {
	/**
	 * @return true if this pointer represents the start of log
	 */
	public boolean isSOF();

	/**
	 * @return true if this pointer represents the end of log
	 */
	public boolean isEOF();

	/**
	 * Returns an JSON serialized representation of this pointer.
	 * 
	 * @return an JSON serialized representation of this pointer
	 */
	@JsonRawValue
	public String getJson();
}
