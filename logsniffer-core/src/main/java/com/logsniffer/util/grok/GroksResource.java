package com.logsniffer.util.grok;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes registered {@link Grok}s via REST.
 * 
 * @author mbok
 *
 */
@RestController
public class GroksResource {
	@Autowired
	private GroksRegistry registry;

	/**
	 * Exposes registered {@link Grok} groups.
	 * 
	 * @return grok groups
	 */
	@RequestMapping(path = "utils/groks/groups", method = RequestMethod.GET)
	public Map<String, Map<String, Grok>> getGrokGroups() {
		return registry.getGrokGroups();
	}
}
