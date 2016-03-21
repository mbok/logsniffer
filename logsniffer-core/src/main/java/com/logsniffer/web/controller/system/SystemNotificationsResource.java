package com.logsniffer.web.controller.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.system.notification.Notification;
import com.logsniffer.system.notification.NotificationProvider;
import com.logsniffer.system.notification.NotificationProvider.NotificationSummary;
import com.logsniffer.user.UserTokenProvider;
import com.logsniffer.util.PageableResult;
import com.logsniffer.web.controller.system.ElasticSettingsResource.EsStatusAndSettings;

/**
 * REST resource for {@link Notification}s.
 * 
 * @author mbok
 *
 */
@RestController
public class SystemNotificationsResource {
	@Autowired
	private UserTokenProvider tokenProvider;
	@Autowired
	private NotificationProvider notificationProvider;

	@RequestMapping(value = "/system/notifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public PageableResult<Notification> getUserNootifications(HttpServletRequest request,
			HttpServletResponse response) {
		return notificationProvider.getNotifications(tokenProvider.getToken(request, response), 0, 100);
	}

	@RequestMapping(value = "/system/notifications", method = RequestMethod.POST)
	@ResponseBody
	public NotificationSummary acknowledge(@RequestParam("id") String id, HttpServletRequest request,
			HttpServletResponse response) {
		String userToken = tokenProvider.getToken(request, response);
		notificationProvider.acknowledge(id, userToken);
		return notificationProvider.getSummary(userToken);
	}

	@RequestMapping(value = "/system/notifications", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void delete(@RequestParam("id") String id) {
		notificationProvider.delete(id);
	}
}
