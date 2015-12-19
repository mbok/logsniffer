package com.logsniffer.user.profile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a profile storage token assigned to the surfing user.
 * 
 * @author mbok
 *
 */
public interface ProfileSettingsTokenProvider {
	/**
	 * Returns a token associated with the user currently applying the request.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            response
	 * @return null-safe token associated with current user
	 */
	public String getToken(HttpServletRequest request, HttpServletResponse response);
}
