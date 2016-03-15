package com.logsniffer.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a token associated with current surfing user.
 * 
 * @author mbok
 *
 */
public interface UserTokenProvider {
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
