package com.logsniffer.reader.support;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Inverts read direction of the target reader.
 * 
 * @author mbok
 *
 * @param <ACCESSTYPE>
 */
public class InverseReader<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>>
		implements LogEntryReader<ACCESSTYPE> {

	private final LogEntryReader<ACCESSTYPE> targetReader;

	public InverseReader(final LogEntryReader<ACCESSTYPE> targetReader) {
		this.targetReader = targetReader;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return targetReader.getFieldTypes();
	}

	@Override
	public void readEntries(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		targetReader.readEntriesReverse(log, logAccess, startOffset, consumer);
	}

	@Override
	public void readEntriesReverse(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		targetReader.readEntries(log, logAccess, startOffset, consumer);
	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return targetReader.getSupportedSeverities();
	}

}
