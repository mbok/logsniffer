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
package com.logsniffer.web.controller.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.logsniffer.web.ViewController;
import com.logsniffer.web.app.NavigationAppConfig;
import com.logsniffer.web.nav.NavNode;

/**
 * Base controller for settings pages.
 * 
 * @author mbok
 * 
 */
@ViewController
public class SettingsBaseController {
	@Autowired
	@Qualifier(NavigationAppConfig.NAV_NODE_SETTINGS)
	private NavNode settingsNode;

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public ModelAndView renderSettings(
			@RequestParam(value = "path", required = false) final String path) {
		List<NavNode> breadcrumbNodes = new ArrayList<NavNode>();
		breadcrumbNodes.add(settingsNode);

		ModelAndView mv = new ModelAndView("settings/main");
		mv.addObject("rootNode", settingsNode);
		NavNode activeNode = getActiveNode(settingsNode, path);

		mv.addObject("breadcrumbNodes", getBreadcrumb(activeNode));
		mv.addObject("activeNode", activeNode);
		return mv;
	}

	private NavNode getActiveNode(final NavNode parentNode, final String path) {
		if (StringUtils.isBlank(path)) {
			if (!parentNode.getSubNodes().isEmpty()) {
				return parentNode.getSubNodes().get(0);
			}
		} else {
			String[] pathes = path.split("/", 2);
			for (NavNode c : parentNode.getSubNodes()) {
				if (c.getPath().equals(pathes[0])) {
					if (pathes.length == 1) {
						return c;
					} else {
						return getActiveNode(c, pathes[1]);
					}
				}
			}
		}
		return parentNode;
	}

	private List<NavNode> getBreadcrumb(NavNode activeNode) {
		ArrayList<NavNode> nodes = new ArrayList<NavNode>();
		while (activeNode.getParent() != null) {
			nodes.add(activeNode.getParent());
			activeNode = activeNode.getParent();
		}
		Collections.reverse(nodes);
		return nodes;
	}
}
