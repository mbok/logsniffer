package com.logsniffer.event.es;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logsniffer.model.support.JsonLogPointer;

/**
 * Deserializes log pointers from ES representation as simple string. This
 * implementation is compatible with previous representation as complex JSON
 * objects.
 * 
 * @author mbok
 *
 */
public class EsLogPointerDeserializer extends JsonDeserializer<JsonLogPointer> {
	private static final Logger LOGGER = LoggerFactory.getLogger(EsLogPointerDeserializer.class);

	@Override
	public JsonLogPointer deserialize(final JsonParser p, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (p.isExpectedStartObjectToken()) {
			String fName;
			while ((fName = p.nextFieldName()) != null) {
				if ("json".equals(fName)) {
					final JsonToken jsonFieldToken = p.nextToken();
					if (jsonFieldToken == JsonToken.START_OBJECT) {
						// Old serialization
						final String jsonPointer = ((ObjectNode) p.readValueAsTree()).toString();
						if (jsonPointer != null) {
							return new JsonLogPointer(jsonPointer);
						}
					}
					break;
				} else if ("_json".equals(fName)) {
					final JsonToken jsonFieldToken = p.nextToken();
					if (jsonFieldToken == JsonToken.VALUE_STRING) {
						// New string value serialization
						final String jsonPointer = p.getValueAsString();
						if (jsonPointer != null) {
							return new JsonLogPointer(jsonPointer);
						}
					}
					break;
				}
			}
		}
		LOGGER.warn("Unable to deserialize log pointer from: {}", ctxt);
		return null;
	}

}
