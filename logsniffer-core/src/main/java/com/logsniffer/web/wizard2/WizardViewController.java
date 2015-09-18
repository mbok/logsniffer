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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.logsniffer.config.ConfigBeanTypeResolver;
import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.web.ViewController;

@ViewController
@RequestMapping(value = "/wizards/view")
public class WizardViewController {
	@Autowired
	private ConfigBeanWizardProvider wizardProvider;

	@Autowired
	private ConfigBeanTypeResolver beanTypeResolver;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView renderWizardView(
			@RequestParam("type") final String beanType)
			throws ClassNotFoundException {
		ConfigBeanWizard<?> wizard = wizardProvider.getWizard(beanTypeResolver
				.resolveTypeClass(beanType, ConfiguredBean.class));
		ModelAndView mv = new ModelAndView(wizard.getWizardView());
		mv.addObject("wizard", wizard);
		return mv;
	}
}
