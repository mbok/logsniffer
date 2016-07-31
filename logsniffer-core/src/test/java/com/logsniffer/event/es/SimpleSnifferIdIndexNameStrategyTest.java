package com.logsniffer.event.es;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.ConfigValueAppConfig;
import com.logsniffer.app.CoreAppConfig;

/**
 * Test for {@link SimpleSnifferIdIndexNameStrategy}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SimpleSnifferIdIndexNameStrategyTest.HelperAppConfig.class, CoreAppConfig.class,
		ConfigValueAppConfig.class })
public class SimpleSnifferIdIndexNameStrategyTest {
	@Value(value = "${" + SimpleSnifferIdIndexNameStrategy.PROP_ES_INDEX_NAME_PREFIX + "}")
	private String indexNamePrefix;

	@Configuration
	public static class HelperAppConfig {
		@Bean
		SimpleSnifferIdIndexNameStrategy strategy() {
			return new SimpleSnifferIdIndexNameStrategy();
		}
	}

	@Autowired
	private SimpleSnifferIdIndexNameStrategy strategy;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(SimpleSnifferIdIndexNameStrategy.PROP_LEGACY_ES_INDEX_NAME, "lf");
	}

	@Test
	public void testGenerating() {
		Assert.assertEquals(indexNamePrefix + 123, strategy.buildActiveName(123));
	}

	@Test
	public void testRetrievalWithoutLegacy() {
		Assert.assertEquals(2, strategy.getRetrievalNames(123).length);
		Assert.assertEquals(indexNamePrefix + 123, strategy.getRetrievalNames(123)[0]);
		Assert.assertEquals("lf", strategy.getRetrievalNames(123)[1]);
	}

}
