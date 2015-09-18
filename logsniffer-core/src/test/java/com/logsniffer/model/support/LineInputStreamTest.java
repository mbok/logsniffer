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
package com.logsniffer.model.support;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.file.DirectFileLogAccess;
import com.logsniffer.model.file.FileLog;

/**
 * Test for {@link LineInputStream}.
 * 
 * @author mbok
 * 
 */
public class LineInputStreamTest {

	@Test
	public void testDefaultNL() throws IOException {
		FileLog flog = new FileLog(writeString2Tmp("line1\nline2", "UTF-8"));
		DirectFileLogAccess logAccess = new DirectFileLogAccess(flog);
		LineInputStream log = new LineInputStream(logAccess,
				logAccess.getInputStream(null), "UTF-8");
		LogPointer start = log.getPointer();
		Assert.assertEquals("line1", log.readNextLine());
		Assert.assertEquals("line1\n".length(),
				logAccess.getDifference(start, log.getPointer()));
		Assert.assertEquals(false, log.getPointer().isEOF());
		Assert.assertEquals("line2", log.readNextLine());
		log.close();

		log = new LineInputStream(logAccess, logAccess.getInputStream(logAccess
				.createRelative(null, "line1\n".length())), "UTF-8");
		Assert.assertEquals("line2", log.readNextLine());
		Assert.assertNull(log.readNextLine());
		Assert.assertEquals(true, log.getPointer().isEOF());
		log.close();
	}

	@Test
	public void testWindowsNL() throws IOException {
		FileLog flog = new FileLog(writeString2Tmp("line1ü\r\nline2", "UTF-8"));
		DirectFileLogAccess logAccess = new DirectFileLogAccess(flog);
		LineInputStream log = new LineInputStream(logAccess,
				logAccess.getInputStream(null), "UTF-8");
		LogPointer start = log.getPointer();
		Assert.assertEquals("line1ü", log.readNextLine());
		Assert.assertEquals("line1ü\r\n".getBytes("UTF-8").length,
				logAccess.getDifference(start, log.getPointer()));
		Assert.assertEquals(false, log.getPointer().isEOF());
		Assert.assertEquals("line2", log.readNextLine());
		log.close();

		log = new LineInputStream(logAccess, logAccess.getInputStream(logAccess
				.createRelative(null, "line1ü\r\n".getBytes("UTF-8").length)),
				"UTF-8");
		Assert.assertEquals("line2", log.readNextLine());
		Assert.assertNull(log.readNextLine());
		Assert.assertEquals(true, log.getPointer().isEOF());
		log.close();

	}

	@Test
	public void testNLTrimingDuringBufferLimit() throws IOException {
		FileLog flog = new FileLog(new File(
				"src/test/resources/logs/nl-triming.txt"));
		DirectFileLogAccess logAccess = new DirectFileLogAccess(flog);
		LineInputStream log = new LineInputStream(logAccess,
				logAccess.getInputStream(null), "UTF-8");
		String line;
		int i = 0;
		while ((line = log.readNextLine()) != null) {
			i++;
			Assert.assertFalse(line.matches("[\n\r]+$"));
		}
		log.close();
		Assert.assertEquals(53, i);
	}

	/*
	 * @Test public void testReadingAfterEnd() throws IOException { FileLog log
	 * = new FileLog(writeString2Tmp("line1ü", "UTF-8"), "UTF-8");
	 * ArrayList<String> lines = new ArrayList<String>(); // No new lines
	 * Assert.assertEquals(log.getSize(), log.readLines(log.getSize(), lines,
	 * 1)); Assert.assertEquals(0, lines.size()); }
	 */
	public static File writeString2Tmp(final String text, final String charset)
			throws IOException {
		File tmp = File.createTempFile("test", "txt");
		tmp.deleteOnExit();
		FileUtils.writeStringToFile(tmp, text, charset);
		return tmp;
	}
}
