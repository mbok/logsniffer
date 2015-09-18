package com.logsniffer.web.ide;

import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Thanks to:
 * https://github.com/vorburger/EclipseWebDevEnv/blob/servlet30/simpleservers
 * /ch.vorburger.modudemo.server/src/main/java/ch/vorburger/modudemo/server/
 * 
 * FragmentFolderConfiguration.
 * 
 * Extension of FragmentConfiguration to support web-fragment.xml in folders as
 * well as inside JARs.
 * 
 * @see FragmentConfiguration
 * 
 * @see TBD
 * 
 * @author Michael Vorburger
 */
public class FragmentFolderConfiguration extends FragmentConfiguration {

	/**
	 * Overriden method which, contrary to the original implementation in the
	 * parent class, add directly the web-fragment.xml resource to the MetaData,
	 * instead of re-creating it with a forced jar prefix.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void findWebFragments(final WebAppContext context,
			final MetaData metaData) throws Exception {
		List<Resource> frags = (List<Resource>) context
				.getAttribute(FRAGMENT_RESOURCES);
		if (frags != null) {
			for (Resource frag : frags) {
				Resource parentResource = Util.chop(frag.getURL(),
						"/META-INF/web-fragment.xml");
				metaData.addFragment(parentResource, frag);
			}
		}
	}

}