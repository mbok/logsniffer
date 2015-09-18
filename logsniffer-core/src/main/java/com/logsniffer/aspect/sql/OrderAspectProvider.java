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
package com.logsniffer.aspect.sql;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.logsniffer.aspect.AspectHost;

/**
 * Order by aspect.
 * 
 * @author mbok
 * 
 * @param <T>
 * @param <AspectType>
 */
public class OrderAspectProvider<T extends AspectHost, AspectType> implements
		QueryAdaptor<T, AspectType> {

	private final String orderBy;

	public OrderAspectProvider(final String orderBy) {
		super();
		this.orderBy = orderBy;
	}

	@Override
	public String getQuery(final String innerQuery) {
		return "SELECT * FROM (" + innerQuery + ") ORDER BY " + orderBy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RowMapper<T> getRowMapper(final RowMapper<? extends T> innerMapper) {
		return (RowMapper<T>) innerMapper;
	}

	@Override
	public List<Object> getQueryArgs(final List<Object> innerArgs) {
		return innerArgs;
	}

	@Override
	public AspectType getApsect(final T host) {
		return null;
	}

}
