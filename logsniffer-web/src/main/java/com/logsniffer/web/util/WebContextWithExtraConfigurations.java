package com.logsniffer.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Thanks to:
 * https://github.com/vorburger/EclipseWebDevEnv/blob/servlet30/simpleservers
 * /ch.vorburger.modudemo.server/src/main/java/ch/vorburger/modudemo/server/
 * 
 * WebAppContext allowing to register additional Configurations.
 * 
 * @see TBD
 * 
 * @author Michael Vorburger
 */
public class WebContextWithExtraConfigurations extends WebAppContext {

	private final HashMap<String, String> configClasses = new LinkedHashMap<>();

	{
		for (String c : WebAppContext.DEFAULT_CONFIGURATION_CLASSES) {
			configClasses.put(c, c);
		}
	}

	public WebContextWithExtraConfigurations(final String webApp, final String contextPath) {
		super(webApp, contextPath);
	}

	public WebContextWithExtraConfigurations() {
		super();
	}

	public WebContextWithExtraConfigurations(final HandlerContainer parent, final String webApp,
			final String contextPath) {
		super(parent, webApp, contextPath);
	}

	public WebContextWithExtraConfigurations(final SessionHandler sessionHandler, final SecurityHandler securityHandler,
			final ServletHandler servletHandler, final ErrorHandler errorHandler) {
		super(sessionHandler, securityHandler, servletHandler, errorHandler);
	}

	public <T extends Configuration> void replaceConfiguration(final Class<T> toReplace,
			final Class<? extends Configuration> newConfiguration) throws Exception {
		for (String c : configClasses.keySet()) {
			if (c.equals(toReplace.getName())) {
				configClasses.put(c, newConfiguration.getName());
				return;
			}
		}
		throw new IllegalStateException(toReplace.toString() + " not found");
	}

	public void freezeConfigClasses() {
		setConfigurationClasses(new ArrayList<>(configClasses.values()));
	}

}