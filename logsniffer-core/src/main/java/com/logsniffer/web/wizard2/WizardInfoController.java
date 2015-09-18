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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.config.ConfigBeanTypeResolver;
import com.logsniffer.config.ConfiguredBean;

@RestController
public class WizardInfoController {
	@Autowired
	private ConfigBeanWizardProvider wizardProvider;

	@Autowired
	private ConfigBeanTypeResolver beanTypeResolver;

	@Autowired
	private MessageSource msgSource;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/wizards", method = RequestMethod.GET)
	@ResponseBody
	public List<WizardInfo> getWizardsInfo(
			@RequestParam("supportType") final String supportType,
			final Locale locale) throws ClassNotFoundException {
		return getWizardsInfo(
				(Class<? extends ConfiguredBean>) Class.forName(supportType),
				locale);
	}

	public List<WizardInfo> getWizardsInfo(
			final Class<? extends ConfiguredBean> supportType,
			final Locale locale) {
		ArrayList<WizardInfo> infos = new ArrayList<WizardInfo>();
		ConfigBeanWizard<? extends ConfiguredBean>[] wizards = wizardProvider
				.getWizards(supportType);
		for (ConfigBeanWizard<? extends ConfiguredBean> wizard : wizards) {
			infos.add(new WizardInfo(wizard.getTemplate(), beanTypeResolver
					.resolveTypeName(wizard.getBeanType()), wizard
					.getWizardView(),
					msgSource.getMessage(wizard.getNameKey(), new Object[0],
							wizard.getBeanType().getSimpleName(), locale)));
		}
		return infos;
	}
}
