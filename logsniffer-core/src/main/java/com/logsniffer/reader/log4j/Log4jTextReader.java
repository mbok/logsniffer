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
package com.logsniffer.reader.log4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.support.DateFormatUtils;
import com.logsniffer.reader.support.FormattedTextReader;

/**
 * Reads log4j entries from text log files. The default charset is UTF-8.
 * Registered to application context in scope of {@link ConfigurableBeanFactory}
 * .
 * 
 * @author mbok
 * 
 */
@Component
public class Log4jTextReader extends FormattedTextReader {
	private static final Logger logger = LoggerFactory.getLogger(Log4jTextReader.class);

	private static List<SeverityLevel> levelList;
	private static final HashMap<String, SeverityLevel> LEVEL_MAP = new HashMap<String, SeverityLevel>();

	private MessageSpecifier messageSpecifier;

	private TimeSpecifier timeSpecifier;

	private LevelSpecifier levelSpecifier;

	@JsonProperty
	private Locale locale;

	@JsonProperty
	private String timeZone;

	static {
		LEVEL_MAP.put(Level.TRACE.toString(),
				new SeverityLevel(Level.TRACE.toString(), 6, SeverityClassification.TRACE));
		LEVEL_MAP.put(Level.DEBUG.toString(),
				new SeverityLevel(Level.DEBUG.toString(), 5, SeverityClassification.DEBUG));
		LEVEL_MAP.put(Level.INFO.toString(),
				new SeverityLevel(Level.INFO.toString(), 4, SeverityClassification.INFORMATIONAL));
		LEVEL_MAP.put(Level.WARN.toString(),
				new SeverityLevel(Level.WARN.toString(), 3, SeverityClassification.WARNING));
		LEVEL_MAP.put(Level.ERROR.toString(),
				new SeverityLevel(Level.ERROR.toString(), 2, SeverityClassification.ERROR));
		LEVEL_MAP.put(Level.FATAL.toString(),
				new SeverityLevel(Level.FATAL.toString(), 1, SeverityClassification.EMERGENCY));
		levelList = new ArrayList<SeverityLevel>(LEVEL_MAP.values());
		Collections.sort(levelList);
		LEVEL_MAP.put("WARNING", LEVEL_MAP.get(Level.WARN.toString()));
		LEVEL_MAP.put("SEVERE", LEVEL_MAP.get(Level.FATAL.toString()));
	}

	/**
	 * Specifier for the level part "p".
	 * 
	 * @author mbok
	 * 
	 */
	private static class LevelSpecifier extends Specifier {
		public LevelSpecifier(final String specifierKey) {
			super(specifierKey);
		}

		@Override
		protected String getRegex() {
			return adaptRegexByLength("[ A-Z]", "[A-Z]");
		}

		@Override
		protected void set(final LogEntry entry, String match) {
			entry.put(getFieldName(), match);
			match = match.trim();
			SeverityLevel level = LEVEL_MAP.get(match);
			if (level != null) {
				// Direct placement
				entry.setSeverity(level);
			} else {
				// Substring
				for (final String key : LEVEL_MAP.keySet()) {
					if (key.startsWith(match)) {
						level = LEVEL_MAP.get(key);
						break;
					}
				}
				if (level != null) {
					entry.setSeverity(level);
				} else {
					logger.debug("No matching level found {}", match);
				}
			}
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return FieldBaseTypes.STRING;
		}

	}

	/**
	 * Specifier for the date/time part "d".
	 * 
	 * @author mbok
	 * 
	 */
	private class TimeSpecifier extends Specifier {
		public TimeSpecifier(final String specifierKey) {
			super(specifierKey);
		}

		private SimpleDateFormat dateFormat;

		@Override
		protected String getRegex() throws FormatException {
			if ("ABSOLUTE".equals(getModifier())) {
				dateFormat = createFormat("HH:mm:ss,SSS");
				return "\\d{1,2}:\\d{1,2}:\\d{1,2},\\d{1,3}";
			} else if (StringUtils.isEmpty(getModifier())) {
				// ISO8601: yyyy-MM-dd HH:mm:ss,SSS
				dateFormat = createFormat("yyyy-MM-dd HH:mm:ss,SSS");
				return "[1-9]\\d{3}-\\d{2}-\\d{2} " + "\\d{1,2}:\\d{1,2}:\\d{1,2},\\d{1,3}";
			} else {
				try {
					dateFormat = createFormat(getModifier());
					return DateFormatUtils.getRegexPattern(dateFormat);
				} catch (final IllegalArgumentException e) {
					throw new FormatException("Not valid simple formate date format: " + getModifier());
				}
			}
		}

		private SimpleDateFormat createFormat(final String format) {
			final SimpleDateFormat dateFormat = locale != null ? new SimpleDateFormat(format, locale)
					: new SimpleDateFormat(format);
			if (StringUtils.isNotBlank(timeZone)) {
				dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			}
			return dateFormat;
		}

		@Override
		protected void set(final LogEntry entry, final String match) throws FormatException {
			try {
				entry.getFields().put(getFieldName(), match);
				entry.setTimeStamp(dateFormat.parse(match));
			} catch (final ParseException e) {
				throw new FormatException("Failed to parse date: " + match, e);
			}
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return FieldBaseTypes.STRING;
		}

		@Override
		public Specifier clone() throws CloneNotSupportedException {
			// Clone not thread-safe date format
			final TimeSpecifier c = (TimeSpecifier) super.clone();
			if (dateFormat != null) {
				c.dateFormat = createFormat(dateFormat.toPattern());
			}
			return c;
		}
	}

	/**
	 * Speicifer for message part. Used a specific type to identify if this
	 * modifier is part of the conversion pattern.
	 * 
	 * @author mbok
	 * 
	 */
	public static class MessageSpecifier extends ArbitraryTextSpecifier {

		public MessageSpecifier(final String specifierKey, final boolean greedy) {
			super(specifierKey, greedy);
		}

	}

	public Log4jTextReader() {
		super();
	}

	public Log4jTextReader(final String conversionPattern, final String charset) {
		setFormatPattern(conversionPattern);
		setCharset(charset);
	}

	@Override
	protected Specifier[] createSupportedSpecifiers() {
		return new Specifier[] { new LevelSpecifier("p"), new TimeSpecifier("d"), new MessageSpecifier("m", true),
				new IgnoreSpecifier("n") };
	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return levelList;
	}

	@Override
	protected void init() throws FormatException {
		messageSpecifier = null;
		timeSpecifier = null;
		levelSpecifier = null;
		super.init();
		if (parsingSpecifiers != null) {
			for (final Specifier s : parsingSpecifiers) {
				if (s instanceof MessageSpecifier) {
					messageSpecifier = (MessageSpecifier) s;
				} else if (s instanceof TimeSpecifier) {
					timeSpecifier = (TimeSpecifier) s;
				} else if (s instanceof LevelSpecifier) {
					levelSpecifier = (LevelSpecifier) s;
				}

			}
		}
	}

	@Override
	protected void attachOverflowLine(final LogEntry entry, final String overflowLine) {
		if (messageSpecifier != null) {
			final FieldsMap fMap = entry.getFields();
			final String mfName = messageSpecifier.getFieldName();
			final String oldMsg = (String) fMap.get(mfName);
			if (oldMsg == null) {
				fMap.put(mfName, overflowLine);
			} else {
				fMap.put(mfName, oldMsg + "\n" + overflowLine);
			}
		}
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		init();
		final LinkedHashMap<String, FieldBaseTypes> types = super.getFieldTypes();
		if (timeSpecifier != null) {
			types.put(LogEntry.FIELD_TIMESTAMP, FieldBaseTypes.DATE);
		}
		if (levelSpecifier != null) {
			types.put(LogEntry.FIELD_SEVERITY_LEVEL, FieldBaseTypes.SEVERITY);
		}
		return types;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *            the locale to set
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone
	 *            the timeZone to set
	 */
	public void setTimeZone(final String timeZone) {
		this.timeZone = timeZone;
	}
}
