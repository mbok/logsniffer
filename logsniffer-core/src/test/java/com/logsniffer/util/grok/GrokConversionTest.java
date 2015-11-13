package com.logsniffer.util.grok;

import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.fields.FieldsMap;

/**
 * Conversion test for Groks.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, CoreAppConfig.class })
@Configuration
public class GrokConversionTest {
	@Autowired
	private GroksRegistry registry;

	@Test
	public void testIntConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:int}");
		GrokMatcher matcher = grok.matcher("123");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(123, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testLongConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:long}");
		GrokMatcher matcher = grok.matcher(Long.MAX_VALUE + "");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(Long.MAX_VALUE, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testFloatConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:float}");
		GrokMatcher matcher = grok.matcher("3.14");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(3.14f, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testDoubleConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:double}");
		GrokMatcher matcher = grok.matcher("3.1415376485635");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(3.1415376485635d, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testBooleanTrueConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:boolean}");
		GrokMatcher matcher = grok.matcher("true");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(true, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testBooleanFalseConversion() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v:boolean}");
		GrokMatcher matcher = grok.matcher("false");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals(false, fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test
	public void testStringProcessing() throws GrokException {
		Grok grok = Grok.compile(registry, "%{DATA:v}");
		GrokMatcher matcher = grok.matcher("someText");

		FieldsMap fields = new FieldsMap();
		Assert.assertTrue(matcher.matches());
		matcher.setToField("v", fields);
		Assert.assertEquals("someText", fields.get("v"));
		Assert.assertEquals(1, fields.size());
	}

	@Test(expected = PatternSyntaxException.class)
	public void testInvalidConversionType() throws GrokException {
		Grok.compile(registry, "%{DATA:v:unknown}");
	}

	public void testInvalidConversion() throws GrokException {

		FieldsMap fields = new FieldsMap();

		for (String type : new String[] { "int", "long", "float", "double", "boolean" }) {
			Grok grok = Grok.compile(registry, "%{DATA:v:" + type + "}");
			GrokMatcher matcher = grok.matcher("12invalid3");
			Assert.assertTrue(matcher.matches());
			matcher.setToField("v", fields);
			Assert.assertEquals(0, fields.size());
		}
	}
}
