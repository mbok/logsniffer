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
package com.logsniffer.web.app;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logsniffer.web.nav.NavNode;
import com.logsniffer.web.nav.support.NgPage;

/**
 * Spring app config for navigation.
 * 
 * @author mbok
 * 
 */
@Configuration
public class NavigationAppConfig {
	public static final String NAV_NODE_SETTINGS = "settingsNode";

	/**
	 * 
	 * @return the settings root node exposed by {@link Qualifier}
	 *         {@value NavigationAppConfig#NAV_NODE_SETTINGS}.
	 */
	@Bean
	@Qualifier(NAV_NODE_SETTINGS)
	public NavNode settingsNode() {
		NavNode settings = new NavNode("Settings", "settings");

		NavNode general = new NavNode("General", "general");
		settings.addSubNode(general);
		general.setPageContext(new NgPage("ng/settings/general/app.js",
				"SettingsGeneralModule", "SettingsGeneralController",
				"ng/settings/general/main.html"));
		return settings;
	}
}
