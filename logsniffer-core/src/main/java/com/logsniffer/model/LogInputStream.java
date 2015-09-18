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

import java.io.IOException;

/**
 * Log input stream with an abstract pointer concept which allows distinct
 * positioning in a log file. Whenever {@link #getPointer()} returns a pointer
 * with {@link LogPointer#isEOF()} no data is read anymore even more is written
 * in the mean time after the log instance was instantiated. A new log instance
 * has to be retrieved from the source to get the new data.
 * 
 * @author mbok
 * 
 */
public interface LogInputStream {

	/**
	 * Returns the actual position in a stream after read some data.
	 * 
	 * @return actual position in a stream after read some data.
	 */
	public abstract LogPointer getPointer() throws IOException;

}
