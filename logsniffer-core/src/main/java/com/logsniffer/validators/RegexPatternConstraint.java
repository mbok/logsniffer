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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.validators.RegexPatternConstraint.RegexPatternValidator;

/**
 * Validates the string input for a valid {@link Pattern} syntax.
 * 
 * @author blank08
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RegexPatternValidator.class)
public @interface RegexPatternConstraint {
	String message() default "Invalid regular expression";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class RegexPatternValidator implements
			ConstraintValidator<RegexPatternConstraint, String> {
		private static Logger logger = LoggerFactory
				.getLogger(RegexPatternValidator.class);
		private String defaultMsg;

		@Override
		public void initialize(final RegexPatternConstraint ctr) {
			defaultMsg = ctr.message();
		}

		@Override
		public boolean isValid(final String expr,
				final ConstraintValidatorContext ctx) {
			try {
				Pattern.compile(expr);
				return true;
			} catch (PatternSyntaxException e) {
				logger.info("Input '{}' isn't valid regex pattern: {}", expr,
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