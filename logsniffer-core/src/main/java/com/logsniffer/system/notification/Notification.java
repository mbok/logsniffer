package com.logsniffer.system.notification;

import java.util.Date;

/**
 * Notification model bean.
 * 
 * @author mbok
 *
 */
public class Notification {
	/**
	 * Level for a notification.
	 * 
	 * @author mbok
	 *
	 */
	public static enum Level {
		INFO, WARN, ERROR
	}

	/**
	 * Notification type.
	 * 
	 * @author mbok
	 *
	 */
	public static enum Type {
		/**
		 * Means the notification will be addressed to all users until it isn't
		 * expired. Acknowledging a notification per user is possible only for
		 * this type.
		 */
		TOPIC,
		/**
		 * Means the notification is globally addressed to all users.
		 * Acknowledging isn't supported for this type, only deletion.
		 */
		MESSAGE
	}

	private String id;
	private String title;
	private String message;
	private Date expirationDate;
	private Date creationDate;
	private Level level = Level.INFO;
	private Type type = Type.MESSAGE;

	/**
	 * @return the id defined by the producer - not null
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the expirationDate or null if message doesn't expire
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *            the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the creationDate - managed by {@link NotificationProvider}
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Notification [id=" + id + ", title=" + title + ", expirationDate=" + expirationDate + ", creationDate="
				+ creationDate + ", level=" + level + ", type=" + type + "]";
	}
}
