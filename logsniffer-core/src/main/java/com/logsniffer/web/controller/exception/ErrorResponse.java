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

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Represents a REST error response.
 * 
 * @author mbok
 * 
 */
public class ErrorResponse {
	private Map<String, String> bindErrors;
	private String exceptionMessage;

	/**
	 * @return the bindErrors
	 */
	public Map<String, String> getBindErrors() {
		return bindErrors;
	}

	/**
	 * @param bindErrors
	 *            the bindErrors to set
	 */
	public void setBindErrors(final Map<String, String> bindErrors) {
		this.bindErrors = bindErrors;
	}

	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * @param exceptionMessage
	 *            the exceptionMessage to set
	 */
	public void setExceptionMessage(final String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * @param exception
	 *            the exception to set
	 */
	public void setException(final Throwable exception) {
		setExceptionMessage(ExceptionUtils.getMessage(exception));
	}
}
