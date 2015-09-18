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
package com.logsniffer.config;

/**
 * A post constructor for configured beans.
 * 
 * @author mbok
 * 
 */
public interface BeanPostConstructor<BeanType> {

	/**
	 * The bean creation and configuration is split by
	 * {@link BeanConfigFactoryManager} into two phases: 1) creating the bean
	 * with proper config using the {@link #createBean(BeanConfig)} method 2)
	 * adapting further bean attributes in a post construct step, usually used
	 * to create nested beans.
	 * 
	 * @param bean
	 *            the configured bean
	 * @param configManager
	 *            involved config manager instance, which can be used to create
	 *            nested beans
	 * @throws ConfigException
	 *             in case of errors.
	 */
	void postConstruct(BeanType bean, BeanConfigFactoryManager configManager)
			throws ConfigException;
}
