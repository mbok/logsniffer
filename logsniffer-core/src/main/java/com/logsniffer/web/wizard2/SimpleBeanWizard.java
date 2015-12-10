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

/**
 * Simple wizard for exposing in Java configuration classes.
 * 
 * @author mbok
 * 
 * @param <BeanType>
 *            the bean type
 */
public class SimpleBeanWizard<BeanType extends ConfiguredBean> implements ExclusiveConfigBeanWizard<BeanType> {
	private final String nameKey;
	private final String wizardView;
	private final Class<BeanType> beanType;
	private final BeanType template;
	private Class<? super BeanType> exclusiveType;

	public SimpleBeanWizard(final String nameKey, final String wizardView, final Class<BeanType> beanType,
			final BeanType template) {
		this.nameKey = nameKey;
		this.wizardView = wizardView;
		this.beanType = beanType;
		this.template = template;
	}

	public SimpleBeanWizard(final String nameKey, final String wizardView, final Class<BeanType> beanType,
			final BeanType template, final Class<? super BeanType> exclusiveType) {
		this(nameKey, wizardView, beanType, template);
		this.exclusiveType = exclusiveType;
	}

	@Override
	public String getWizardView() {
		return wizardView;
	}

	@Override
	public Class<BeanType> getBeanType() {
		return beanType;
	}

	@Override
	public String getNameKey() {
		return nameKey;
	}

	@Override
	public BeanType getTemplate() {
		return template;
	}

	/**
	 * @return the exclusiveType
	 */
	@Override
	public Class<? super BeanType> getExclusiveType() {
		return exclusiveType;
	}

}
