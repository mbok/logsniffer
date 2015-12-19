package com.logsniffer.user.profile.support;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import com.logsniffer.user.profile.ProfileSettingsTokenProvider;

/**
 * Manages generated token using browser cookies.
 * 
 * @author mbok
 *
 */
@Component
public class CookieTokenProvider implements ProfileSettingsTokenProvider {
	private final static String COOKIE_KEY = "profileKey";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getToken(final HttpServletRequest request, final HttpServletResponse response) {
		final Cookie tokenCookie = WebUtils.getCookie(request, COOKIE_KEY);
		if (tokenCookie != null && tokenCookie.getValue() != null) {
			logger.debug("Detected profile token from cookie: {}", tokenCookie.getValue());
			return tokenCookie.getValue();
		}
		final String token = UUID.randomUUID().toString();
		final CookieGenerator g = new CookieGenerator();
		g.setCookieMaxAge(Integer.MAX_VALUE);
		g.setCookiePath("/");
		g.setCookieName(COOKIE_KEY);
		g.addCookie(response, token);
		logger.debug("Generated a new token: {}", token);
		return token;
	}

}
