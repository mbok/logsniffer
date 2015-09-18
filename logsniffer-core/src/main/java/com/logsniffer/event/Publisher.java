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
package com.logsniffer.event;

import com.logsniffer.config.ConfigException;
import com.logsniffer.config.ConfiguredBean;
import com.logsniffer.config.WrappedBean;

/**
 * Publisher for an encountered event.
 * 
 * @author mbok
 * 
 */
public interface Publisher extends ConfiguredBean {
	/**
	 * Indicates a failed publishing of an event.
	 * 
	 * @author mbok
	 * 
	 */
	public class PublishException extends Exception {
		private static final long serialVersionUID = -4791344818968763109L;

		public PublishException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public PublishException(final String message) {
			super(message);
		}

	}

	public void publish(Event event) throws PublishException;

	/**
	 * Wrapper for delegated publisher e.g. to allow lazy inititaing.
	 * 
	 * @author mbok
	 */
	public static abstract class PublisherWrapper implements Publisher,
			WrappedBean<Publisher> {
		private Publisher wrapped;

		public static final Publisher unwrap(final Publisher possiblyWrapped) {
			if (possiblyWrapped instanceof PublisherWrapper) {
				return ((PublisherWrapper) possiblyWrapped).getPublisher();
			}
			return possiblyWrapped;
		}

		private Publisher getPublisher() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void publish(final Event event) throws PublishException {
			try {
				getPublisher().publish(event);
			} catch (ConfigException e) {
				throw new PublishException(
						"Failed to create configured publisher", e);
			}
		}

	}
}
