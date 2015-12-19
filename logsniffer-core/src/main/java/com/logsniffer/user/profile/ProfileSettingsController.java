package com.logsniffer.user.profile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.logsniffer.fields.FieldsMap;

/**
 * Exposes profile settings feature via REST.
 * 
 * @author mbok
 *
 */
@RestController
public class ProfileSettingsController {
	private static final String RESOURCE_PATH = "/user/profile/settings";
	@Autowired
	private ProfileSettingsTokenProvider tokenProvider;

	@Autowired
	private ProfileSettingsStorage storage;

	@RequestMapping(value = "/user/profile/settings/**", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void storeSettings(@RequestBody final FieldsMap data, final HttpServletRequest request,
			final HttpServletResponse response) {
		final String token = tokenProvider.getToken(request, response);
		storage.storeSettings(token, getSettingsPath(request), data);
	}

	@RequestMapping(value = "/user/profile/settings/**", method = RequestMethod.GET)
	@ResponseBody
	public FieldsMap getSettings(final HttpServletRequest request, final HttpServletResponse response) {
		final String token = tokenProvider.getToken(request, response);
		return storage.getSettings(token, getSettingsPath(request), false);
	}

	private String getSettingsPath(final HttpServletRequest request) {
		final String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return fullPath.substring(RESOURCE_PATH.length());
	}
}
