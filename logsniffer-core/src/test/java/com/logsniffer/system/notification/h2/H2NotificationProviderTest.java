package com.logsniffer.system.notification.h2;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.app.QaDataSourceAppConfig;
import com.logsniffer.system.notification.Notification;
import com.logsniffer.system.notification.Notification.Level;
import com.logsniffer.system.notification.Notification.Type;
import com.logsniffer.system.notification.NotificationProvider;
import com.logsniffer.util.PageableResult;

/**
 * Test for {@link H2NotificationProvider}.
 * 
 * @author mbok
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { H2NotificationProviderTest.class, CoreAppConfig.class, QaDataSourceAppConfig.class })
@Configuration

public class H2NotificationProviderTest {
	@Bean
	H2NotificationProvider notificationProvider() {
		return new H2NotificationProvider();
	}

	@Autowired
	private NotificationProvider provider;

	@DirtiesContext
	@Test
	public void testStoring() {
		final Date now = new Date();
		final Notification n1 = new Notification();
		n1.setExpirationDate(new Date(now.getTime() + 1000));
		n1.setTitle("Title");
		n1.setMessage("msg");
		n1.setLevel(Level.WARN);
		n1.setType(Type.TOPIC);
		n1.setId("update");
		Assert.assertEquals(true, provider.store(n1, false));

		PageableResult<Notification> notifs = provider.getNotifications("user1", 0, 10);
		Assert.assertEquals(1, notifs.getTotalCount());
		Assert.assertEquals("Title", notifs.getItems().get(0).getTitle());
		Assert.assertEquals("msg", notifs.getItems().get(0).getMessage());
		Assert.assertEquals(Level.WARN, notifs.getItems().get(0).getLevel());
		Assert.assertEquals(Type.TOPIC, notifs.getItems().get(0).getType());
		Assert.assertEquals("update", notifs.getItems().get(0).getId());
		Assert.assertTrue(notifs.getItems().get(0).getCreationDate().getTime() >= now.getTime() - 2000);

		// Try to manipulate with old expiration date
		n1.setCreationDate(new Date(0));
		// n1.setExpirationDate(new Date(0));
		n1.setTitle("xxx");
		n1.setMessage("xxx");
		n1.setLevel(Level.ERROR);
		n1.setType(Type.MESSAGE);
		Assert.assertEquals(false, provider.store(n1, false));
		notifs = provider.getNotifications("user1", 0, 10);
		Assert.assertEquals(1, notifs.getTotalCount());

		// Still the same unchanged data
		Assert.assertEquals("Title", notifs.getItems().get(0).getTitle());
		Assert.assertEquals("msg", notifs.getItems().get(0).getMessage());
		Assert.assertEquals(Level.WARN, notifs.getItems().get(0).getLevel());
		Assert.assertEquals(Type.TOPIC, notifs.getItems().get(0).getType());
		Assert.assertEquals("update", notifs.getItems().get(0).getId());
		Assert.assertEquals(now.getTime() + 1000, notifs.getItems().get(0).getExpirationDate().getTime());
		Assert.assertTrue(notifs.getItems().get(0).getCreationDate().getTime() >= now.getTime() - 2000);

		// Try to manipulate with new expiration date
		n1.setExpirationDate(new Date(now.getTime() + 2000));
		Assert.assertEquals(true, provider.store(n1, false));
		notifs = provider.getNotifications("user1", 0, 10);
		Assert.assertEquals(1, notifs.getTotalCount());

		// Still the same unchanged data, but newer expiration date
		Assert.assertEquals("Title", notifs.getItems().get(0).getTitle());
		Assert.assertEquals("msg", notifs.getItems().get(0).getMessage());
		Assert.assertEquals(Level.WARN, notifs.getItems().get(0).getLevel());
		Assert.assertEquals(Type.TOPIC, notifs.getItems().get(0).getType());
		Assert.assertEquals("update", notifs.getItems().get(0).getId());
		Assert.assertTrue(notifs.getItems().get(0).getCreationDate().getTime() >= now.getTime() - 2000);
		Assert.assertEquals(now.getTime() + 2000, notifs.getItems().get(0).getExpirationDate().getTime());
	}

	@DirtiesContext
	@Test
	public void testAcknowledging() {
		final Notification n1 = new Notification();
		n1.setExpirationDate(new Date(new Date().getTime() + 2000));
		n1.setTitle("Title");
		n1.setMessage("msg");
		n1.setLevel(Level.WARN);
		n1.setType(Type.TOPIC);
		n1.setId("update1");
		Assert.assertEquals(true, provider.store(n1, false));
		Assert.assertEquals(Level.WARN, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());

		n1.setId("update2");
		n1.setLevel(Level.ERROR);
		Assert.assertEquals(true, provider.store(n1, false));
		Assert.assertEquals(Level.ERROR, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(2, provider.getSummary("user1").getCount());

		// Ack 2
		provider.acknowledge("update2", "user1");
		Assert.assertEquals(Level.WARN, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());

		// Ack 1
		provider.acknowledge("update1", "user1");
		Assert.assertNull(provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(0, provider.getSummary("user1").getCount());

		// Another user
		Assert.assertEquals(Level.ERROR, provider.getSummary("userX").getWorstLevel());
		Assert.assertEquals(2, provider.getSummary("userX").getCount());

		// Store again without override
		Assert.assertEquals(false, provider.store(n1, false));
		Assert.assertNull(provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(0, provider.getSummary("user1").getCount());
		Assert.assertEquals(Level.ERROR, provider.getSummary("userX").getWorstLevel());
		Assert.assertEquals(2, provider.getSummary("userX").getCount());

		// Override
		Assert.assertEquals(true, provider.store(n1, true));
		Assert.assertEquals(Level.ERROR, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());
	}

	@DirtiesContext
	@Test
	public void testExpiration() throws InterruptedException {
		final Notification n1 = new Notification();
		n1.setExpirationDate(new Date(new Date().getTime() + 1500));
		n1.setTitle("Title");
		n1.setMessage("msg");
		n1.setLevel(Level.WARN);
		n1.setType(Type.TOPIC);
		n1.setId("update1");
		Assert.assertEquals(true, provider.store(n1, false));
		Assert.assertEquals(Level.WARN, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());
		Assert.assertEquals(1, provider.getNotifications("user1", 0, 10).getItems().size());

		// Sleep to wait for expiration
		Thread.sleep(2000);
		Assert.assertNull(provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(0, provider.getSummary("user1").getCount());
		Assert.assertEquals(0, provider.getNotifications("user1", 0, 10).getItems().size());
	}

	@DirtiesContext
	@Test
	public void testDeletion() {
		final Notification n1 = new Notification();
		n1.setExpirationDate(new Date(new Date().getTime() + 1500));
		n1.setTitle("Title");
		n1.setMessage("msg");
		n1.setLevel(Level.WARN);
		n1.setType(Type.MESSAGE);
		n1.setId("update1");
		Assert.assertEquals(true, provider.store(n1, false));
		Assert.assertEquals(Level.WARN, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());
		Assert.assertEquals(1, provider.getNotifications("user1", 0, 10).getItems().size());

		// Acknowledging without effect for the message type
		provider.acknowledge("update1", "user1");
		Assert.assertEquals(Level.WARN, provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(1, provider.getSummary("user1").getCount());
		Assert.assertEquals(1, provider.getNotifications("user1", 0, 10).getItems().size());

		// Deletion
		provider.delete("update1");
		Assert.assertNull(provider.getSummary("user1").getWorstLevel());
		Assert.assertEquals(0, provider.getSummary("user1").getCount());
		Assert.assertEquals(0, provider.getNotifications("user1", 0, 10).getItems().size());
	}
}
