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
package com.logsniffer.app;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.MapType;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.model.fields.FieldJsonMapper;
import com.logsniffer.model.fields.FieldsMap;
import com.logsniffer.model.fields.FieldsMap.FieldsMapMixInLikeSerializer;

/**
 * Core app config.
 * 
 * @author mbok
 * 
 */
@Configuration
@Import(StartupAppConfig.class)
public class CoreAppConfig {
	public static final String BEAN_LOGSNIFFER_PROPS = "logSnifferProps";
	public static final String LOGSNIFFER_PROPERTIES_FILE = "config.properties";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Registers the {@link ContextProvider}.
	 * 
	 * @return the context provider.
	 */
	@Bean
	public ContextProvider contextProvider() {
		return new ContextProvider();
	}

	@Bean(name = { BEAN_LOGSNIFFER_PROPS })
	@Autowired
	public PropertiesFactoryBean logSnifferProperties(
			final ApplicationContext ctx) throws IOException {
		if (ctx.getEnvironment().acceptsProfiles(
				"!" + ContextProvider.PROFILE_NONE_QA)) {
			File qaFile = File.createTempFile("logsniffer", "qa");
			qaFile.delete();
			String qaHomeDir = qaFile.getPath();
			logger.info("QA mode active, setting random home directory: {}",
					qaHomeDir);
			System.setProperty("logsniffer.home", qaHomeDir);
		}
		PathMatchingResourcePatternResolver pathMatcher = new PathMatchingResourcePatternResolver();
		Resource[] classPathProperties = pathMatcher
				.getResources("classpath*:/config/**/logsniffer-*.properties");
		Resource[] metainfProperties = pathMatcher
				.getResources("classpath*:/META-INF/**/logsniffer-*.properties");
		PropertiesFactoryBean p = new PropertiesFactoryBean();
		for (Resource r : metainfProperties) {
			classPathProperties = (Resource[]) ArrayUtils.add(
					classPathProperties, r);
		}
		classPathProperties = (Resource[]) ArrayUtils.add(classPathProperties,
				new FileSystemResource(System.getProperty("logsniffer.home")
						+ "/" + LOGSNIFFER_PROPERTIES_FILE));
		p.setLocations(classPathProperties);
		p.setLocalOverride(false);
		p.setIgnoreResourceNotFound(true);
		return p;
	}

	/**
	 * Returns a general properties placeholder configurer based on
	 * {@link #logSnifferProperties()}.
	 * 
	 * @param props
	 *            autowired logSnifferProperties bean
	 * @return A general properties placeholder configurer.
	 * @throws IOException
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@Autowired
	public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(
			@Qualifier(BEAN_LOGSNIFFER_PROPS) final Properties props)
			throws IOException {
		PropertyPlaceholderConfigurer c = new PropertyPlaceholderConfigurer();
		c.setIgnoreResourceNotFound(true);
		c.setIgnoreUnresolvablePlaceholders(true);
		c.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
		c.setProperties(props);
		return c;
	}

	@Bean
	public ObjectMapper jsonObjectMapper() {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		jsonMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jsonMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);

		SimpleModule module = new SimpleModule("FieldsMapping",
				Version.unknownVersion());
		module.setSerializerModifier(new BeanSerializerModifier() {
			@Override
			public JsonSerializer<?> modifyMapSerializer(
					final SerializationConfig config, final MapType valueType,
					final BeanDescription beanDesc,
					final JsonSerializer<?> serializer) {
				if (FieldsMap.class.isAssignableFrom(valueType.getRawClass())) {
					return new FieldsMapMixInLikeSerializer();
				} else {
					return super.modifyMapSerializer(config, valueType,
							beanDesc, serializer);
				}
			}
		});
		jsonMapper.registerModule(module);
		return jsonMapper;
	}

	/**
	 * Used for proper serilization/deserilization of {@link FieldsMap}s.
	 * 
	 * @return
	 */
	@Bean
	public FieldJsonMapper fieldJsonMapper() {
		return new FieldJsonMapper();
	}

	/**
	 * Used for proper serilization/deserilization of {@link ConfiguredBean}s as
	 * key concept for persisting models in logsniffer.
	 * 
	 * @return a {@link BeanConfigFactoryManager} instance
	 */
	@Bean
	public BeanConfigFactoryManager beanConfigFactoryManager() {
		return new BeanConfigFactoryManager();
	}
}
