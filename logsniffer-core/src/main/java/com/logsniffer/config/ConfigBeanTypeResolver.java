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
 * Resolves type for a {@link ConfiguredBean} class and vice versa used for JSON
 * seriliaztion / desrialiaztion.
 * 
 * @author mbok
 * 
 */
public interface ConfigBeanTypeResolver {
	/**
	 * Resolves a name for given class which can be used later to resolve the
	 * corresponding class.
	 * 
	 * @param clazz
	 *            class of config bean
	 * @return name for given class which can be used later to resolve the
	 *         corresponding class
	 * @return in case no names are defined for the given type
	 */
	String resolveTypeName(Class<? extends ConfiguredBean> clazz)
			throws ConfigException;

	/**
	 * Resolves the config bean class which corresponds to the given name. Note
	 * that by design multiple names can reference the same class to support
	 * upgrade compatibility.
	 * 
	 * @param name
	 *            name of config bean
	 * @param wantedSuperType
	 *            super type
	 * @return config bean class which corresponds to the given name
	 * @throws ConfigException
	 *             in case of no class could be found which matches the given
	 *             name or in case of embedded exceptions
	 */
	<T extends ConfiguredBean> Class<? extends T> resolveTypeClass(String name,
			Class<T> wantedSuperType) throws ConfigException;
}
