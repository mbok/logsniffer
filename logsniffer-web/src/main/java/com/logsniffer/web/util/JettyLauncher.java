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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Launches the Jetty server with LogSniffer's web app.
 * 
 * @author mbok
 * 
 */
public class JettyLauncher {

	/**
	 * Starts Jetty.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void start(final String[] args, final URL warLocation) throws Exception {
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);

		// Set JSP to use Standard JavaC always
		System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

		// Set some timeout options to make debugging easier.
		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(Integer.parseInt(System.getProperty("logsniffer.httpPort", "8082")));
		connector.setHost(System.getProperty("logsniffer.httpListenAddress", "0.0.0.0"));
		server.setConnectors(new Connector[] { connector });

		// Log.setLog(new Slf4jLog());

		// This webapp will use jsps and jstl. We need to enable the
		// AnnotationConfiguration in order to correctly
		// set up the jsp container
		Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
				"org.eclipse.jetty.annotations.AnnotationConfiguration");

		WebContextWithExtraConfigurations ctx = createWebAppContext();

		ctx.setServer(server);
		ctx.setWar(warLocation.toExternalForm());
		String ctxPath = System.getProperty("logsniffer.contextPath", "/");
		if (!ctxPath.startsWith("/")) {
			ctxPath = "/" + ctxPath;
		}
		ctx.setContextPath(ctxPath);
		configureWebAppContext(ctx);
		ctx.freezeConfigClasses();
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

	/**
	 * Creates a web context.
	 * 
	 * @return a web app context instance
	 */
	protected WebContextWithExtraConfigurations createWebAppContext() {
		return new WebContextWithExtraConfigurations();
	}

	/**
	 * Setups the web application context.
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	protected void configureWebAppContext(final WebContextWithExtraConfigurations context) throws Exception {
		context.setAttribute("javax.servlet.context.tempdir", getScratchDir());
		// Set the ContainerIncludeJarPattern so that jetty examines these
		// container-path jars for tlds, web-fragments etc.
		// If you omit the jar that contains the jstl .tlds, the jsp engine will
		// scan for them instead.
		context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
				".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
		context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
		context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
		context.addBean(new ServletContainerInitializersStarter(context), true);
		// context.setClassLoader(getUrlClassLoader());

		context.addServlet(jspServletHolder(), "*.jsp");
		context.replaceConfiguration(WebInfConfiguration.class, WebInfConfigurationHomeUnpacked.class);
	}

	/**
	 * Ensure the jsp engine is initialized correctly
	 */
	private List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
		initializers.add(initializer);
		return initializers;
	}

	/**
	 * Set Classloader of Context to be sane (needed for JSTL) JSP requires a
	 * non-System classloader, this simply wraps the embedded System classloader
	 * in a way that makes it suitable for JSP to use
	 */
	private ClassLoader getUrlClassLoader() {
		ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
		return jspClassLoader;
	}

	/**
	 * Create JSP Servlet (must be named "jsp")
	 */
	private ServletHolder jspServletHolder() {
		ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
		holderJsp.setInitOrder(0);
		holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
		holderJsp.setInitParameter("fork", "false");
		holderJsp.setInitParameter("xpoweredBy", "false");
		holderJsp.setInitParameter("compilerTargetVM", "1.7");
		holderJsp.setInitParameter("compilerSourceVM", "1.7");
		holderJsp.setInitParameter("keepgenerated", "true");
		return holderJsp;
	}

	/**
	 * Establish Scratch directory for the servlet context (used by JSP
	 * compilation)
	 */
	private File getScratchDir() throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File scratchDir = new File(tempDir, "embedded-jetty-jsp");
		if (!scratchDir.exists()) {
			if (!scratchDir.mkdirs()) {
				throw new IOException("Unable to create scratch directory: " + scratchDir);
			}
		}
		return scratchDir;
	}

}
