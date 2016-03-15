package com.logsniffer.system.notification;

import com.logsniffer.system.notification.Notification.Level;
import com.logsniffer.util.PageableResult;

/**
 * Manages the lifecycle of notifications.
 * 
 * @author mbok
 *
 */
public interface NotificationProvider {
	/**
	 * Notification summary.
	 * 
	 * @author mbok
	 *
	 */
	public static final class NotificationSummary {
		private int count;
		private Level worstLevel;

		public NotificationSummary(int count, Level worstLevel) {
			this.count = count;
			this.worstLevel = worstLevel;
		}

		/**
		 * @return count of not acknowledged notifications addressed to a user
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return the worst level of not acknowledged notifications addressed
		 *         to a user
		 */
		public Level getWorstLevel() {
			return worstLevel;
		}
	}

	/**
	 * Stores given notification in the case the id is unknown. If the
	 * notification exists and the override flag is true the message will be
	 * overridden.
	 * 
	 * @param n
	 *            notification
	 * @param override
	 *            if true and the notification exists already it will be
	 *            overriden
	 * @return true if notification was stored or overridden, false if notification is already
	 *         stored
	 */
	boolean store(Notification n, boolean override);

	/**
	 * Returns not acknowledged notifications addressed to the given user.
	 * 
	 * @param userToken
	 *            the user token
	 * @param from
	 *            offset
	 * @param limit
	 *            limit
	 * @return pageable notification result
	 */
	PageableResult<Notification> getNotifications(String userToken, int from, int limit);

	/**
	 * Returns a summary for not acknowledged notifications addressed to the
	 * given user.
	 * 
	 * @param userToken
	 *            the user token
	 * @return summary
	 */
	NotificationSummary getSummary(String userToken);

	/**
	 * Acknowledges a notification for given user.
	 * 
	 * @param notificationId
	 *            the notification id
	 * @param userToken
	 *            user token
	 */
	void acknowledge(String notificationId, String userToken);

	/**
	 * Deletes a notification physically, thus it won't be available for any
	 * user anymore.
	 * 
	 * @param notificationId
	 *            the id
	 */
	void delete(String notificationId);
}
