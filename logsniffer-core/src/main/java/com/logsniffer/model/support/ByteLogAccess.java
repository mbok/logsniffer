package com.logsniffer.model.support;

import java.io.IOException;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.Navigation.ByteOffsetNavigation;

public interface ByteLogAccess extends LogRawAccess<ByteLogInputStream>, ByteOffsetNavigation {
	/**
	 * Creates a position pointer in the log relative to the source pointer. A
	 * null source means the log start position. The calculated pointer will
	 * never leave the start or end bound of the log.
	 * 
	 * @param source
	 *            source position or null for log start position
	 * @param relativeBytePosition
	 *            bytes to move the pointer relative to the source position. A
	 *            negative number moves the pointer to the log start, a positive
	 *            number to the end.
	 * @return new pointer
	 * @throws IOException
	 *             in case of errors
	 */
	public abstract LogPointer createRelative(LogPointer source, long relativeBytePosition) throws IOException;

}
