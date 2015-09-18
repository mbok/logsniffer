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
package com.logsniffer.model.file;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.ByteLogInputStream;

/**
 * Test for {@link FileLog}.
 * 
 * @author mbok
 * 
 */
public class FileLogTest {
	@Test
	public void testReadInt() throws IOException {
		File openFile = File.createTempFile("test", "txt");
		openFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(openFile);
		FileLog flog = new FileLog(openFile);
		IOUtils.write("line1\n", out);
		out.flush();
		ByteLogInputStream lis = new DirectFileLogAccess(flog)
				.getInputStream(null);
		// Log instanatiated before data is written
		assertEquals(-1, lis.read());
		flog = new FileLog(openFile);
		lis = new DirectFileLogAccess(flog).getInputStream(null);
		assertEquals('l', lis.read());
		assertEquals('i', lis.read());
		assertEquals('n', lis.read());
		assertEquals('e', lis.read());
		assertEquals('1', lis.read());
		assertEquals('\n', lis.read());
		assertEquals(-1, lis.read());

		// Write more, but lis doesn't see the new data due to size limitation
		IOUtils.write("l2\n", out);
		out.flush();
		assertEquals(-1, lis.read());
		lis.close();
	}

	@Test
	public void testReadArray() throws IOException {
		File openFile = File.createTempFile("test", "txt");
		openFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(openFile);
		FileLog flog = new FileLog(openFile);
		IOUtils.write("line1\n", out);
		out.flush();
		ByteLogInputStream lis = new DirectFileLogAccess(flog)
				.getInputStream(null);
		byte[] buffer = new byte[1024];
		// Log instantiated before data is written
		assertEquals(-1, lis.read(buffer));
		flog = new FileLog(openFile);
		lis = new DirectFileLogAccess(flog).getInputStream(null);

		assertEquals(6, lis.read(buffer));
		assertEquals(-1, lis.read(buffer));

		// Write more, but lis doesn't see the new data due to size limitation
		IOUtils.write("l2\n", out);
		out.flush();
		assertEquals(-1, lis.read(buffer));
		LogPointer pointer = lis.getPointer();

		// Reopen input stream
		flog = new FileLog(openFile);
		lis = new DirectFileLogAccess(flog).getInputStream(pointer);
		assertEquals(3, lis.read(buffer, 0, 3));
		assertEquals(-1, lis.read(buffer, 0, 1));
		assertEquals('l', buffer[0]);
		assertEquals('2', buffer[1]);
		assertEquals('\n', buffer[2]);
	}
}
