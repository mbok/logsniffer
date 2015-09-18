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
package com.logsniffer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.logsniffer.config.ConfiguredBean.ConfiguredBeanDeserializer;

/**
 * Manages creating, serialization and deserialization of bean configs.
 * 
 * @author mbok
 * 
 */
public class BeanConfigFactoryManager implements ConfigBeanTypeResolver {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	private BeanPostConstructor<?>[] postConstructors;

	private final HashMap<Class<?>, BeanPostConstructor<?>> mappedPostConstrucors = new HashMap<Class<?>, BeanPostConstructor<?>>();

	@Autowired
	private ObjectMapper jsonMapper;

	private final Map<Class<? extends ConfiguredBean>, List<String>> configBeanNames = new HashMap<>();;

	@SuppressWarnings("unchecked")
	@PostConstruct
	private void initJsonMapper() {
		SimpleModule module = new SimpleModule();
		module.setDeserializerModifier(new BeanDeserializerModifier() {
			@Override
			public JsonDeserializer<?> modifyDeserializer(
					final DeserializationConfig config,
					final BeanDescription beanDesc,
					final JsonDeserializer<?> deserializer) {
				if (ConfiguredBean.class.isAssignableFrom(beanDesc
						.getBeanClass())) {
					return new ConfiguredBeanDeserializer(deserializer);
				}
				return deserializer;
			}
		});
		jsonMapper.registerModule(module);
		if (postConstructors != null) {
			for (BeanPostConstructor<?> bpc : postConstructors) {
				mappedPostConstrucors.put(bpc.getClass(), bpc);
			}
		}

		// Register sub beans
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		final AssignableTypeFilter filter4configBenas = new AssignableTypeFilter(
				ConfiguredBean.class);
		scanner.addIncludeFilter(filter4configBenas);

		for (BeanDefinition bd : scanner
				.findCandidateComponents("com.logsniffer")) {
			try {
				Class<? extends ConfiguredBean> clazz = (Class<? extends ConfiguredBean>) Class
						.forName(bd.getBeanClassName());
				JsonTypeName jsonNameAnnotation = clazz
						.getAnnotation(JsonTypeName.class);
				List<String> names = new ArrayList<String>();
				configBeanNames.put(clazz, names);
				if (jsonNameAnnotation != null) {
					names.add(jsonNameAnnotation.value());
					if (jsonNameAnnotation.deprecated() != null) {
						for (String dep : jsonNameAnnotation.deprecated()) {
							names.add(dep);
						}
					}
				}
				names.add(clazz.getSimpleName());
				logger.debug("Registered JSON type {} for following names: {}",
						clazz, names);
			} catch (ClassNotFoundException e) {
				logger.warn(
						"Failed to register JSON type name for "
								+ bd.getBeanClassName(), e);
			}
		}
	}

	/**
	 * Serializes the bean to JSON.
	 * 
	 * @param config
	 *            the config to serialize
	 * @param json
	 *            the JSON object to serialize config data to
	 * @throws ConfigException
	 *             in case of serialize errors
	 */
	public <BeanType extends ConfiguredBean> String saveBeanToJSON(
			final BeanType bean) throws ConfigException {
		try {
			String str = jsonMapper.writeValueAsString(bean);
			return str;
		} catch (Exception e) {
			throw new ConfigException("Failed to serialize bean: " + bean, e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <BeanType> void postConstruct(final ConfiguredBean bean) {
		if (bean == null) {
			return;
		}
		PostConstructed pc = AnnotationUtils.findAnnotation(bean.getClass(),
				PostConstructed.class);
		if (bean instanceof BeanPostConstructor<?>
				&& (pc == null || !mappedPostConstrucors.containsKey(bean
						.getClass()))) {
			((BeanPostConstructor) bean).postConstruct(bean, this);
		}
		if (pc != null) {
			BeanPostConstructor bpc = mappedPostConstrucors.get(pc
					.constructor());
			if (bpc != null) {
				bpc.postConstruct(bean, this);
			} else {
				logger.error(
						"Unsatisfied bean construction of '{}' due to missing post constructor of type: {}",
						bean, pc.getClass());
			}
		}
	}

	/**
	 * Creates a bean related to the given config. The creation is performed by
	 * the corresponding bean factory.
	 * 
	 * @param clazz
	 *            the bean class
	 * @param config
	 *            the config to use
	 * @return desired configured bean
	 * @throws ConfigException
	 *             in case of errors
	 */
	public <BeanType extends ConfiguredBean> BeanType createBeanFromJSON(
			final Class<BeanType> clazz, final String json)
			throws ConfigException {
		try {
			BeanType bean = jsonMapper.readValue(json, clazz);
			return bean;
		} catch (Exception e) {
			throw new ConfigException("Failed to deserialize bean: " + clazz, e);
		}
	}

	@Override
	public String resolveTypeName(final Class<? extends ConfiguredBean> clazz)
			throws ConfigException {
		if (configBeanNames.containsKey(clazz)
				&& !configBeanNames.get(clazz).isEmpty()) {
			return configBeanNames.get(clazz).get(0);
		}
		throw new ConfigException("No name defined for type: " + clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ConfiguredBean> Class<? extends T> resolveTypeClass(
			final String searchNname, final Class<T> wantedSuperType)
			throws ConfigException {
		for (Class<? extends ConfiguredBean> clazz : configBeanNames.keySet()) {
			if (wantedSuperType == null
					|| wantedSuperType.isAssignableFrom(clazz)) {
				for (String name : configBeanNames.get(clazz)) {
					if (name.equalsIgnoreCase(searchNname)) {
						return (Class<? extends T>) clazz;
					}
				}
			}
		}
		throw new ConfigException("Couldn't resolve type for name '"
				+ searchNname + "' of type base: " + wantedSuperType);
	}
}
