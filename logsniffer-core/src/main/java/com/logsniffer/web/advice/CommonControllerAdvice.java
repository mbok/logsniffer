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
package com.logsniffer.web.advice;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.logsniffer.app.CoreAppConfig;
import com.logsniffer.system.notification.NotificationProvider;
import com.logsniffer.system.notification.NotificationProvider.NotificationSummary;
import com.logsniffer.user.UserTokenProvider;
import com.logsniffer.web.ViewController;

/**
 * General advice for common model attributes.
 * 
 * @author mbok
 *
 */
@ControllerAdvice(annotations = ViewController.class)
public class CommonControllerAdvice {
	@Autowired
	@Qualifier(CoreAppConfig.BEAN_LOGSNIFFER_PROPS)
	private Properties logsnifferProps;

	@Autowired
	private NotificationProvider notificationProvider;

	@Autowired
	private UserTokenProvider userTokenProvider;

	/**
	 * Exposes the CoreAppConfig#BEAN_LOGSNIFFER_PROPS as general modell
	 * attribute under the name "logsnifferProps".
	 * 
	 * @return the CoreAppConfig#BEAN_LOGSNIFFER_PROPS properties object
	 */
	@ModelAttribute("logsnifferProps")
	public Properties logsnifferProps() {
		return logsnifferProps;
	}

	@ModelAttribute("systemNotificationSummary")
	public NotificationSummary systemNotificationSummary(HttpServletRequest request, HttpServletResponse response) {
		return notificationProvider.getSummary(userTokenProvider.getToken(request, response));
	}
}
