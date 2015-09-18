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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.reader.FormatException;

/**
 * Reader based on patterns.
 * 
 * @author mbok
 * 
 */
public abstract class FormattedTextReader extends
		AbstractPatternLineReader<Matcher> {
	private static final Logger logger = LoggerFactory
			.getLogger(FormattedTextReader.class);

	public abstract static class Specifier {
		private int minWidth;
		private int maxWidth;
		private String modifier;
		private String specifierKey;
		private String fieldName;

		public Specifier(final String specifierKey) {
			super();
			this.specifierKey = specifierKey;
		}

		/**
		 * @return the minWidth
		 */
		public int getMinWidth() {
			return minWidth;
		}

		/**
		 * @param minWidth
		 *            the minWidth to set
		 */
		public void setMinWidth(final int minWidth) {
			this.minWidth = minWidth;
		}

		/**
		 * @return the maxWidth
		 */
		public int getMaxWidth() {
			return maxWidth;
		}

		/**
		 * @param maxWidth
		 *            the maxWidth to set
		 */
		public void setMaxWidth(final int maxWidth) {
			this.maxWidth = maxWidth;
		}

		/**
		 * @return the modifier
		 */
		public String getModifier() {
			return modifier;
		}

		/**
		 * @param modifier
		 *            the modifier to set
		 */
		public void setModifier(final String modifier) {
			this.modifier = modifier;
		}

		protected abstract String getRegex() throws FormatException;

		protected abstract void set(LogEntry entry, String match)
				throws FormatException;

		protected abstract FieldBaseTypes getFieldType();

		public final String getSpecifierKey() {
			return specifierKey;
		}

		/**
		 * @return the attributeName
		 */
		public String getFieldName() {
			return fieldName != null ? fieldName : specifierKey;
		}

		/**
		 * Adapts the lengthPattern by length modifiers and returns it. If
		 * length modifiers aren't specified the withoutLengthPattern is
		 * returned if given otherwise the unchanged lengthPattern.
		 * 
		 * @param lengthPattern
		 * @param withoutLengthPattern
		 * @return
		 */
		protected String adaptRegexByLength(final String lengthPattern,
				final String withoutLengthPattern) {
			if (maxWidth > 0 && maxWidth == minWidth) {
				return lengthPattern + "{" + maxWidth + "}";
			} else if (maxWidth > 0) {
				return lengthPattern + "{" + Math.max(0, minWidth) + ","
						+ maxWidth + "}";
			} else if (minWidth > 0) {
				return lengthPattern + "{" + minWidth + ",}";
			}
			return withoutLengthPattern != null ? withoutLengthPattern
					: lengthPattern;
		}
	}

	/**
	 * Specifier for arbitrary unknown specifiers.
	 * 
	 * @author mbok
	 * 
	 */
	public static class ArbitraryTextSpecifier extends Specifier {
		private boolean greedy;

		public ArbitraryTextSpecifier(final String specifierKey,
				final boolean greedy) {
			super(specifierKey);
			this.greedy = greedy;
		}

		@Override
		protected String getRegex() {
			return adaptRegexByLength(".", ".*") + (greedy ? "" : "?");
		}

		@Override
		protected void set(final LogEntry entry, final String match)
				throws FormatException {
			entry.getFields().put(getFieldName(), match);
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return FieldBaseTypes.STRING;
		}
	}

	/**
	 * Specifier to ignore.
	 * 
	 * @author mbok
	 * 
	 */
	public static final class IgnoreSpecifier extends Specifier {
		public IgnoreSpecifier(final String specifierKey) {
			super(specifierKey);
		}

		@Override
		protected String getRegex() {
			return null;
		}

		@Override
		protected void set(final LogEntry entry, final String match)
				throws FormatException {
			// NOP
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return null;
		}
	}

	private static final Pattern SPECIFIER_PATTERN = Pattern
			.compile("%(-?(\\d+))?(\\.(\\d+))?([a-zA-Z])(\\{([^\\}]+)\\})?");

	@JsonProperty
	@NotEmpty
	private String formatPattern;

	@JsonProperty
	private Map<String, String> specifiersFieldMapping = new HashMap<String, String>();

	protected Specifier[] parsingSpecifiers;
	protected Pattern parsingPattern;

	/**
	 * 
	 * @return the supported specifiers
	 */
	protected abstract Specifier[] createSupportedSpecifiers();

	/**
	 * @param formatPattern
	 *            the formatPattern to set
	 */
	@Override
	protected void initPattern() throws FormatException {
		if (parsingPattern == null) {
			// Only if not yet parsed
			if (formatPattern != null) {
				ArrayList<Specifier> specs = new ArrayList<Specifier>();
				StringBuilder parsingPatternStr = new StringBuilder();
				Matcher m = SPECIFIER_PATTERN.matcher(formatPattern);
				int leftPos = 0;
				while (m.find()) {
					if (m.start() > leftPos) {
						parsingPatternStr.append(Pattern.quote(formatPattern
								.substring(leftPos, m.start())));
					}
					leftPos = m.end();
					String minWidthStr = m.group(2);
					String maxWidthStr = m.group(4);
					String specName = m.group(5);
					String specModifier = m.group(7);
					int minWidth = -1;
					if (minWidthStr != null && minWidthStr.length() > 0) {
						minWidth = Integer.parseInt(minWidthStr);
					}
					int maxWidth = -1;
					if (maxWidthStr != null && maxWidthStr.length() > 0) {
						maxWidth = Integer.parseInt(maxWidthStr);
					}
					Specifier spec = null;
					for (Specifier specTest : createSupportedSpecifiers()) {
						if (specTest.getSpecifierKey().equals(specName)) {
							spec = specTest;
							break;
						}
					}
					if (spec == null) {
						logger.info(
								"Format specifier {} in pattern '{}' is unknown and will be parsed as simple text pattern",
								specName, formatPattern);
						spec = new ArbitraryTextSpecifier(specName, false);
					} else if (spec instanceof IgnoreSpecifier) {
						logger.debug(
								"Format specifier '{}' in pattern '{}' is ignored",
								specName, formatPattern);
						continue;
					}
					spec.setMaxWidth(maxWidth);
					spec.setMinWidth(minWidth);
					spec.setModifier(specModifier);
					parsingPatternStr.append("(");
					parsingPatternStr.append(spec.getRegex());
					parsingPatternStr.append(")");
					spec.fieldName = specifiersFieldMapping.get(specName);
					if (StringUtils.isBlank(spec.fieldName)) {
						spec.fieldName = specName;
					}
					specs.add(spec);
				}
				parsingPatternStr.append(Pattern.quote(formatPattern
						.substring(leftPos)));
				parsingPattern = Pattern.compile(parsingPatternStr.toString());
				parsingSpecifiers = specs.toArray(new Specifier[specs.size()]);
				logger.debug(
						"Prepared parsing pattern '{}' for log4j conversion pattern: {}",
						parsingPattern, formatPattern);
			} else {
				parsingSpecifiers = null;
				parsingPattern = null;
			}
		}
	}

	/**
	 * @return the formatPattern
	 */
	public String getFormatPattern() {
		return formatPattern;
	}

	/**
	 * @param formatPattern
	 *            the formatPattern to set
	 */
	public void setFormatPattern(final String formatPattern) {
		this.formatPattern = formatPattern;
		this.parsingPattern = null;
	}

	@Override
	protected Matcher matches(final String line) {
		Matcher m = parsingPattern.matcher(line);
		return m.matches() ? m : null;
	}

	@Override
	protected void fillAttributes(final LogEntry entry, final Matcher ctx)
			throws FormatException {
		int specNumber = 1;
		for (Specifier spec : parsingSpecifiers) {
			String match = ctx.group(specNumber++);
			spec.set(entry, match);
		}
	}

	@Override
	protected String getPatternInfo() {
		return formatPattern;
	}

	/**
	 * @return the specifiersFieldMapping
	 */
	public Map<String, String> getSpecifiersFieldMapping() {
		return specifiersFieldMapping;
	}

	/**
	 * @param specifiersFieldMapping
	 *            the specifiersFieldMapping to set
	 */
	public void setSpecifiersFieldMapping(
			final Map<String, String> specifiersFieldMapping) {
		this.specifiersFieldMapping = specifiersFieldMapping;
		this.parsingPattern = null;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes()
			throws FormatException {
		initPattern();
		LinkedHashMap<String, FieldBaseTypes> fields = new LinkedHashMap<String, FieldBaseTypes>();
		for (Specifier s : parsingSpecifiers) {
			fields.put(s.getFieldName(), s.getFieldType());
		}
		return fields;
	}

}
