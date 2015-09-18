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
package com.logsniffer.util.value;

import java.io.IOException;

/**
 * Extendes config value source with possibility to store/update values.
 * 
 * @author mbok
 * 
 */
public interface ConfigValueStore extends ConfigValueSource {
	/**
	 * Stores a value with given key.
	 * 
	 * @param key
	 *            the key for config value
	 * @param value
	 *            the value to store
	 * @throws IOException
	 *             in case of store errors
	 */
	public void store(String key, String value) throws IOException;
}
