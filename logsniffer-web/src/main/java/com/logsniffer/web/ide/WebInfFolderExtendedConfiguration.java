package com.logsniffer.web.ide;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

import com.logsniffer.web.util.WebInfConfigurationHomeUnpacked;

/**
 * Thanks to:
 * https://github.com/vorburger/EclipseWebDevEnv/blob/servlet30/simpleservers
 * /ch.vorburger.modudemo.server/src/main/java/ch/vorburger/modudemo/server/
 * 
 * Extended WebInfConfiguration.
 * 
 * Helps to accept e.g. web-fragment.xml from anywhere on the classpath, folders
 * or JARs, and not only from JARs neccessarily inside a WEB-INF/lib.
 * 
 * @see WebInfConfiguration
 * 
 * @see TBD
 * 
 * @author Michael Vorburger
 */
public class WebInfFolderExtendedConfiguration extends WebInfConfigurationHomeUnpacked {

	@Override
	protected List<Resource> findJars(final WebAppContext context) throws Exception {
		List<Resource> r = super.findJars(context); // let original
													// WebInfConfiguration do
													// it's thing first
		if (r == null) {
			r = new LinkedList<Resource>();
		}

		final List<Resource> containerJarResources = context.getMetaData().getOrderedWebInfJars();
		r.addAll(containerJarResources);

		return r;
	}

}