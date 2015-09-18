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
package com.logsniffer.util;

import java.util.AbstractList;
import java.util.List;

/**
 * Lazy list which creates and wraps the real list the first time it's accessed
 * explicitly by {@link #get(int)} or {@link #size()}.
 * 
 * @author mbok
 * 
 * @param <E>
 *            the list value type
 */
public class LazyList<E> extends AbstractList<E> {
	/**
	 * Factory to create the real lazy instantiated List<E>.
	 * 
	 * @author mbok
	 * 
	 * @param <E>
	 *            the list value type
	 */
	public static interface ListFactory<E> {
		public List<E> createList();
	}

	private final ListFactory<E> factory;
	private List<E> wrapped;

	public LazyList(final ListFactory<E> factory) {
		this.factory = factory;
	}

	private List<E> getWrapped() {
		if (wrapped == null) {
			wrapped = factory.createList();
		}
		return wrapped;
	}

	@Override
	public E get(final int index) {
		return getWrapped().get(index);
	}

	@Override
	public int size() {
		return getWrapped().size();
	}

}
