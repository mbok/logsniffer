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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logsniffer.config.ConfiguredBean;

/**
 * Provides wizards related to a requested bean type.
 * 
 * @author mbok
 * 
 */
@Component
public class ConfigBeanWizardProvider {
	@Autowired(required = false)
	private ConfigBeanWizard<?>[] wizards;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <BeanType extends ConfiguredBean> ConfigBeanWizard<BeanType>[] getWizards(
			final Class<BeanType> supportType) {
		final ArrayList<ConfigBeanWizard<BeanType>> wzs = new ArrayList<ConfigBeanWizard<BeanType>>();
		for (final ConfigBeanWizard<?> wizard : wizards) {
			if (wizard instanceof ExclusiveConfigBeanWizard
					&& ((ExclusiveConfigBeanWizard) wizard).getExclusiveType() != null) {
				if (supportType == ((ExclusiveConfigBeanWizard) wizard).getExclusiveType()) {
					wzs.add((ConfigBeanWizard<BeanType>) wizard);
				}
				continue;
			}
			if (supportType.isAssignableFrom(wizard.getBeanType())) {
				wzs.add((ConfigBeanWizard<BeanType>) wizard);
			}
		}
		return wzs.toArray(new ConfigBeanWizard[wzs.size()]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <BeanType extends ConfiguredBean> ConfigBeanWizard<BeanType> getWizard(
			final Class<BeanType> actualBeanClass) {
		for (final ConfigBeanWizard<?> wizard : wizards) {
			if (wizard instanceof ExclusiveConfigBeanWizard
					&& ((ExclusiveConfigBeanWizard) wizard).getExclusiveType() != null) {
				if (actualBeanClass == ((ExclusiveConfigBeanWizard) wizard).getExclusiveType()) {
					return (ConfigBeanWizard<BeanType>) wizard;
				}
			}

			if (actualBeanClass.isAssignableFrom(wizard.getBeanType())) {
				return (ConfigBeanWizard<BeanType>) wizard;
			}
		}
		return null;
	}
}
