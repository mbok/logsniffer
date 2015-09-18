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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.logsniffer.config.WrappedBean.WrapperSerializer;

/**
 * Wrapper bean for lazy unmarshalling of configured beans. Specially used to
 * delegate JSON serialization to the wrapped bean.
 * 
 * @author mbok
 * 
 * @param <BeanType>
 *            the wrapped bean type
 */
@JsonSerialize(using = WrapperSerializer.class)
public interface WrappedBean<BeanType extends ConfiguredBean> extends
		ConfiguredBean {
	public static class WrapperSerializer extends
			JsonSerializer<WrappedBean<ConfiguredBean>> {

		@Override
		public void serialize(WrappedBean<ConfiguredBean> value,
				JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonProcessingException {
			provider.defaultSerializeValue(value.getWrapped(), jgen);
		}

		@Override
		public void serializeWithType(WrappedBean<ConfiguredBean> value,
				JsonGenerator jgen, SerializerProvider provider,
				TypeSerializer typeSer) throws IOException,
				JsonProcessingException {
			provider.defaultSerializeValue(value.getWrapped(), jgen);
		}

	}

	BeanType getWrapped() throws ConfigException;
}
