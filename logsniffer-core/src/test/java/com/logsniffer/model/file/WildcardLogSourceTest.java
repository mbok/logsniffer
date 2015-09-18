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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.logsniffer.model.Log;

/**
 * Test for {@link WildcardLogsSource}.
 * 
 * @author mbok
 * 
 */
public class WildcardLogSourceTest {

	@Test
	public void testLogsResolving() throws IOException {
		File tmp = File.createTempFile("sdkj", "jk");
		tmp.deleteOnExit();
		File tmpFolder1 = new File(tmp.getPath() + "d", "f1");
		tmpFolder1.mkdirs();
		tmpFolder1.deleteOnExit();
		File tmpFolder2 = new File(tmp.getPath() + "d", "f2");
		tmpFolder2.mkdirs();
		tmpFolder2.deleteOnExit();
		FileUtils.write(new File(tmpFolder1, "1.txt"), "txt");
		FileUtils.write(new File(tmpFolder1, "2.txt"), "txt");
		FileUtils.write(new File(tmpFolder1, "3.log"), "log");
		FileUtils.write(new File(tmpFolder2, "22.txt"), "txt");
		File tmpDir = new File(tmpFolder2, "folder.txt");
		tmpDir.mkdir();
		tmpDir.deleteOnExit();

		// Check now
		WildcardLogsSource source = new WildcardLogsSource();
		source.setPattern(tmp.getPath() + "d/*.txt");
		Assert.assertEquals(0, source.getLogs().size());
		source.setPattern(tmp.getPath() + "d/**/*.txt");
		Log[] logs = source.getLogs().toArray(new Log[0]);
		Assert.assertEquals(3, logs.length);
		Arrays.sort(logs, new Comparator<Log>() {
			@Override
			public int compare(final Log o1, final Log o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		});
		Assert.assertTrue(logs[0].getPath().endsWith("1.txt"));
		Assert.assertTrue(logs[1].getPath().endsWith("2.txt"));
		Assert.assertTrue(logs[2].getPath().endsWith("22.txt"));
	}
}
