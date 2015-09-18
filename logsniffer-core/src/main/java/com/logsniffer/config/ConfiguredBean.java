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

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.logsniffer.app.ContextProvider;

/**
 * Marker interface with required JSON anotations.
 * 
 * @author mbok
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeIdResolver(ConfiguredBean.ConfiguredBeanTypeIdResolver.class)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public interface ConfiguredBean {

	public static class ConfiguredBeanTypeIdResolver implements TypeIdResolver {
		private JavaType baseType;
		private ConfigBeanTypeResolver beanTypeResolver;

		@Override
		public void init(final JavaType baseType) {
			this.baseType = baseType;
			this.beanTypeResolver = ContextProvider.getContext().getBean(
					ConfigBeanTypeResolver.class);
		}

		@Override
		public String idFromValue(final Object value) {
			return this.beanTypeResolver
					.resolveTypeName(((ConfiguredBean) value).getClass());
		}

		@Override
		public String idFromValueAndType(final Object value,
				final Class<?> suggestedType) {
			return idFromValue(value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public String idFromBaseType() {
			return beanTypeResolver
					.resolveTypeName((Class<ConfiguredBean>) baseType
							.getRawClass());
		}

		@Override
		public JavaType typeFromId(final String id) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JavaType typeFromId(final DatabindContext context,
				final String id) {
			return context.constructType(beanTypeResolver.resolveTypeClass(id,
					(Class<ConfiguredBean>) baseType.getRawClass()));
		}

		@Override
		public Id getMechanism() {
			return Id.NAME;
		}

	}

	/**
	 * Delegates post construction for deserialized beans to
	 * {@link BeanConfigFactoryManager#postConstruct(ConfiguredBean)}.
	 * 
	 * @author mbok
	 * 
	 */
	public static class ConfiguredBeanDeserializer extends
			StdDeserializer<ConfiguredBean> implements ResolvableDeserializer {
		private static final long serialVersionUID = 8978550911628758105L;
		private final JsonDeserializer<?> defaultDeserializer;

		protected ConfiguredBeanDeserializer(
				final JsonDeserializer<?> defaultDeserializer) {
			super(ConfiguredBean.class);
			this.defaultDeserializer = defaultDeserializer;
		}

		@Override
		public ConfiguredBean deserialize(final JsonParser jp,
				final DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			ConfiguredBean bean = (ConfiguredBean) defaultDeserializer
					.deserialize(jp, ctxt);
			ContextProvider.getContext()
					.getBean(BeanConfigFactoryManager.class)
					.postConstruct(bean);
			return bean;
		}

		// for some reason you have to implement ResolvableDeserializer when
		// modifying BeanDeserializer
		// otherwise deserializing throws JsonMappingException??
		@Override
		public void resolve(final DeserializationContext ctxt)
				throws JsonMappingException {
			if (defaultDeserializer instanceof ResolvableDeserializer) {
				((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
			}
		}
	}

}
