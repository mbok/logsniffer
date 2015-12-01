package com.logsniffer.fields;

import java.util.LinkedHashMap;

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
import com.logsniffer.fields.filter.support.RegexFilter;
import com.logsniffer.reader.FormatException;
import com.logsniffer.util.grok.GrokAppConfig;
import com.logsniffer.util.grok.GrokConsumerConstructor;

/**
 * Test for {@link RegexFilter}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, CoreAppConfig.class, RegexFilterTest.class })
@Configuration
public class RegexFilterTest {
	@Bean
	public GrokConsumerConstructor grokReaderConstructor() {
		return new GrokConsumerConstructor();
	}

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private GrokConsumerConstructor constructor;

	private RegexFilter filter;

	@Before
	public void setUp() {
		filter = new RegexFilter();
		constructor.postConstruct(filter, null);
	}

	@Test
	public void testEmptyKnownFields() throws FormatException {
		filter.getGrokBean().setPattern("abc");
		final LinkedHashMap<String, FieldBaseTypes> knownFields = new LinkedHashMap<>();
		filter.filterKnownFields(knownFields);
		Assert.assertEquals(0, knownFields.size());
	}

	@Test
	public void testRegexAndGrokKnownFields() throws FormatException {
		filter.getGrokBean().setPattern("(?<named>[az]+)%{INT:num:int}");
		final LinkedHashMap<String, FieldBaseTypes> knownFields = new LinkedHashMap<>();
		knownFields.put("e", FieldBaseTypes.BYTE);
		filter.filterKnownFields(knownFields);
		Assert.assertEquals(3, knownFields.size());
		Assert.assertEquals(FieldBaseTypes.BYTE, knownFields.get("e"));
		Assert.assertEquals(FieldBaseTypes.STRING, knownFields.get("named"));
		Assert.assertEquals(FieldBaseTypes.INTEGER, knownFields.get("num"));

	}

	@Test
	public void testRegexAndGrokFiltering() throws FormatException {
		filter.getGrokBean().setPattern("(?<named>[a-z]+)%{INT:num:int}");
		filter.setSourceField("s");
		final FieldsMap fields = new FieldsMap();

		// Not matching
		fields.put("s", "123xyz");
		filter.filter(fields);
		Assert.assertEquals(1, fields.size());

		// Matching
		fields.put("s", "xyz123");
		filter.filter(fields);
		Assert.assertEquals(3, fields.size());
		Assert.assertEquals("xyz123", fields.get("s"));
		Assert.assertEquals("xyz", fields.get("named"));
		Assert.assertEquals(123, fields.get("num"));

	}

	@Test
	public void testIntSourceValue() throws FormatException {
		filter.getGrokBean().setPattern("%{INT:num:int}");
		filter.setSourceField("s");
		final FieldsMap fields = new FieldsMap();
		fields.put("s", 775);
		filter.filter(fields);
		Assert.assertEquals(2, fields.size());
		Assert.assertEquals(775, fields.get("num"));
	}

	@Test
	public void testNullSourceValue() throws FormatException {
		filter.getGrokBean().setPattern("%{INT:num:int}");
		filter.setSourceField("s");
		final FieldsMap fields = new FieldsMap();
		filter.filter(fields);
		Assert.assertEquals(0, fields.size());
	}
}
