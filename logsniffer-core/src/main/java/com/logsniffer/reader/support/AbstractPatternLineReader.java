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
package com.logsniffer.reader.support;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.support.ByteLogInputStream;
import com.logsniffer.model.support.LineInputStream;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;

/**
 * Abstract line text reader based on pattern matching.
 * 
 * @author mbok
 * 
 */
public abstract class AbstractPatternLineReader<MatcherContext> implements LogEntryReader<ByteLogInputStream> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractPatternLineReader.class);
	private static int MAX_LINES2CONSUME_WITHOUT_PATTERN = 50;

	@JsonProperty
	@NotEmpty
	private String charset = "UTF-8";

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public void setCharset(final String charset) {
		this.charset = charset;
	}

	/**
	 * Initializes a pattern before reading.
	 * 
	 * @throws ParseException
	 *             in case pattern initialization errors
	 */
	protected abstract void initPattern() throws FormatException;

	/**
	 * @return a matcher context in case of a matching line or null if doesn't.
	 */
	protected abstract MatcherContext matches(String line);

	/**
	 * Fills the attributes from the matcher context.
	 * 
	 * @param entry
	 *            entry to fill attributes to
	 * @param ctx
	 *            the matcher context
	 */
	protected abstract void fillAttributes(LogEntry entry, MatcherContext ctx) throws FormatException;

	/**
	 * Called in case of a line not matching the format pattern and which is
	 * associated with an overflow of the previous entry. Implement this method
	 * to attach the overflow line to a field.
	 * 
	 * @param entry
	 *            the previous entry
	 * @param overflowLine
	 *            the overflow line
	 */
	protected abstract void attachOverflowLine(LogEntry entry, String overflowLine);

	/**
	 * 
	 * @return pattern info for logging issues
	 */
	protected abstract String getPatternInfo();

	@Override
	public final void readEntries(final Log log, final LogRawAccess<ByteLogInputStream> logAccess,
			final LogPointer startOffset, final LogEntryConsumer consumer) throws IOException, FormatException {
		initPattern();
		final LinkedHashMap<String, FieldBaseTypes> fieldTypes = getFieldTypes();
		LineInputStream lis = null;
		try {
			boolean patternAware = true;
			int linesWithoutPattern = 0;
			lis = new LineInputStream(logAccess, logAccess.getInputStream(startOffset), getCharset());
			LogEntry entry = null;
			StringBuilder text = new StringBuilder();
			String line;
			LogPointer lastOffset = lis.getPointer();
			if (lastOffset == null) {
				lastOffset = logAccess.createRelative(null, 0);
			}
			LogPointer currentOffset = null;
			while ((line = lis.readNextLine()) != null && (currentOffset = lis.getPointer()) != null) {
				final MatcherContext ctx = matches(line);
				if (ctx != null || !patternAware) {
					linesWithoutPattern = -1;
					if (entry != null) {
						entry.setRawContent(text.toString());
						entry.setEndOffset(lastOffset);
						if (!consumer.consume(log, logAccess, entry)) {
							return;
						}
					}
					entry = new LogEntry();
					entry.getFields().setTypes(fieldTypes);
					entry.setStartOffset(lastOffset);
					text = new StringBuilder(line);
					if (ctx != null) {
						fillAttributes(entry, ctx);
					}
				} else {
					if (patternAware && linesWithoutPattern >= 0) {
						linesWithoutPattern++;
						if (linesWithoutPattern > MAX_LINES2CONSUME_WITHOUT_PATTERN) {
							logger.warn(
									"Pattern {} for log '{}' didn't matched any of read {} lines, pattern matching will be disabled",
									getPatternInfo(), log, linesWithoutPattern);
							patternAware = false;
						}
					}
					if (entry == null) {
						entry = new LogEntry();
						entry.getFields().setTypes(fieldTypes);
						entry.setStartOffset(lastOffset);
					}
					if (text.length() > 0) {
						text.append("\n");
					}
					text.append(line);
					attachOverflowLine(entry, line);
				}
				lastOffset = currentOffset;
			}
			if (entry != null) {
				entry.setRawContent(text.toString());
				entry.setEndOffset(lastOffset);
				consumer.consume(log, logAccess, entry);
			}
		} finally {
			lis.close();
		}
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		final LinkedHashMap<String, FieldBaseTypes> fields = new LinkedHashMap<String, FieldBaseTypes>();
		fields.put(LogEntry.FIELD_RAW_CONTENT, FieldBaseTypes.STRING);
		return fields;
	}
}
