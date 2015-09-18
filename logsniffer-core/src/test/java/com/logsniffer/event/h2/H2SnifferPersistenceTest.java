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
package com.logsniffer.event.h2;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.ConfigException;
import com.logsniffer.event.IncrementData;
import com.logsniffer.event.Publisher;
import com.logsniffer.event.Scanner;
import com.logsniffer.event.Scanner.LogEntryReaderStrategyWrapper;
import com.logsniffer.event.Sniffer;
import com.logsniffer.event.SnifferPersistence.AspectSniffer;
import com.logsniffer.event.SnifferScheduler;
import com.logsniffer.event.SnifferScheduler.ScheduleInfo;
import com.logsniffer.event.processing.ScheduleInfoAccess;
import com.logsniffer.event.processing.SnifferJobManager;
import com.logsniffer.event.publisher.MailPublisher;
import com.logsniffer.event.support.LevelScanner;
import com.logsniffer.event.support.MinBAmountReadStrategy;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogSource;
import com.logsniffer.model.file.WildcardLogsSource;
import com.logsniffer.model.h2.H2LogSourceProvider;

/**
 * Tets for {@link H2SnifferPersistence}.
 * 
 * @author mbok
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { H2SnifferPersistenceTest.class,
		CoreAppConfig.class, QaDataSourceAppConfig.class })
@Configuration
public class H2SnifferPersistenceTest {
	@Bean
	H2SnifferPersistence snifferPersistence() {
		return new H2SnifferPersistence();
	}

	@Bean
	H2LogSourceProvider sourceProvider() {
		return new H2LogSourceProvider();
	}

	@Bean
	ScheduleInfoAccess scheduleInfoAccess() {
		return new ScheduleInfoAccess();
	}

	@Bean
	SnifferScheduler snifferScheduler() {
		return new SnifferJobManager();
	}

	@Bean
	Scheduler scheduler() {
		return Mockito.mock(Scheduler.class);
	}

	@Autowired
	private SnifferScheduler snifferScheduler;

	@Autowired
	private ScheduleInfoAccess scheduleInfoAccess;

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private H2SnifferPersistence snifferPersistence;

	@Autowired
	private H2LogSourceProvider sourceProvider;

	private WildcardLogsSource source1, source2;

	@Before
	public void setUp() {
		source1 = new WildcardLogsSource();
		source1.setName("Source 1");
		source1.setId(sourceProvider.createSource(source1));

		source2 = new WildcardLogsSource();
		source2.setName("Source 2");
		source2.setId(sourceProvider.createSource(source2));
	}

	@Test
	@DirtiesContext
	public void testGetPersistentSniffers() throws ConfigException {
		Assert.assertEquals(0, snifferPersistence.getSnifferListBuilder()
				.list().getItems().size());
		Sniffer s1 = new Sniffer();
		s1.setName("S1");
		s1.setScheduleCronExpression("-");
		s1.setLogSourceId(source1.getId());
		LevelScanner levelScanner = new LevelScanner();
		levelScanner.setSeverityNumber(5);
		s1.setScanner(levelScanner);
		s1.setPublishers(Collections
				.singletonList((Publisher) new MailPublisher()));
		s1.setReaderStrategy(new MinBAmountReadStrategy(2));
		long sid = snifferPersistence.createSniffer(s1);
		Assert.assertEquals(1, snifferPersistence.getSnifferListBuilder()
				.list().getItems().size());
		Sniffer checkSniffer = snifferPersistence.getSnifferListBuilder()
				.list().getItems().get(0);

		// Verify
		checkSniffer = snifferPersistence.getSniffer(sid);
		Assert.assertEquals("S1", checkSniffer.getName());
		Assert.assertEquals(source1.getId(), checkSniffer.getLogSourceId());
		Assert.assertEquals(true, checkSniffer.getScanner() instanceof Scanner);
		Assert.assertNotNull(checkSniffer.getScanner());
		Assert.assertEquals(1, checkSniffer.getPublishers().size());
		Assert.assertNotNull(checkSniffer.getReaderStrategy());
		Assert.assertTrue(((LogEntryReaderStrategyWrapper) checkSniffer
				.getReaderStrategy()).getWrappedStrategy() instanceof MinBAmountReadStrategy);
		Assert.assertEquals(
				2,
				((MinBAmountReadStrategy) ((LogEntryReaderStrategyWrapper) checkSniffer
						.getReaderStrategy()).getWrappedStrategy())
						.getMinBytesAmount());

		// Update
		checkSniffer.setName("S2");
		checkSniffer.setLogSourceId(source2.getId());
		snifferPersistence.updateSniffer(checkSniffer);
		Sniffer checkSnifferUpd = snifferPersistence.getSniffer(sid);
		Assert.assertEquals("S2", checkSnifferUpd.getName());
		Assert.assertEquals(source2.getId(), checkSnifferUpd.getLogSourceId());

		// Schedule info adaptor
		AspectSniffer as = snifferPersistence
				.getSnifferListBuilder()
				.withScheduleInfo(
						snifferScheduler.getScheduleInfoAspectAdaptor()).list()
				.getItems().get(0);
		ScheduleInfo scheduleInfo = snifferScheduler
				.getScheduleInfoAspectAdaptor().getApsect(as);
		Assert.assertEquals(false, scheduleInfo.isScheduled());
		Assert.assertNull(scheduleInfo.getLastFireTime());
		scheduleInfo.setScheduled(true);
		scheduleInfo.setLastFireTime(new Date());
		scheduleInfoAccess.updateScheduleInfo(as.getId(), scheduleInfo);
		as = snifferPersistence
				.getSnifferListBuilder()
				.withScheduleInfo(
						snifferScheduler.getScheduleInfoAspectAdaptor()).list()
				.getItems().get(0);
		scheduleInfo = snifferScheduler.getScheduleInfoAspectAdaptor()
				.getApsect(as);
		Assert.assertEquals(true, scheduleInfo.isScheduled());
		Assert.assertEquals(true, new Date().getTime()
				- scheduleInfo.getLastFireTime().getTime() < 1000);

		// Delete
		Sniffer checkSnifferDel = snifferPersistence.getSniffer(sid);
		Assert.assertNotNull(checkSnifferDel);
		snifferPersistence.deleteSniffer(checkSnifferDel);
		checkSnifferDel = snifferPersistence.getSniffer(sid);
		Assert.assertNull(checkSnifferDel);
	}

	@SuppressWarnings("unchecked")
	@Test
	@DirtiesContext
	public void testIncData() throws IOException {
		Sniffer s1 = new Sniffer();
		s1.setName("S1");
		s1.setScheduleCronExpression("-");
		s1.setLogSourceId(source1.getId());
		LevelScanner ls = new LevelScanner();
		ls.setSeverityNumber(5);
		s1.setScanner(ls);

		snifferPersistence.createSniffer(s1);
		Log log1 = Mockito.mock(Log.class);
		LogRawAccess<LogInputStream> log1Access = Mockito
				.mock(LogRawAccess.class);
		Mockito.when(log1.getPath()).thenReturn("abc");
		Mockito.when(log1Access.getFromJSON(Mockito.anyString())).thenThrow(
				new IOException("Not expected"));
		LogSource<LogInputStream> mockedSource1 = Mockito.mock(LogSource.class);
		Mockito.when(mockedSource1.getId()).thenReturn(source1.getId());
		Mockito.when(mockedSource1.getLogs()).thenReturn(
				Collections.singletonList(log1));
		Mockito.when(mockedSource1.getLogAccess(log1)).thenReturn(log1Access);
		IncrementData inc = snifferPersistence.getIncrementData(s1, source1,
				log1);
		Assert.assertNull(inc.getNextOffset());
		Assert.assertEquals(0, inc.getData().size());
		Assert.assertEquals(1,
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.size());
		Assert.assertEquals("abc",
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.keySet().iterator().next().getPath());
		Assert.assertEquals(0,
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.values().iterator().next().getData().size());

		// Update
		log1 = Mockito.mock(Log.class);
		log1Access = Mockito.mock(LogRawAccess.class);
		Mockito.when(mockedSource1.getLogs()).thenReturn(
				Collections.singletonList(log1));
		Mockito.when(mockedSource1.getLogAccess(log1)).thenReturn(log1Access);
		Mockito.when(log1.getPath()).thenReturn("abc");
		LogPointer myPointer = Mockito.mock(LogPointer.class);
		Mockito.when(myPointer.getJson()).thenReturn("my-pos");
		inc.setNextOffset(myPointer);
		inc.getData().element("index", 125);
		snifferPersistence.storeIncrementalData(s1, source1, log1, inc);
		Mockito.when(log1Access.getFromJSON("my-pos")).thenReturn(myPointer);
		IncrementData inc2 = snifferPersistence.getIncrementData(s1, source1,
				log1);
		Assert.assertEquals("my-pos", inc2.getNextOffset().getJson());
		Assert.assertEquals(125, inc2.getData().getInt("index"));
		Assert.assertEquals(1,
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.size());
		Assert.assertEquals("abc",
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.keySet().iterator().next().getPath());
		Assert.assertEquals(125,
				snifferPersistence.getIncrementDataByLog(s1, mockedSource1)
						.values().iterator().next().getData().getInt("index"));

		// Update again
		inc2.getData().element("top", 789);
		snifferPersistence.storeIncrementalData(s1, source1, log1, inc2);
		IncrementData inc3 = snifferPersistence.getIncrementData(s1, source1,
				log1);
		Assert.assertEquals("my-pos", inc3.getNextOffset().getJson());
		Assert.assertEquals(125, inc3.getData().getInt("index"));
		Assert.assertEquals(789, inc3.getData().getInt("top"));

		// Delete sniffer
		Assert.assertNotNull(inc3.getNextOffset());
		snifferPersistence.deleteSniffer(s1);
		inc3 = snifferPersistence.getIncrementData(s1, source1, log1);
		Assert.assertNull(inc3.getNextOffset());
	}
}
