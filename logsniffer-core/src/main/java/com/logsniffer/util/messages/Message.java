package com.logsniffer.util.messages;

/**
 * Represents a message.
 * 
 * @author mbok
 *
 */
public class Message {
	public enum MessageType {
		SUCCESS, INFO, WARN, ERROR
	}

	private final MessageType type;
	private final String message;

	public Message(final MessageType type, final String message) {
		super();
		this.type = type;
		this.message = message;
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

}
