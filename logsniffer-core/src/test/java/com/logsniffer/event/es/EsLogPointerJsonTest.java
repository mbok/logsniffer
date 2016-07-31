package com.logsniffer.event.es;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.event.Event;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.DefaultPointer;
import com.logsniffer.model.support.JsonLogPointer;

import net.sf.json.JSONObject;

/**
 * Test for {@link EsLogPointerDeserializer} and {@link EsLogPointerSerializer}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
public class EsLogPointerJsonTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ObjectMapper jsonMapper;

	@Before
	public void setUp() {
		jsonMapper = new ObjectMapper();
		jsonMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.registerSubtypes(LogEntry.class);
		final SimpleModule esModule = new SimpleModule();
		esModule.addSerializer(LogPointer.class, new EsLogPointerSerializer());
		esModule.addDeserializer(LogPointer.class, new EsLogPointerDeserializer());
		esModule.addDeserializer(JsonLogPointer.class, new EsLogPointerDeserializer());
		jsonMapper.registerModule(esModule);
	}

	@Test
	public void testNewJsonStringRepresentation() throws IOException {
		final Event event = new Event();
		final DefaultPointer p = new DefaultPointer(19, 999);
		event.put("my", p);
		event.setId("123");
		event.setSnifferId(123);
		event.setTimestamp(new Date());
		event.setLogPath("path");
		event.setPublished(new Date());

		// Serialiazion
		final String json = jsonMapper.writeValueAsString(event);
		logger.info("Serialized {} to: {}", event, json);

		// Deserialization
		final Event test = jsonMapper.readValue(json, Event.class);
		Assert.assertEquals(event.getId(), test.getId());
		Assert.assertEquals(event.getSnifferId(), test.getSnifferId());
		Assert.assertEquals(event.getTimestamp(), test.getTimestamp());
		Assert.assertEquals(event.getPublished(), test.getPublished());
		Assert.assertEquals(event.getLogPath(), test.getLogPath());
		Assert.assertTrue(test.get("my") instanceof LogPointer);
		final DefaultPointer testPointer = DefaultPointer.fromJSON(((LogPointer) test.get("my")).getJson());
		Assert.assertEquals(19, testPointer.getOffset());
		Assert.assertEquals(999, testPointer.getSize());
	}

	@Test
	public void testOldSerializationWithNativeJsonLogPointerRepresentation() throws IOException {
		final String oldJson = "{\"@types\":{\"lf_entries\":[\"LENTRY\"],\"lf_timestamp\":\"DATE\",\"lf_snifferId\":\"LONG\",\"lf_logSourceId\":\"LONG\",\"lf_logPath\":\"STRING\",\"lf_published\":\"DATE\"},\"lf_entries\":[{\"@types\":{\"date\":\"STRING\",\"lf_timestamp\":\"DATE\",\"priority\":\"STRING\",\"lf_severity\":\"SEVERITY\",\"category\":\"STRING\",\"message\":\"STRING\",\"lf_startOffset\":\"LPOINTER\",\"lf_raw\":\"STRING\",\"lf_endOffset\":\"LPOINTER\"},\"date\":\"2016-04-08 23:36:46,736\",\"lf_timestamp\":1460151406736,\"priority\":\"DEBUG\",\"lf_severity\":{\"o\":5,\"c\":7,\"n\":\"DEBUG\"},\"category\":\"com.logsniffer.event.h2.H2SnifferPersistence\",\"message\":\"Storing inc data for sniffer=com.logsniffer.event.SnifferPersistence$AspectSniffer@6275df71, source=com.logsniffer.model.h2.H2LogSourceProvider$SourceRowMapper$1@664a57e7 and log=RollingLog [liveLog=FileLog [file=/home/mbok/logsniffer/logs/logsniffer.log]] with next offset: RollingLogPointer [path=/home/mbok/logsniffer/logs/logsniffer.log, filePointer=SingleFilePointer [offset=8026804, size=36465380]]\",\"lf_startOffset\":{\"json\":{\"p\":\"/home/mbok/logsniffer/logs/logsniffer.log.2016-04-08\",\"l\":false,\"f\":false,\"h\":-97277146,\"u\":{\"o\":61187613,\"s\":643460743}},\"eof\":false,\"sof\":false},\"lf_raw\":\"2016-04-08 23:36:46,736 DEBUG [com.logsniffer.event.h2.H2SnifferPersistence] Storing inc data for sniffer=com.logsniffer.event.SnifferPersistence$AspectSniffer@6275df71, source=com.logsniffer.model.h2.H2LogSourceProvider$SourceRowMapper$1@664a57e7 and log=RollingLog [liveLog=FileLog [file=/home/mbok/logsniffer/logs/logsniffer.log]] with next offset: RollingLogPointer [path=/home/mbok/logsniffer/logs/logsniffer.log, filePointer=SingleFilePointer [offset=8026804, size=36465380]]\",\"lf_endOffset\":{\"json\":{\"p\":\"/home/mbok/logsniffer/logs/logsniffer.log.2016-04-08\",\"l\":false,\"f\":false,\"h\":-97277146,\"u\":{\"o\":61188095,\"s\":643460743}},\"eof\":false,\"sof\":false}}],\"lf_timestamp\":1460151406736,\"lf_snifferId\":1,\"lf_logSourceId\":1,\"lf_logPath\":\"/home/mbok/logsniffer/logs/logsniffer.log\",\"lf_published\":1469964960113}";
		final Event test = jsonMapper.readValue(oldJson, Event.class);
		Assert.assertTrue(test.getEntries().get(0).getStartOffset() instanceof LogPointer);
		Assert.assertTrue("Invalid pointer data: " + test.getEntries().get(0).getStartOffset().getJson(),
				test.getEntries().get(0).getStartOffset().getJson().contains("logsniffer"));
		Assert.assertNotNull(JSONObject.fromObject(test.getEntries().get(0).getStartOffset().getJson()));

		Assert.assertTrue(test.getEntries().get(0).getEndOffset() instanceof LogPointer);
		Assert.assertTrue("Invalid pointer data: " + test.getEntries().get(0).getEndOffset().getJson(),
				test.getEntries().get(0).getEndOffset().getJson().contains("logsniffer"));
		Assert.assertNotNull(JSONObject.fromObject(test.getEntries().get(0).getEndOffset().getJson()));
	}
}
