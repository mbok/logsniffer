//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
package com.logsniffer.web.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Override the {@link #unpack(WebAppContext)} method to unpack the WAR to a
 * destined location instead of to $name (<name>.WAR) folder, which conflicts
 * when the folder has a newer modification date as the WAR file, which is the
 * case for logsniffer, when starting the WAR from ${user.home}.
 * 
 * @author mbok
 *
 */
public class WebInfConfigurationUnpackOverridable extends WebInfConfiguration {
	private static final Logger LOG = Log.getLogger(WebInfConfigurationUnpackOverridable.class);

	@Override
	public void unpack(final WebAppContext context) throws IOException {
		Resource web_app = context.getBaseResource();
		_preUnpackBaseResource = context.getBaseResource();

		if (web_app == null) {
			String war = context.getWar();
			if (war != null && war.length() > 0)
				web_app = context.newResource(war);
			else
				web_app = context.getBaseResource();

			if (web_app == null)
				throw new IllegalStateException("No resourceBase or war set for context");

			// Accept aliases for WAR files
			if (web_app.getAlias() != null) {
				LOG.debug(web_app + " anti-aliased to " + web_app.getAlias());
				web_app = context.newResource(web_app.getAlias());
			}

			if (LOG.isDebugEnabled())
				LOG.debug("Try webapp=" + web_app + ", exists=" + web_app.exists() + ", directory="
						+ web_app.isDirectory() + " file=" + (web_app.getFile()));
			// Is the WAR usable directly?
			if (web_app.exists() && !web_app.isDirectory() && !web_app.toString().startsWith("jar:")) {
				// No - then lets see if it can be turned into a jar URL.
				Resource jarWebApp = JarResource.newJarResource(web_app);
				if (jarWebApp.exists() && jarWebApp.isDirectory())
					web_app = jarWebApp;
			}

			// If we should extract or the URL is still not usable
			if (web_app.exists()
					&& ((context.isCopyWebDir() && web_app.getFile() != null && web_app.getFile().isDirectory())
							|| (context.isExtractWAR() && web_app.getFile() != null && !web_app.getFile().isDirectory())
							|| (context.isExtractWAR() && web_app.getFile() == null) || !web_app.isDirectory())) {

				File extractedWebAppDir = getExtractedWebAppDir(context, war);

				if (web_app.getFile() != null && web_app.getFile().isDirectory()) {
					// Copy directory
					LOG.debug("Copy " + web_app + " to " + extractedWebAppDir);
					web_app.copyTo(extractedWebAppDir);
				} else {
					// Use a sentinel file that will exist only whilst the
					// extraction is taking place.
					// This will help us detect interrupted extractions.
					File extractionLock = new File(context.getTempDirectory(), ".extract_lock");

					if (!extractedWebAppDir.exists()) {
						// it hasn't been extracted before so extract it
						extractionLock.createNewFile();
						extractedWebAppDir.mkdir();
						LOG.info("Extract " + web_app + " to " + extractedWebAppDir);
						Resource jar_web_app = JarResource.newJarResource(web_app);
						jar_web_app.copyTo(extractedWebAppDir);
						extractionLock.delete();
					} else {
						// only extract if the war file is newer, or a
						// .extract_lock file is left behind meaning a possible
						// partial extraction
						if (web_app.lastModified() > extractedWebAppDir.lastModified() || extractionLock.exists()) {
							extractionLock.createNewFile();
							IO.delete(extractedWebAppDir);
							extractedWebAppDir.mkdir();
							LOG.info("Extract " + web_app + " to " + extractedWebAppDir);
							Resource jar_web_app = JarResource.newJarResource(web_app);
							jar_web_app.copyTo(extractedWebAppDir);
							extractionLock.delete();
						}
					}
				}
				web_app = Resource.newResource(extractedWebAppDir.getCanonicalPath());
			}

			// Now do we have something usable?
			if (!web_app.exists() || !web_app.isDirectory()) {
				LOG.warn("Web application not found " + war);
				throw new java.io.FileNotFoundException(war);
			}

			context.setBaseResource(web_app);

			if (LOG.isDebugEnabled())
				LOG.debug("webapp=" + web_app);
		}

		// Do we need to extract WEB-INF/lib?
		if (context.isCopyWebInf() && !context.isCopyWebDir()) {
			Resource web_inf = web_app.addPath("WEB-INF/");

			File extractedWebInfDir = new File(context.getTempDirectory(), "webinf");
			if (extractedWebInfDir.exists())
				IO.delete(extractedWebInfDir);
			extractedWebInfDir.mkdir();
			Resource web_inf_lib = web_inf.addPath("lib/");
			File webInfDir = new File(extractedWebInfDir, "WEB-INF");
			webInfDir.mkdir();

			if (web_inf_lib.exists()) {
				File webInfLibDir = new File(webInfDir, "lib");
				if (webInfLibDir.exists())
					IO.delete(webInfLibDir);
				webInfLibDir.mkdir();

				LOG.debug("Copying WEB-INF/lib " + web_inf_lib + " to " + webInfLibDir);
				web_inf_lib.copyTo(webInfLibDir);
			}

			Resource web_inf_classes = web_inf.addPath("classes/");
			if (web_inf_classes.exists()) {
				File webInfClassesDir = new File(webInfDir, "classes");
				if (webInfClassesDir.exists())
					IO.delete(webInfClassesDir);
				webInfClassesDir.mkdir();
				LOG.debug("Copying WEB-INF/classes from " + web_inf_classes + " to "
						+ webInfClassesDir.getAbsolutePath());
				web_inf_classes.copyTo(webInfClassesDir);
			}

			web_inf = Resource.newResource(extractedWebInfDir.getCanonicalPath());

			ResourceCollection rc = new ResourceCollection(web_inf, web_app);

			if (LOG.isDebugEnabled())
				LOG.debug("context.resourcebase = " + rc);

			context.setBaseResource(rc);
		}
	}

	protected File getExtractedWebAppDir(final WebAppContext context, final String war)
			throws IOException, MalformedURLException {
		File extractedWebAppDir = null;
		// Look for sibling directory.
		if (war != null) {
			// look for a sibling like "foo/" to a "foo.war"
			File warfile = Resource.newResource(war).getFile();
			if (warfile != null && warfile.getName().toLowerCase(Locale.ENGLISH).endsWith(".war")) {
				File sibling = new File(warfile.getParent(),
						warfile.getName().substring(0, warfile.getName().length() - 4));
				if (sibling.exists() && sibling.isDirectory() && sibling.canWrite())
					extractedWebAppDir = sibling;
			}
		}

		if (extractedWebAppDir == null)
			// Then extract it if necessary to the temporary location
			extractedWebAppDir = new File(context.getTempDirectory(), "webapp");
		return extractedWebAppDir;
	}

}
