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
package com.logsniffer.model.fields;


/**
 * Mapps fields Java type to a serializable / deserializable type.
 * 
 * @author mbok
 * 
 */
public class FieldJsonMapper {
	/**
	 * Resolves the Java type from given serialized type.
	 * 
	 * @param type
	 *            the simple type
	 * @return the Java type if deserialization is supported for this type
	 *         otherwise null
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> resolveDeserializationType(final String type) {
		try {
			if (type != null) {
				FieldBaseTypes baseType = FieldBaseTypes.valueOf(type);
				return (Class<T>) baseType.getDeserializationType();
			}
		} catch (IllegalArgumentException e) {
			// Accepted
		}
		return null;
	}

	/**
	 * Resolves the simple type from current field value used later for correct
	 * deserialization.
	 * 
	 * @param fieldValue
	 *            field value
	 * @return simple type, in worst case {@link FieldBaseTypes#OBJECT} is
	 *         always returned as fallback
	 */
	public <T> String resolveSerializationType(final T fieldValue) {
		return FieldBaseTypes.resolveType(fieldValue).name();
	}
}
