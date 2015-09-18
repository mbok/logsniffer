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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogEntryData;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.SeverityLevel.SeverityClassification;
import com.logsniffer.model.fields.FieldBaseTypes;
import com.logsniffer.model.support.ByteArrayLog;
import com.logsniffer.model.support.ByteLogInputStream;
import com.logsniffer.model.support.LineInputStream;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.support.BufferedConsumer;

/**
 * Test for {@link Log4jParser}.
 * 
 * @author mbok
 * 
 */
public class Log4jTextReaderTest {
	@Test
	public void testParsingConversionPattern() throws FormatException {
		Log4jTextReader r = new Log4jTextReader();
		r.setFormatPattern("%d{ABSOLUTE} %-5p [%c] %m%n");
		r.setCharset("UTF-8");
		r.setSpecifiersFieldMapping(Collections.singletonMap("m", "Message"));
		String[] fieldNames = r.getFieldTypes().keySet().toArray(new String[0]);
		Assert.assertEquals(6, fieldNames.length);
		Assert.assertEquals("d", fieldNames[0]);
		Assert.assertEquals("p", fieldNames[1]);
		Assert.assertEquals("c", fieldNames[2]);
		Assert.assertEquals("Message", fieldNames[3]);
		Assert.assertEquals("_timestamp", fieldNames[4]);
		Assert.assertEquals("_severity", fieldNames[5]);

		Assert.assertEquals(FieldBaseTypes.DATE,
				r.getFieldTypes().get(LogEntryData.FIELD_TIMESTAMP));
		Assert.assertEquals(FieldBaseTypes.STRING,
				r.getFieldTypes().get(fieldNames[0]));
		Assert.assertEquals(FieldBaseTypes.STRING,
				r.getFieldTypes().get(fieldNames[1]));
		Assert.assertEquals(FieldBaseTypes.STRING,
				r.getFieldTypes().get(fieldNames[2]));
		Assert.assertEquals(FieldBaseTypes.STRING,
				r.getFieldTypes().get(fieldNames[3]));
		Assert.assertEquals(FieldBaseTypes.DATE, r.getFieldTypes()
				.get(fieldNames[4]));
		Assert.assertEquals(FieldBaseTypes.SEVERITY,
				r.getFieldTypes().get(fieldNames[5]));
	}

	public static LogEntry[] readEntries(
			final LogEntryReader<ByteLogInputStream> reader,
			final ByteArrayLog log, final LogPointer start, final int size)
			throws IOException, FormatException {
		BufferedConsumer c = new BufferedConsumer(size);
		reader.readEntries(log, log, start, c);
		return c.getBuffer().toArray(new LogEntry[0]);
	}

	@Test
	public void testParsingOneLine() throws FormatException,
			UnsupportedEncodingException, IOException, ParseException {
		Log4jTextReader reader = new Log4jTextReader(
				"%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		String logLine1 = "00:27:29,456 DEBUG [com.logsniffer.parser.log4j.Log4jParser] Prepared parsing pattern";
		ByteArrayLog log = createLog(0, logLine1);
		LogPointer start = log.getInputStream(null).getPointer();
		LogEntry[] entries = readEntries(reader, log, null, 1);

		Assert.assertEquals(1, entries.length);
		Assert.assertEquals(logLine1, entries[0].getRawContent());
		Assert.assertEquals("00:27:29,456", entries[0].getFields().get("d"));
		Assert.assertEquals(SeverityClassification.DEBUG, entries[0]
				.getSeverity().getClassification());
		Assert.assertEquals(
				new SimpleDateFormat("HH:mm:ss,SSS").parse("00:27:29,456"),
				entries[0].getTimeStamp());
		Assert.assertEquals(0,
				log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals(logLine1.getBytes("UTF-8").length,
				log.getDifference(start, entries[0].getEndOffset()));
	}

	@Test
	public void testISO8601DateFormat() throws FormatException,
			UnsupportedEncodingException, IOException, ParseException {
		Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] %m%n",
				"UTF-8");
		String logLine1 = "2013-03-24 00:27:29,456 DEBUG [com.logsniffer.parser.log4j.Log4jParser] Prepared parsing pattern";
		ByteArrayLog log = createLog(0, logLine1);
		LogPointer start = log.getInputStream(null).getPointer();
		LogEntry[] entries = readEntries(reader, log, null, 1);
		Assert.assertEquals(1, entries.length);
		Assert.assertEquals(logLine1, entries[0].getRawContent());
		Assert.assertEquals(SeverityClassification.DEBUG, entries[0]
				.getSeverity().getClassification());
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")
				.parse("2013-03-24 00:27:29,456"), entries[0].getTimeStamp());
		Assert.assertEquals(0,
				log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals(logLine1.getBytes("UTF-8").length,
				log.getDifference(start, entries[0].getEndOffset()));
	}

	@Test
	public void testParsingOneLineWithException() throws ParseException,
			UnsupportedEncodingException, IOException, FormatException {
		Log4jTextReader reader = new Log4jTextReader(
				"%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		String[] logLines = new String[] {
				"00:27:29,456 ERROR [com.logsniffer.parser.log4j.Log4jParser] Prepared parsing pattern",
				"java.lang.Exception: kll",
				"at com.logsniffer.parser.log4j.Log4jParser.setConversionPattern(Log4jParser.java:280)",
				"at com.logsniffer.parser.log4j.Log4jParserTest.testParsingOneLineWithException(Log4jParserTest.java:44",
				"22:27:29,456 INFO  [com.logsniffer.parser.log4j.Log4jParser] Finished" };
		ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		LogPointer start = log.getInputStream(null).getPointer();
		LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n"
				+ logLines[2] + "\n" + logLines[3], entries[0].getRawContent());
		Assert.assertEquals("Prepared parsing pattern\n" + logLines[1] + "\n"
				+ logLines[2] + "\n" + logLines[3],
				entries[0].getFields().get("m"));
		Assert.assertEquals(SeverityClassification.ERROR, entries[0]
				.getSeverity().getClassification());
		Assert.assertEquals(
				new SimpleDateFormat("HH:mm:ss,SSS").parse("00:27:29,456"),
				entries[0].getTimeStamp());
		Assert.assertEquals(0,
				log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals(
				(logLines[0] + logLines[1] + logLines[2] + logLines[3])
						.getBytes("UTF-8").length + 4, log.getDifference(start,
						entries[0].getEndOffset()));

		// Check last entry
		Assert.assertEquals(logLines[4], entries[1].getRawContent());
		Assert.assertEquals(SeverityClassification.INFORMATIONAL, entries[1]
				.getSeverity().getClassification());
		Assert.assertEquals(
				new SimpleDateFormat("HH:mm:ss,SSS").parse("22:27:29,456"),
				entries[1].getTimeStamp());
		Assert.assertEquals(
				(logLines[0] + logLines[1] + logLines[2] + logLines[3])
						.getBytes("UTF-8").length + 4, log.getDifference(start,
						entries[1].getStartOffset()));
		Assert.assertEquals(
				StringUtils.join(logLines, "\n").getBytes("UTF-8").length,
				log.getDifference(start, entries[1].getEndOffset()));
	}

	@Test
	public void testOnlyOverflow() throws ParseException,
			UnsupportedEncodingException, IOException, FormatException {
		Log4jTextReader reader = new Log4jTextReader(
				"%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		String[] logLines = new String[] {
				"java.lang.Exception: kll",
				"at com.logsniffer.parser.log4j.Log4jParser.setConversionPattern(Log4jParser.java:280)",
				"at com.logsniffer.parser.log4j.Log4jParserTest.testParsingOneLineWithException(Log4jParserTest.java:44",
				"22:27:29,456 INFO  [com.logsniffer.parser.log4j.Log4jParser] Finished" };
		ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		LogPointer start = log.getInputStream(null).getPointer();
		LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n"
				+ logLines[2], entries[0].getRawContent());
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n"
				+ logLines[2], entries[0].getFields().get("m"));
		Assert.assertNull(entries[0].getSeverity());
		Assert.assertNull(entries[0].getTimeStamp());
		Assert.assertEquals(0,
				log.getDifference(start, entries[0].getStartOffset()));
	}

	@Test
	public void testInvalidPattern() throws UnsupportedEncodingException,
			IOException, FormatException {
		Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] (%t) %m%n",
				"UTF-8");
		String[] logLines = {
				"2013-09-28 01:28:27,145 INFO  [org.jboss.resource.deployers.RARDeployment] (main) Required license terms exist, view vfszip:/D:/work/scaleborn3/selunit-cloud-backend-jboss/dev/jboss-5.1.0.GA/server/default/deploy/jboss-local-jdbc.rar/META-INF/ra.xml",
				"2013-09-28 01:28:27,193 INFO  [org.jboss.resource.deployers.RARDeployment] (main) Required license terms exist, view vfszip:/D:/work/scaleborn3/selunit-cloud-backend-jboss/dev/jboss-5.1.0.GA/server/default/deploy/jboss-xa-jdbc.rar/META-INF/ra.xml" };
		ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0], entries[0].getRawContent());
		Assert.assertEquals(logLines[1], entries[1].getRawContent());
	}

	/**
	 * Tests matching bug caused by new line character caused by
	 * {@link LineInputStream}.
	 */
	@Test
	public void testInvalidPatternFromFile()
			throws UnsupportedEncodingException, IOException, FormatException {
		Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] (%t) %m%n",
				"UTF-8");
		File f = new File("src/test/resources/logs/nl-triming.txt");
		LogEntry[] entries = readEntries(reader,
				new ByteArrayLog(FileUtils.readFileToByteArray(f)), null, 2000);

		Assert.assertEquals(46, entries.length);
	}

	public static ByteArrayLog createLog(final long offest, final String lines)
			throws UnsupportedEncodingException, IOException {
		return new ByteArrayLog(lines.getBytes("UTF-8"));
	}
}
