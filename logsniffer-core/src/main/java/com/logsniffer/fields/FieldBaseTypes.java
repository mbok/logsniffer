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
package com.logsniffer.fields;

import java.awt.List;
import java.util.Collection;
import java.util.Date;

import com.google.common.primitives.Primitives;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.support.JsonLogPointer;

/**
 * Field type enum.
 * 
 * @author mbok
 * 
 */
public enum FieldBaseTypes {
	BYTE(byte.class), BOOLEAN(boolean.class), STRING(String.class), DATE(Date.class), SEVERITY(
			SeverityLevel.class), INTEGER(int.class), LONG(long.class), FLOAT(float.class), DOUBLE(
					double.class), LPOINTER(LogPointer.class, JsonLogPointer.class), LENTRY(LogEntry.class), FIELDS_MAP(
							FieldsMap.class), LIST(Collection.class, List.class), OBJECT(null);

	private Class<?> deserializationType;
	private Class<?> serializationType;

	FieldBaseTypes(final Class<?> javaType) {
		this.deserializationType = javaType;
		this.serializationType = javaType;
	}

	FieldBaseTypes(final Class<?> serializationType, final Class<?> deserializationType) {
		this.serializationType = serializationType;
		this.deserializationType = deserializationType;
	}

	public static FieldBaseTypes resolveType(final Object v) {
		if (v != null) {
			for (final FieldBaseTypes t : values()) {
				if (t.serializationType == null) {
					continue;
				}
				Class<?> vc = v.getClass();
				if (Primitives.isWrapperType(vc)) {
					vc = Primitives.unwrap(vc);
				}
				if (t.serializationType.isAssignableFrom(vc)) {
					return t;
				}
			}
		}
		return OBJECT;
	}

	/**
	 * @return the deserializationType
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<? super T> getDeserializationType() {
		return (Class<? super T>) deserializationType;
	}

	/**
	 * @return the deserializationType
	 */
	@SuppressWarnings("unchecked")
	public Class<Object> getSerializationType() {
		return (Class<Object>) serializationType;
	}

}
