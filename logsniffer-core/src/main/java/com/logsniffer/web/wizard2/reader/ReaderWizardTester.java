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
package com.logsniffer.web.wizard2.reader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.support.ByteArrayLog;
import com.logsniffer.model.support.ByteLogAccess;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.reader.LogEntryReader.LogEntryConsumer;
import com.logsniffer.web.controller.LogEntriesResult;

/**
 * REST controller to test reader configuration.
 * 
 * @author mbok
 * 
 */
@Controller
public class ReaderWizardTester {
	/**
	 * Test input data.
	 * 
	 * @author mbok
	 * 
	 */
	public static class TestContext {
		@Valid
		private LogEntryReader<ByteLogAccess> reader;
		@NotEmpty
		private String testLogData;

		/**
		 * @return the reader
		 */
		public LogEntryReader<ByteLogAccess> getReader() {
			return reader;
		}

		/**
		 * @param reader
		 *            the reader to set
		 */
		public void setReader(final LogEntryReader<ByteLogAccess> reader) {
			this.reader = reader;
		}

		/**
		 * @return the testLogData
		 */
		public String getTestLogData() {
			return testLogData;
		}

		/**
		 * @param testLogData
		 *            the testLogData to set
		 */
		public void setTestLogData(final String testLogData) {
			this.testLogData = testLogData;
		}

	}

	@RequestMapping(value = "/wizards/readers/test", method = RequestMethod.POST)
	@ResponseBody
	LogEntriesResult testReading(@RequestBody @Valid final TestContext testCtx)
			throws UnsupportedEncodingException, IOException, FormatException {
		final ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
		final ByteArrayLog tempLog = new ByteArrayLog(testCtx.getTestLogData().getBytes("UTF-8"));
		testCtx.getReader().readEntries(tempLog, tempLog, null, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				entries.add(entry);
				return true;
			}
		});
		return new LogEntriesResult(testCtx.getReader().getFieldTypes(), entries, -1);
	}
}
