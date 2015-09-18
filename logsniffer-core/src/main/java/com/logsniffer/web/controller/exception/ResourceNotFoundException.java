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
package com.logsniffer.web.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates a not found resource.
 * 
 * @author mbok
 * 
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = 7535908982400189055L;

	private final Class<?> resourceType;

	private final Object id;

	public ResourceNotFoundException(final Class<?> resourceType,
			final Object id, final String internalMsg) {
		super(internalMsg);
		this.resourceType = resourceType;
		this.id = id;
	}

	/**
	 * @return the resourceType
	 */
	public Class<?> getResourceType() {
		return resourceType;
	}

	/**
	 * @return the id
	 */
	public Object getId() {
		return id;
	}

}
