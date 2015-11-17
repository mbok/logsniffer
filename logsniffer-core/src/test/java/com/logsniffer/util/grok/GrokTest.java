package com.logsniffer.util.grok;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;

/**
 * Several tests for {@link Grok}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, CoreAppConfig.class })
@Configuration
public class GrokTest {
	@Autowired
	private GroksRegistry registry;

	@Test
	public void testGroupNamesRightOrderWhenGrokIsFirst() throws GrokException {
		final Grok grok = Grok.compile(registry, "%{DATA:v:int} (?<named>.*)");
		Assert.assertEquals(2, grok.getGroupNames().size());
		final Iterator<String> names = grok.getGroupNames().keySet().iterator();
		Assert.assertEquals("v", names.next());
		Assert.assertEquals("named", names.next());
	}

	@Test
	public void testGroupNamesRightOrderWhenGrokIsLast() throws GrokException {
		final Grok grok = Grok.compile(registry, "(?<named>.*) %{DATA:v:int}");
		Assert.assertEquals(2, grok.getGroupNames().size());
		final Iterator<String> names = grok.getGroupNames().keySet().iterator();
		Assert.assertEquals("named", names.next());
		Assert.assertEquals("v", names.next());
	}
}
