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
package com.logsniffer.util.value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

public class ConfigInjector implements BeanPostProcessor {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Autowired
	private ConversionService conversionService;

	@Autowired
	private ApplicationContext appCtx;

	private ConfigValueSource source;

	private ConfigValueSource getSource() {
		if (source == null) {
			source = appCtx.getBean(ConfigValueSource.class);
		}
		return source;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean,
			final String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean,
			final String beanName) throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
			@Override
			public void doWith(final Field field)
					throws IllegalArgumentException, IllegalAccessException {
				LOGGER.info("Injecting value={} for bean={}", field.getName(),
						beanName);
				field.setAccessible(true);
				final String key = field.getAnnotation(Configured.class)
						.value();
				final String defaultValue = field.getAnnotation(
						Configured.class).defaultValue();
				final Class<?> targetValueType = (Class<?>) ((ParameterizedType) field
						.getGenericType()).getActualTypeArguments()[0];
				field.set(bean, new ConfigValue<Object>() {
					private String oldTextValue;
					private Object oldValue;

					@Override
					public Object get() {
						String textValue = getSource().getValue(key);
						if (textValue == null) {
							textValue = defaultValue;
						}
						if (oldTextValue != null
								&& oldTextValue.equals(textValue)) {
							return oldValue;
						} else {
							oldValue = conversionService.convert(textValue,
									targetValueType);
							oldTextValue = textValue;
							return oldValue;
						}
					}
				});

			}
		}, new FieldFilter() {
			@Override
			public boolean matches(final Field field) {
				return field.getType().equals(ConfigValue.class)
						&& field.isAnnotationPresent(Configured.class);
			}
		});

		return bean;
	}

}
