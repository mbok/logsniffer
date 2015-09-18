package com.logsniffer.web.ide;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.util.resource.Resource;

/**
 * 
 * Thanks to:
 * https://github.com/vorburger/EclipseWebDevEnv/blob/servlet30/simpleservers
 * /ch.vorburger.modudemo.server/src/main/java/ch/vorburger/modudemo/server/
 * 
 */
public final class Util {
	private Util() {
	}

	public static Resource chop(final URL baseURL, final String toChop)
			throws MalformedURLException, IOException {
		String base = baseURL.toExternalForm();
		if (!base.endsWith(toChop)) {
			throw new IllegalArgumentException(base + " does not endWith "
					+ toChop);
		}
		base = base.substring(0, base.length() - toChop.length());

		if (base.startsWith("jar:file:") && base.endsWith("!")) {
			// If it was a jar:file:/.../.jar!/META-INF/web-fragment.xml, then
			// 'jar:' & '!' has to go as well:
			base = base.substring(0, base.length() - 1);
			base = "file:" + base.substring("jar:file:".length());
		}
		return Resource.newResource(base);
	}
}