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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogRawAccessor;
import com.logsniffer.model.support.BaseLogsSource;
import com.logsniffer.model.support.ByteLogAccess;

/**
 * Combines log files matching an Ant-like path pattern to one source. Source
 * for rolling log files based on {@link WildcardLogsSource}. Registered to
 * application context in scope of {@link ConfigurableBeanFactory}.
 * 
 * @author mbok
 * 
 */
@Component
public class WildcardLogsSource extends BaseLogsSource<ByteLogAccess> {
	private static Logger logger = LoggerFactory.getLogger(WildcardLogsSource.class);

	private LogRawAccessor<ByteLogAccess, FileLog> logAccessAdapter;

	private String pattern;

	/**
	 * @return the baseDir
	 */
	@Deprecated
	public String getBaseDir() {
		return null;
	}

	/**
	 * @param baseDir
	 *            the baseDir to set
	 */
	@Deprecated
	public void setBaseDir(final String baseDir) {
		// NOP
	}

	/**
	 * @return the pattern
	 */
	@JsonProperty
	@NotEmpty
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(final String pattern) {
		this.pattern = FilenameUtils.separatorsToUnix(pattern);
	}

	@Override
	public List<Log> getLogs() throws IOException {
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		resolver.setPathMatcher(new AntPathMatcher());
		final Resource[] resources = resolver.getResources("file:" + getPattern());
		final ArrayList<Log> logs = new ArrayList<Log>(resources.length);
		// TODO Decouple direct file log association
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].exists()) {
				if (resources[i].getFile().isFile()) {
					logs.add(new FileLog(resources[i].getFile()));
				}
			} else {
				logger.info("Ignore not existent file: {}", resources[i].getFile());
			}
		}
		return logs;
	}

	@Override
	public Log getLog(final String path) throws IOException {
		final File f = new File(path);
		if (f.exists()) {
			return new FileLog(f);
		} else {
			return null;
		}
	}

	@Override
	public ByteLogAccess getLogAccess(final Log origLog) throws IOException {
		final FileLog log = (FileLog) getLog(origLog.getPath());
		if (log != null) {
			return getLogAccessAdapter() != null ? getLogAccessAdapter().getLogAccess(log)
					: new DirectFileLogAccess(log);
		} else {
			return null;
		}
	}

	public LogRawAccessor<ByteLogAccess, FileLog> getLogAccessAdapter() {
		return logAccessAdapter;
	}

	public void setLogAccessAdapter(final LogRawAccessor<ByteLogAccess, FileLog> logAccessAdapter) {
		this.logAccessAdapter = logAccessAdapter;
	}
}
