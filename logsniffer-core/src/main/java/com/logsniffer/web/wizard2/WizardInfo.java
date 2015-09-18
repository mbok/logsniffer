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
package com.logsniffer.web.wizard2;

import com.logsniffer.config.ConfiguredBean;

public final class WizardInfo {
	private String beanType;

	private String view;

	private String label;

	private ConfiguredBean template;

	public WizardInfo(final ConfiguredBean template, final String beanType,
			final String view, final String label) {
		super();
		this.template = template;
		this.beanType = beanType;
		this.view = view;
		this.label = label;
	}

	/**
	 * @return the beanType
	 */
	public String getBeanType() {
		return beanType;
	}

	/**
	 * @param beanType
	 *            the beanType to set
	 */
	public void setBeanType(final String beanType) {
		this.beanType = beanType;
	}

	/**
	 * @return the view
	 */
	public String getView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(final String view) {
		this.view = view;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * @return the template
	 */
	public ConfiguredBean getTemplate() {
		return template;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(final ConfiguredBean template) {
		this.template = template;
	}

}
