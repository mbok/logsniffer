package com.logsniffer.event.filter.support;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.logsniffer.event.Event;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry;
import com.logsniffer.reader.FormatException;

/**
 * Test for {@link EntriesFieldsDumper}.
 * 
 * @author mbok
 *
 */
public class EntriesFieldsDumperTest {
	EntriesFieldsDumper filter = new EntriesFieldsDumper();

	@Test
	public void testIgnoreForNonEvents() throws FormatException {
		final FieldsMap fields = new FieldsMap();
		final LogEntry entry = new LogEntry();
		entry.put("abc", 1);
		fields.put(Event.FIELD_ENTRIES, Collections.singletonList(entry));
		filter.filter(fields);
		Assert.assertFalse(fields.containsKey("abc"));
	}

	@Test
	public void testCopyingWithExcludingRawAndPreservingEntries() throws FormatException {
		final Event event = new Event();
		final LogEntry entry = new LogEntry();
		entry.put("abc", 1);
		entry.setRawContent("abceeeeee");
		event.setEntries(Collections.singletonList(entry));
		filter.filter(event);
		Assert.assertTrue(filter.isExcludeRaw());
		Assert.assertEquals(1, event.get("abc"));
		Assert.assertFalse(event.containsKey(LogEntry.FIELD_RAW_CONTENT));
		Assert.assertEquals(2, event.size());
		Assert.assertEquals(entry, event.getEntries().get(0));
		Assert.assertEquals("abceeeeee", event.getEntries().get(0).getRawContent());
	}

	@Test
	public void testCopyingWithIncludingRawAndPreservingEntries() throws FormatException {
		filter.setExcludeRaw(false);
		final Event event = new Event();
		final LogEntry entry = new LogEntry();
		entry.put("abc", 1);
		entry.setRawContent("abceeeeee");
		event.setEntries(Collections.singletonList(entry));
		filter.filter(event);
		Assert.assertEquals(1, event.get("abc"));
		Assert.assertEquals("abceeeeee", event.get(LogEntry.FIELD_RAW_CONTENT));
		Assert.assertEquals(3, event.size());
		Assert.assertEquals(entry, event.getEntries().get(0));
	}

	@Test
	public void testCopyingWithoutPreservingEntries() throws FormatException {
		filter.setDeleteEntries(true);
		final Event event = new Event();
		final LogEntry entry = new LogEntry();
		entry.put("abc", 1);
		entry.setRawContent("abceeeeee");
		event.setEntries(Collections.singletonList(entry));
		filter.filter(event);
		Assert.assertEquals(1, event.get("abc"));
		Assert.assertEquals(1, event.size());
		Assert.assertNull(event.getEntries());
	}
}
