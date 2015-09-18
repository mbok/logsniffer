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
package com.logsniffer.util.sql;

/**
 * Executes logic inside a transaction.
 * 
 * @author mbok
 * 
 */
public interface TxExecutor {
	/**
	 * Exception inside a transacted execution with a rollback in mind.
	 * 
	 * @author mbok
	 * 
	 */
	public static class TxNestedException extends Exception {
		private static final long serialVersionUID = 5173180647137939056L;

		public TxNestedException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

		public TxNestedException(String arg0) {
			super(arg0);
		}

	}

	/**
	 * Nested execution to execute inside a transaction.
	 * 
	 * @author mbok
	 * 
	 * @param <ReturnType>
	 *            the return type of execution
	 */
	public static interface Execution<ReturnType> {
		ReturnType execute() throws TxNestedException;
	}

	/**
	 * Execute the given execution inside a transaction.
	 * 
	 * @param exec
	 *            the execution
	 * @return the execution return value if any
	 * @throws TxNestedException
	 *             thrown to perform a rollback
	 */
	public <ReturnType> ReturnType execute(Execution<ReturnType> exec)
			throws TxNestedException;
}
