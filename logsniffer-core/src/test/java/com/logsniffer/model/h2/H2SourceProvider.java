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
package com.logsniffer.model.h2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.file.WildcardLogsSource;

/**
 * Test for {@link H2SourceProvider}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { H2SourceProvider.class, CoreAppConfig.class,
		QaDataSourceAppConfig.class })
@Configuration
public class H2SourceProvider {
	@Bean
	public H2LogSourceProvider sourceProvider() {
		return new H2LogSourceProvider();
	}

	@Bean
	WildcardLogsSource wildcardLogSourceFactory() {
		return new WildcardLogsSource();
	}

	@Autowired
	private H2LogSourceProvider sourceProvider;

	@Test
	public void testPersistence() throws IOException {
		WildcardLogsSource source1 = new WildcardLogsSource();
		source1.setName("Source 1");
		source1.setPattern(new File("src/test/resources/logs", "log*.test")
				.getPath());
		Assert.assertEquals(1, source1.getLogs().size());
		Assert.assertEquals("log1.test",
				FilenameUtils.getName(source1.getLogs().get(0).getPath()));
		long id = sourceProvider.createSource(source1);
		Assert.assertEquals(true, id > 0);
		LogSource<LogInputStream> sourceCheck = sourceProvider
				.getSourceById(id);
		Assert.assertEquals(source1.getName(), sourceCheck.getName());
		Assert.assertEquals(id, sourceCheck.getId());
		Assert.assertEquals(1, sourceCheck.getLogs().size());
		Assert.assertEquals("log1.test",
				FilenameUtils.getName(sourceCheck.getLogs().get(0).getPath()));

		source1.setName("Source 1x");
		source1.setId(id);
		sourceProvider.updateSource(source1);
		sourceCheck = sourceProvider.getSourceById(id);
		Assert.assertEquals("Source 1x", sourceCheck.getName());

	}
}
