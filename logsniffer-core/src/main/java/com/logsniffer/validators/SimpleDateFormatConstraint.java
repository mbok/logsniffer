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
package com.logsniffer.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.validators.RegexPatternConstraint.RegexPatternValidator;
import com.logsniffer.validators.SimpleDateFormatConstraint.SimpleDateFormatValidator;

/**
 * Validates a String input for the proper format regarding
 * {@link SimpleDateFormat}.
 *
 * @author mbok
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SimpleDateFormatValidator.class)
public @interface SimpleDateFormatConstraint {
	String message() default "Invalid date format expression";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class SimpleDateFormatValidator implements
			ConstraintValidator<SimpleDateFormatConstraint, String> {
		private static Logger logger = LoggerFactory
				.getLogger(RegexPatternValidator.class);
		private String defaultMsg;

		@Override
		public void initialize(final SimpleDateFormatConstraint ctr) {
			defaultMsg = ctr.message();
		}

		@Override
		public boolean isValid(String source, ConstraintValidatorContext ctx) {
			try {
				new SimpleDateFormat(source);
				return true;
			} catch (IllegalArgumentException e) {
				logger.info("Input '{}' isn't valid simple date format: {}", source,
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
