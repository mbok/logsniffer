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

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import com.google.common.base.Defaults;
import com.google.common.primitives.Primitives;
import com.logsniffer.validators.NotDefaultPrimitiveValue.NotDefaultValidator;

/**
 * Constraint to validate primitives for not default values.
 * 
 * @author mbok
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NotDefaultValidator.class)
public @interface NotDefaultPrimitiveValue {
	String message() default "Please specify a value";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class NotDefaultValidator implements
			ConstraintValidator<NotDefaultPrimitiveValue, Object> {
		@Override
		public void initialize(final NotDefaultPrimitiveValue arg0) {
		}

		@Override
		public boolean isValid(final Object value,
				final ConstraintValidatorContext ctx) {
			return value != null
					&& Primitives.isWrapperType(value.getClass())
					&& !Defaults.defaultValue(
							Primitives.unwrap(value.getClass())).equals(value);
		}
	}
}