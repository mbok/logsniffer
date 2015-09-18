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

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Abstraction of static log metadata.
 * 
 * @author mbok
 * 
 */
@JsonTypeInfo(property = "@type", include = As.PROPERTY, use = Id.NAME)
@JsonTypeName("common")
public interface Log {
	public static class SizeMetric {
		public static SizeMetric BYTE = new SizeMetric();
	}

	/**
	 * Path to the log file used as identifier.
	 * 
	 * @return path to the log file
	 */
	public String getPath();

	/**
	 * 
	 * @return size of the log related to the underlying metric
	 */
	public long getSize();

	/**
	 * 
	 * @return metric size of log is measured
	 */
	@JsonIgnore
	public SizeMetric getSizeMetric();

	/**
	 * 
	 * @return last modification date in analogy to {@link File#lastModified()}.
	 */
	public long getLastModified();

}
