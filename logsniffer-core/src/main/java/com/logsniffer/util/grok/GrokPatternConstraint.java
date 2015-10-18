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
package com.logsniffer.util.grok;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.app.ContextProvider;
import com.logsniffer.util.grok.GrokPatternConstraint.GrokPatternValidator;

/**
 * Validates the string input for a valid {@link Grok} syntax.
 * 
 * @author blank08
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = GrokPatternValidator.class)
public @interface GrokPatternConstraint {
	String message() default "Invalid Grok expression";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class GrokPatternValidator implements
			ConstraintValidator<GrokPatternConstraint, String> {
		private static Logger logger = LoggerFactory
				.getLogger(GrokPatternValidator.class);
		private String defaultMsg;

		@Override
		public void initialize(final GrokPatternConstraint ctr) {
			defaultMsg = ctr.message();
		}

		@Override
		public boolean isValid(final String expr,
				final ConstraintValidatorContext ctx) {
			try {
				Grok.compile(
						ContextProvider.getContext().getBean(
								GroksRegistry.class), expr);
				return true;
			} catch (Exception e) {
				logger.info("Input '{}' isn't valid Grok pattern: {}", expr,
						e.getMessage());
				ctx.disableDefaultConstraintViolation();
				ctx.buildConstraintViolationWithTemplate(
						defaultMsg + ": " + e.getMessage())
						.addConstraintViolation();
			}
			return false;
		}
	}
}
