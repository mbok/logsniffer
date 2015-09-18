package com.logsniffer.web.ide;

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

	public WebContextWithExtraConfigurations(final String webApp,
			final String contextPath) {
		super(webApp, contextPath);
	}

	public WebContextWithExtraConfigurations() {
		super();
	}

	public WebContextWithExtraConfigurations(final HandlerContainer parent,
			final String webApp, final String contextPath) {
		super(parent, webApp, contextPath);
	}

	public WebContextWithExtraConfigurations(
			final SessionHandler sessionHandler,
			final SecurityHandler securityHandler,
			final ServletHandler servletHandler, final ErrorHandler errorHandler) {
		super(sessionHandler, securityHandler, servletHandler, errorHandler);
	}

	public <T extends Configuration> void replaceConfiguration(
			final Class<T> toReplace, final Configuration newConfiguration)
			throws Exception {
		loadConfigurations(); // Force loading of default configurations
		final Configuration[] configs = getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			if (configs[i].getClass().equals(toReplace)) {
				configs[i] = newConfiguration;
				return;
			}
		}
		throw new IllegalStateException(toReplace.toString() + " not found");
	}

	public void addConfiguration(final Configuration configuration)
			throws Exception {
		loadConfigurations(); // Force loading of default configurations
		final Configuration[] configs = getConfigurations();
		final Configuration[] newConfig = new Configuration[configs.length + 1];
		newConfig[configs.length] = configuration;
		setConfigurations(newConfig);
	}

}