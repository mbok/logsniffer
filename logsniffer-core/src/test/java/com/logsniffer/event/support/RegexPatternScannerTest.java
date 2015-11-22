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
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.event.Event;
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
	private BeanConfigFactoryManager configManager;

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
		final LogEntry entry = new LogEntry();
		final Event event = scanner.matches(entry);
		// Asserts
		Assert.assertNull(event);
	}

	@Test
	public void testNamedCaptures() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd).*");
		final LogEntry entry = new LogEntry();
		entry.put("source", "abcdef");
		final Event event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.size());
		Assert.assertEquals("abcd", event.get("MyField"));
	}

	@Test
	public void testMultipleNamedCaptures() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd)(?<RestField>.+)");
		final LogEntry entry = new LogEntry();
		entry.put("source", "abcdef");
		final Event event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(2, event.size());
		Assert.assertEquals("abcd", event.get("MyField"));
		Assert.assertEquals("ef", event.get("RestField"));
	}

	@Test
	public void testNamedCapturesWithSubPattern() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("(?<MyField>abcd)");
		scanner.getGrokBean().setSubStringSearch(true);
		final LogEntry entry = new LogEntry();
		entry.put("source", "wabcdef");
		final Event event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.size());
		Assert.assertEquals("abcd", event.get("MyField"));
	}

	@Test
	public void testGrokWithTypeConversion() throws FormatException {
		scanner.setSourceField("source");
		scanner.getGrokBean().setPattern("%{INT:MyIntField:int}");
		scanner.getGrokBean().setSubStringSearch(false);
		final LogEntry entry = new LogEntry();
		entry.put("source", "777");
		final Event event = scanner.matches(entry);
		// Asserts
		Assert.assertNotNull(event);
		Assert.assertEquals(1, event.size());
		Assert.assertEquals(777, event.get("MyIntField"));
	}

	@Test
	public void testGrokDexserializationWithUnwrappedFields() throws FormatException {
		final String json = "{\"@type\":\"RegexPatternScanner\",\"pattern\":\"%{INT:MyIntField:int}\",\"subStringSearch\":false,\"multiLine\":false,\"dotAll\":false,\"caseInsensitive\":false,\"sourceField\":\"source\",\"fieldTypes\":{\"MyIntField\":\"INTEGER\"}}";
		final RegexPatternScanner s = configManager.createBeanFromJSON(RegexPatternScanner.class, json);
		Assert.assertEquals("%{INT:MyIntField:int}", s.getGrokBean().getPattern());
		Assert.assertEquals(false, s.getGrokBean().isCaseInsensitive());
		Assert.assertEquals(false, s.getGrokBean().isMultiLine());
		Assert.assertEquals(false, s.getGrokBean().isDotAll());
		Assert.assertEquals(false, s.getGrokBean().isSubStringSearch());
	}
}
