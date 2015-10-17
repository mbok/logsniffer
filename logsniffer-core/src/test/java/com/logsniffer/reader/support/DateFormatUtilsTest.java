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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link DateFormatUtils}.
 *
 * @author mbok
 *
 */
public class DateFormatUtilsTest {

	@Test
	public void testSimpleQuotes() {
		Assert.assertEquals("\\Qdate1\\E\\Qdate2\\E", DateFormatUtils
				.getRegexPattern(new SimpleDateFormat("'date1''date2'")));
		Assert.assertEquals("\\Qdate1\\E \\Qdate2\\E", DateFormatUtils
				.getRegexPattern(new SimpleDateFormat("'date1' 'date2'")));
	}

	@Test
	public void testDoubleQuotes() {
		Assert.assertEquals("'\\Qdouble quotes\\E'", DateFormatUtils
				.getRegexPattern(new SimpleDateFormat("'''double quotes'''")));
		Assert.assertEquals("'\\Qdouble quotes\\E''", DateFormatUtils
				.getRegexPattern(new SimpleDateFormat("'''double quotes'''''")));
	}

	@Test
	public void testEras() {
		Date now = new Date();
		SimpleDateFormat germanFormat = new SimpleDateFormat("G", Locale.GERMAN);
		String germanDate = germanFormat.format(now);
		System.out.println(germanDate);
		Matcher germanMatcher = Pattern.compile(
				DateFormatUtils.getRegexPattern(germanFormat)).matcher(
				germanDate);
		Assert.assertEquals(true, germanMatcher.matches());
		Assert.assertEquals(germanDate, germanMatcher.group());
	}

}
