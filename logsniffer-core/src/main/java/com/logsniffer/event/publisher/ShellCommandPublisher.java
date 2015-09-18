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
package com.logsniffer.event.publisher;

import org.apache.velocity.VelocityContext;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.config.PostConstructed;
import com.logsniffer.event.Event;
import com.logsniffer.event.Publisher;
import com.logsniffer.event.publisher.ShellCommandPublisher.ShellPublisherConstructor;

@PostConstructed(constructor = ShellPublisherConstructor.class)
public class ShellCommandPublisher implements Publisher {
	@JsonIgnore
	private VelocityEventRenderer velocityRenderer;

	@NotEmpty
	@JsonProperty
	private String shellScript;

	@JsonProperty
	private String workingDir;

	@Component
	public static class ShellPublisherConstructor implements
			BeanPostConstructor<ShellCommandPublisher> {
		@Autowired
		private VelocityEventRenderer velocityRenderer;

		@Override
		public void postConstruct(final ShellCommandPublisher bean,
				final BeanConfigFactoryManager configManager)
				throws ConfigException {
			bean.velocityRenderer = velocityRenderer;
		}

	}

	@Override
	public void publish(final Event event) throws PublishException {
		VelocityContext velocityContext = velocityRenderer.getContext(event);

	}

	/**
	 * @return the shellScript
	 */
	public String getShellScript() {
		return shellScript;
	}

	/**
	 * @param shellScript
	 *            the shellScript to set
	 */
	public void setShellScript(final String shellScript) {
		this.shellScript = shellScript;
	}

	/**
	 * @return the workingDir
	 */
	public String getWorkingDir() {
		return workingDir;
	}

	/**
	 * @param workingDir
	 *            the workingDir to set
	 */
	public void setWorkingDir(final String workingDir) {
		this.workingDir = workingDir;
	}

}
