package com.logsniffer.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logsniffer.system.notification.Notification;
import com.logsniffer.system.notification.NotificationProvider;
import com.logsniffer.system.notification.Notification.Level;
import com.logsniffer.system.notification.Notification.Type;

/**
 * Produces a {@link Notification} for liking logsniffer.
 * 
 * @author mbok
 *
 */
@Component
public class LikeMeNotificationProducer {
	@Autowired
	private NotificationProvider provider;

	@PostConstruct
	public void storeNotification() {
		Notification likeMe = new Notification();
		likeMe.setId("likeMe");
		likeMe.setTitle("Give logsniffer you star");
		likeMe.setMessage("Give logsniffer you star Give logsniffer you star Give logsniffer you star");
		likeMe.setLevel(Level.INFO);
		likeMe.setType(Type.TOPIC);
		provider.store(likeMe, false);
	}

}
