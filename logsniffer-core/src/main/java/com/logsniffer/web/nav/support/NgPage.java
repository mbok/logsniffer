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
package com.logsniffer.web.nav.support;

/**
 * Represents a a Angular page.
 * 
 * @author mbok
 * 
 */
public class NgPage extends NgTemplate {
	private String[] jsFiles;
	private String module;
	private String controller;

	/**
	 * @param jsFile
	 * @param module
	 * @param controller
	 * @param template
	 */
	public NgPage(final String jsFile, final String module, final String controller, final String template) {
		this(new String[] { jsFile }, module, controller, template);
	}

	/**
	 * @param jsFile
	 * @param module
	 * @param controller
	 * @param template
	 */
	public NgPage(final String[] jsFiles, final String module, final String controller, final String template) {
		super(template);
		this.jsFiles = jsFiles;
		this.module = module;
		this.controller = controller;
	}

	/**
	 * @return the module
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(final String module) {
		this.module = module;
	}

	/**
	 * @return the jsFiles
	 */
	public String[] getJsFiles() {
		return jsFiles;
	}

	/**
	 * @param jsFiles
	 *            the jsFiles to set
	 */
	public void setJsFiles(final String[] jsFiles) {
		this.jsFiles = jsFiles;
	}

	/**
	 * @return the controller
	 */
	public String getController() {
		return controller;
	}

	/**
	 * @param controller
	 *            the controller to set
	 */
	public void setController(final String controller) {
		this.controller = controller;
	}

	@Override
	public String getTypeName() {
		return "ngPage";
	}

}
