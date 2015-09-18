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
package com.logsniffer.util;

/**
 * Represents a reference integrity violation in case of deletions.
 * 
 * @author mbok
 * 
 */
public class ReferenceIntegrityException extends Exception {

	private static final long serialVersionUID = 448286180205979538L;
	private final Class<?> resourceType;

	/**
	 * @param message
	 * @param cause
	 */
	public ReferenceIntegrityException(final Class<?> resourceType,
			final Throwable cause) {
		super(cause);
		this.resourceType = resourceType;
	}

	/**
	 * @return the resourceType
	 */
	public Class<?> getResourceType() {
		return resourceType;
	}
}
