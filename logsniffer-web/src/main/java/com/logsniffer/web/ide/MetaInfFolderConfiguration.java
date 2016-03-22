package com.logsniffer.web.ide;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Extension of MetaInfConfiguration, which also scans META-INF of all folders
 * (not only JARs) on the classpath to find resources, web-fragment.xml, tld.
 * 
 */
public class MetaInfFolderConfiguration extends MetaInfConfiguration {

	@Override
	public void preConfigure(final WebAppContext context) throws Exception {
		super.preConfigure(context); // let original MetaInfConfiguration do
										// it's thing first
		
		// Now add directory classpath resources
		final ArrayList<Resource> resources = new ArrayList<Resource>();
		ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls){
        	Resource r= new FileResource(url);
        	if (r.isDirectory()) {
        		resources.add(r);
        	}
        }
		scanJars(context, resources, false);
	}
}