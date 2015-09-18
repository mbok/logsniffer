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
package com.logsniffer.util.value;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.ConfigValueAppConfig;
import com.logsniffer.util.value.ConfigInjectorTest.HelperAppConfig;

/**
 * Test for {@link ConfigInjector}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigValueAppConfig.class,
		HelperAppConfig.class })
public class ConfigInjectorTest {

	@Configuration
	public static class HelperAppConfig {
		@Bean
		public ConversionService conversionService() {
			return new DefaultFormattingConversionService();
		}

		@Bean
		@Primary
		public ConfigValueSource source() {
			return Mockito.mock(ConfigValueSource.class);
		}
	}

	@Autowired
	private ConfigValueSource source;

	@Configured(value = "test.abc", defaultValue = "def")
	private ConfigValue<String> testDefault;

	@Configured("test.bool")
	private ConfigValue<Boolean> testBool;

	@Test
	public void testDefaultValue() {
		Assert.assertEquals("def", testDefault.get());
		Mockito.when(source.getValue("test.bool")).thenReturn("true");
		Assert.assertEquals(true, testBool.get());
	}
}
