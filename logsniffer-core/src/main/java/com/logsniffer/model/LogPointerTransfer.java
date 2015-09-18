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
package com.logsniffer.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.logsniffer.model.LogPointerTransfer.LogPointerTransferDeserializer;
import com.logsniffer.model.support.JsonLogPointer;

/**
 * Transferable log pointer.
 * 
 * @author mbok
 * 
 */

@JsonDeserialize(using = LogPointerTransferDeserializer.class)
public interface LogPointerTransfer {
	/**
	 * Returns an JSON serialized representation of this pointer.
	 * 
	 * @return an JSON serialized representation of this pointer
	 */
	@JsonRawValue
	public String getJson();

	public static class LogPointerTransferDeserializer extends
			JsonDeserializer<LogPointerTransfer> {

		@Override
		public LogPointerTransfer deserialize(final JsonParser jsonParser,
				final DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = jsonParser.getCodec();
			final JsonNode node = oc.readTree(jsonParser);
			return new JsonLogPointer(node.get("json").toString());
		}
	}
}
