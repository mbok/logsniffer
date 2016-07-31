package com.logsniffer.event.es;

/**
 * Strategy for generating ES index names for events related to a sniffer.
 * 
 * @author mbok
 *
 */
public interface IndexNamingStrategy {
	/**
	 * Generates name of the active ES index to store events currently to.
	 * 
	 * @param sniffer
	 *            id
	 * @return ES index name to store the events of given sniffer to
	 */
	public String buildActiveName(long snifferId);

	/**
	 * Get names for potential indexes containing all events related to given
	 * sniffer.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return at least one index name
	 */
	public String[] getRetrievalNames(long snifferId);

}
