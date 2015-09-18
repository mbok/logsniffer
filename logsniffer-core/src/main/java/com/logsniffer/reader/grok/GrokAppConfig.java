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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Configures a {@link GroksRegistry} with grok patterns read from
 * classpath*:/grok-patterns/*.
 * 
 * @author mbok
 * 
 */
@Configuration
public class GrokAppConfig {
	@Bean
	public GroksRegistry groksRegistry() throws IOException, GrokException {
		GroksRegistry registry = new GroksRegistry();
		PathMatchingResourcePatternResolver pathMatcher = new PathMatchingResourcePatternResolver();
		Resource[] classPathPatterns = pathMatcher
				.getResources("classpath*:/grok-patterns/*");
		Arrays.sort(classPathPatterns, new Comparator<Resource>() {
			@Override
			public int compare(final Resource o1, final Resource o2) {
				return o1.getFilename().compareTo(o2.getFilename());
			}
		});
		LinkedHashMap<String, String[]> grokBlocks = new LinkedHashMap<String, String[]>();
		for (Resource r : classPathPatterns) {
			grokBlocks.put(r.getFilename(),
					IOUtils.readLines(r.getInputStream())
							.toArray(new String[0]));
		}
		registry.registerPatternBlocks(grokBlocks);
		return registry;
	}
}
