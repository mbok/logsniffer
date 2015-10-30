package com.logsniffer.event.support;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.event.EventData;
import com.logsniffer.model.LogEntry;
import com.logsniffer.reader.FormatException;
import com.logsniffer.util.grok.GrokAppConfig;
import com.logsniffer.util.grok.GrokConsumerConstructor;

/**
 * Test for {@link RegexPatternScanner}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, CoreAppConfig.class, RegexPatternScannerTest.class })
@Configuration
public class RegexPatternScannerTest {
	@Bean
	public GrokConsumerConstructor grokReaderConstructor() {
		return new GrokConsumerConstructor();
	}

	@Autowired
	private GrokConsumerConstructor constructor;

	private RegexPatternScanner scanner;

	@Before
	public void setUp() {
		scanner = new RegexPatternScanner();
		constructor.postConstruct(scanner, null);
	}

	@Test
	public void testNullSource() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd.*)");
		LogEntry entry = new LogEntry();
		EventData event = scanner.matches(entry);
		// Asserts
		Assert.assertNull(event);
	}

	@Test
	public void testNamedCaptures() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd).*");
		LogEntry entry = new LogEntry();
		entry.getFields().put("source", "abcdef");
		EventData event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.getFields().size());
		Assert.assertEquals("abcd", event.getFields().get("MyField"));
	}

	@Test
	public void testMultipleNamedCaptures() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd)(?<RestField>.+)");
		LogEntry entry = new LogEntry();
		entry.getFields().put("source", "abcdef");
		EventData event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(2, event.getFields().size());
		Assert.assertEquals("abcd", event.getFields().get("MyField"));
		Assert.assertEquals("ef", event.getFields().get("RestField"));
	}

	@Test
	public void testNamedCapturesWithSubPattern() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd)");
		scanner.getGrokBean().setSubStringSearch(true);
		LogEntry entry = new LogEntry();
		entry.getFields().put("source", "wabcdef");
		EventData event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.getFields().size());
		Assert.assertEquals("abcd", event.getFields().get("MyField"));
	}

	@Test
	public void testGrokWithTypeConversion() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("%{INT:MyIntField:int}");
		scanner.getGrokBean().setSubStringSearch(false);
		LogEntry entry = new LogEntry();
		entry.getFields().put("source", "777");
		EventData event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.getFields().size());
		Assert.assertEquals(777, event.getFields().get("MyIntField"));
	}
}
