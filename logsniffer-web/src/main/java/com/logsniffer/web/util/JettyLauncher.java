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
package com.logsniffer.web.util;

import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Launches the Jetty server with LogSniffer's web app.
 * 
 * @author mbok
 * 
 */
public class JettyLauncher {

	/**
	 * Creates a web app context.
	 * 
	 * @return a web app context.
	 */
	protected WebAppContext createWebAppContext() throws Exception {
		return new WebAppContext();
	}

	/**
	 * Starts Jetty.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void start(final String[] args, final URL warLocation)
			throws Exception {
		Server server = new Server();

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(1);
		threadPool.setMaxThreads(100);
		server.setThreadPool(threadPool);

		SocketConnector connector = new SocketConnector();
		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(Integer.parseInt(System.getProperty(
				"logsniffer.httpPort", "8082")));
		connector.setHost(System.getProperty("logsniffer.httpListenAddress",
				"0.0.0.0"));
		server.setConnectors(new Connector[] { connector });

		WebAppContext ctx = createWebAppContext();
		ctx.setWar(warLocation.toExternalForm());
		String ctxPath = System.getProperty("logsniffer.contextPath", "/");
		if (!ctxPath.startsWith("/")) {
			ctxPath = "/" + ctxPath;
		}
		ctx.setContextPath(ctxPath);
		server.setHandler(ctx);

		server.setStopAtShutdown(true);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}

}
