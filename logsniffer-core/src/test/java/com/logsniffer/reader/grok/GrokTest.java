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
package com.logsniffer.reader.grok;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.logsniffer.util.grok.Grok;
import com.logsniffer.util.grok.GrokException;
import com.logsniffer.util.grok.GrokMatcher;
import com.logsniffer.util.grok.GroksRegistry;

/**
 * Tests for {@link Grok} and {@link GroksRegistry}.
 * 
 * @author mbok
 * 
 */
public class GrokTest {
	@Test
	public void testSingleSimplePattern() throws GrokException {
		GroksRegistry r = new GroksRegistry();
		r.registerPatternBlocks(Collections.singletonMap("base",
				new String[] { "USERNAME [a-zA-Z0-9_-]+" }));
		assertEquals(1, r.getGroks().size());
		assertNotNull(r.getGroks().get("USERNAME"));
		assertEquals(0, r.getGroks().get("USERNAME").getGroupNames().size());
		assertEquals(true, r.getGroks().get("USERNAME").matcher("mbok")
				.matches());
		assertEquals(false, r.getGroks().get("USERNAME").matcher("mbok!")
				.matches());
	}

	@Test
	public void testSubPattern() throws GrokException {
		GroksRegistry r = new GroksRegistry();
		HashMap<String, String[]> grokBlocks = new HashMap<String, String[]>();
		grokBlocks.put("base", new String[] { "USERNAME [a-zA-Z0-9_-]+" });
		grokBlocks.put("ext", new String[] { "USER %{USERNAME}" });
		r.registerPatternBlocks(grokBlocks);
		assertEquals(2, r.getGroks().size());
		assertNotNull(r.getGroks().get("USER"));
		assertEquals(0, r.getGroks().get("USER").getGroupNames().size());
		assertEquals(true, r.getGroks().get("USER").matcher("mbok").matches());
		assertEquals(false, r.getGroks().get("USER").matcher("mbok!").matches());
	}

	@Test
	public void testSubPatternAttributes() throws GrokException {
		GroksRegistry r = new GroksRegistry();
		r.registerPatternBlocks(Collections.singletonMap("base", new String[] {
				"USERNAME [a-zA-Z0-9_-]+", "USER (%{USERNAME:a}-)" }));
		assertEquals(2, r.getGroks().size());
		assertNotNull(r.getGroks().get("USER"));
		assertEquals(1, r.getGroks().get("USER").getGroupNames().size());
		assertEquals("a", r.getGroks().get("USER").getGroupNames().keySet()
				.iterator().next());
		assertEquals(2, r.getGroks().get("USER").getGroupNames().values()
				.iterator().next().intValue());
		Grok g = r.getGroks().get("USER");
		assertEquals(true, g.matcher("mbok-").matches());

		// Test sub groks with attribute projection
		assertEquals(2, g.getGroupNames().get("a").intValue());
		GrokMatcher m = g.matcher("mbok-");
		assertEquals(true, m.matches());
		assertEquals("mbok", m.group(g.getGroupNames().get("a")));
		assertEquals("mbok", m.group("a"));
		assertEquals(false, g.matcher("mbok").matches());
	}

	@Test(expected = GrokException.class)
	public void testExceptionInCaseOfCycles() throws GrokException {
		GroksRegistry r = new GroksRegistry();
		r.registerPatternBlocks(Collections.singletonMap("base", new String[] {
				"USERNAME %{USER}[a-zA-Z0-9_-]+", "USER (%{USERNAME:a}-)" }));
	}

	@Test(expected = GrokException.class)
	public void testExceptionInCaseOfInvalidReference() throws GrokException {
		GroksRegistry r = new GroksRegistry();
		r.registerPatternBlocks(Collections.singletonMap("base",
				new String[] { "USERNAME %{USER}[a-zA-Z0-9_-]+" }));
	}

	@Test
	public void testBasePatternSet() throws GrokException, IOException {
		GroksRegistry r = new GroksRegistry();
		r.registerPatternBlocks(Collections.singletonMap(
				"base",
				IOUtils.readLines(
						getClass().getResourceAsStream("/grok-patterns/base"))
						.toArray(new String[0])));
		String sysLogLine = "Nov  1 21:14:23 <1.3> localhost kernel: pid 84558 (expect), uid 30206: exited on signal 3";
		Grok syslogGrok = Grok
				.compile(
						r,
						"%{SYSLOGBASE} pid %{NUMBER:pid} \\(%{WORD:program2}\\), uid %{NUMBER:uid}: exited on signal %{NUMBER:signal}");
		GrokMatcher m = syslogGrok.matcher(sysLogLine);
		assertEquals(true, m.matches());
		assertEquals("Nov  1 21:14:23", m.group("timestamp"));
		assertEquals("1", m.group("facility"));
		assertEquals("3", m.group("priority"));
		assertEquals("localhost", m.group("logsource"));
		assertEquals("kernel", m.group("program"));
		assertEquals("expect", m.group("program2"));
		assertEquals("30206", m.group("uid"));
		assertEquals("3", m.group("signal"));
		assertEquals("84558", m.group("pid"));
	}
}
