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
package com.logsniffer.util.grok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for known GROK patterns.
 * 
 * @author mbok
 * 
 */
public class GroksRegistry {
	private static final Logger logger = LoggerFactory
			.getLogger(GroksRegistry.class);
	private static final Pattern PATTERN_ENTRY = Pattern
			.compile("^\\s*([A-Z0-9_-]+) (.*)$");

	private Map<String, Grok> groks = new LinkedHashMap<String, Grok>();
	private Map<String, Map<String, Grok>> grokGroups = new LinkedHashMap<String, Map<String, Grok>>();

	public void registerPatternBlocks(final Map<String, String[]> patternBlocks)
			throws GrokException {
		HashMap<String, String> groupMapping = new HashMap<String, String>();
		LinkedHashMap<String, String> strPatterns = new LinkedHashMap<String, String>();
		for (String group : patternBlocks.keySet()) {
			grokGroups.put(group, new LinkedHashMap<String, Grok>());
			for (String block : patternBlocks.get(group)) {
				for (String line : block.split("\n")) {
					Matcher m = PATTERN_ENTRY.matcher(line);
					if (m.matches()) {
						strPatterns.put(m.group(1), m.group(2));
						groupMapping.put(m.group(1), group);
					}
				}
			}
		}
		for (String name : new ArrayList<String>(strPatterns.keySet())) {
			// Could be already compiled by sub reference
			if (strPatterns.containsKey(name)) {
				compile(name, strPatterns.get(name), strPatterns, groupMapping);
			}
		}
		logger.info("Registered grok patterns: {}", groks.keySet());
	}

	private void compile(final String name, final String pattern,
			final LinkedHashMap<String, String> strPatterns,
			final HashMap<String, String> groupMapping) throws GrokException {
		strPatterns.remove(name);
		Matcher subGroks = Grok.PATTERN_SUBGROK.matcher(pattern);
		while (subGroks.find()) {
			String subGrokName = subGroks.group(1);
			if (groks.containsKey(subGrokName)) {
				continue;
			} else if (strPatterns.containsKey(subGrokName)) {
				// Recursion
				compile(subGrokName, strPatterns.get(subGrokName), strPatterns,
						groupMapping);
			} else {
				throw new GrokException("No sub grok pattern for name '"
						+ subGrokName + "' found for " + name + "=" + pattern);
			}
		}
		try {
			Grok g = Grok.compile(this, pattern);
			groks.put(name, g);
			grokGroups.get(groupMapping.get(name)).put(name, g);
		} catch (Exception e) {
			throw new GrokException("Failed to compile Grok with name '" + name
					+ "' and pattern '" + pattern + "'", e);
		}
	}

	/**
	 * @return the groks
	 */
	public Map<String, Grok> getGroks() {
		return groks;
	}

	/**
	 * @return the grokGroups
	 */
	public Map<String, Map<String, Grok>> getGrokGroups() {
		return grokGroups;
	}

}
