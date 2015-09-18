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
import java.text.ParseException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.validators.CronExprConstraint.CronExprValidator;

/**
 * Validates the string input for a valid {@link CronExpression} syntax.
 * 
 * @author blank08
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CronExprValidator.class)
public @interface CronExprConstraint {
	String message() default "Cron expression syntax error";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class CronExprValidator implements
			ConstraintValidator<CronExprConstraint, String> {
		private static Logger logger = LoggerFactory
				.getLogger(CronExprValidator.class);

		@Override
		public void initialize(final CronExprConstraint arg0) {
		}

		@Override
		public boolean isValid(final String expr,
				final ConstraintValidatorContext ctx) {
			try {
				if (expr != null) {
					CronScheduleBuilder
							.cronScheduleNonvalidatedExpression(expr);
					return true;
				}
			} catch (ParseException e) {
				logger.info("Expression '{}' isn't valid: {}", expr,
						e.getMessage());
			}
			return false;
		}

	}
}