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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.logsniffer.util.ReferenceIntegrityException;
import com.logsniffer.web.ViewController;

/**
 * Common controller advice for exception handling.
 * 
 * @author mbok
 * 
 */
@ControllerAdvice(annotations = ViewController.class)
public class ExceptionControllerAdvice {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Handles a {@link ResourceNotFoundException}.
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ModelAndView handleResourceNotFound(
			final ResourceNotFoundException ex) {
		logger.info("Catched resource not found exception", ex);
		ModelAndView mv = new ModelAndView("errors/404");
		mv.addObject("ex", ex);
		return mv;
	}

	@ExceptionHandler(ActionViolationException.class)
	public ModelAndView handleActionViolation(final ActionViolationException ex) {
		logger.info("Catched action violation exception", ex);
		ModelAndView mv = new ModelAndView("errors/action-violation");
		mv.addObject("ex", ex);
		return mv;
	}

	@ExceptionHandler(ReferenceIntegrityException.class)
	public ModelAndView handleActionViolation(
			final ReferenceIntegrityException ex) {
		logger.info("Catched reference integrity violation exception", ex);
		ModelAndView mv = new ModelAndView("errors/ref-intg-violation");
		mv.addObject("ex", ex);
		return mv;
	}
}
