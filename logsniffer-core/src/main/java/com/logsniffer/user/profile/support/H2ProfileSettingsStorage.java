package com.logsniffer.user.profile.support;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.fields.FieldsMap;
import com.logsniffer.user.profile.ProfileSettingsStorage;
import com.logsniffer.util.DataAccessException;

/**
 * H2 implementation for {@link ProfileSettingsStorage}. Recursive retrieval
 * isn't supported so far.
 * 
 * @author mbok
 *
 */
@Component
public class H2ProfileSettingsStorage implements ProfileSettingsStorage {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void storeSettings(final String token, final String settingsPath, final FieldsMap data) {
		try {
			if (getSettings(token, settingsPath, false) == null) {
				logger.info("Inserting profile settings for path: {}", settingsPath);
				jdbcTemplate.update("INSERT INTO USER_PROFILE_SETTINGS SET TOKEN=?, PATH=?, DATA=?", token,
						settingsPath, objectMapper.writeValueAsString(data));
			} else {
				logger.info("Updating existing profile settings for path: {}", settingsPath);
				jdbcTemplate.update("UPDATE USER_PROFILE_SETTINGS SET DATA=? WHERE TOKEN=? AND PATH=?",
						objectMapper.writeValueAsString(data), token, settingsPath);
			}
		} catch (final JsonProcessingException e) {
			throw new DataAccessException(
					"Failed to store settings for token=" + token + " and path '" + settingsPath + "'", e);
		}
	}

	@Override
	public void deleteSettings(final String token, final String path, final boolean recursive) {
		logger.info("Deleting profile settings recursively={} for path: {}", recursive, path);
		if (recursive) {
			jdbcTemplate.update("DELETE FROM USER_PROFILE_SETTINGS WHERE TOKEN=? AND PATH LIKE ? || '%' ", token, path);
		} else {
			jdbcTemplate.update("DELETE FROM USER_PROFILE_SETTINGS WHERE TOKEN=? AND PATH=?", token, path);
		}
	}

	@Override
	public FieldsMap getSettings(final String token, final String path, final boolean recursive) {
		if (recursive) {
			throw new NotImplementedException();
		}
		final List<String> list = jdbcTemplate.queryForList(
				"SELECT DATA FROM USER_PROFILE_SETTINGS WHERE TOKEN=? AND PATH=?", String.class, token, path);
		try {
			if (list.isEmpty()) {
				logger.debug("No profile settings found for token={} and path={}", token, path);
				return null;
			} else {
				logger.debug("Loaded profile settings for token={} and path={}", token, path);
				return objectMapper.readValue(list.get(0), FieldsMap.class);
			}
		} catch (final IOException e) {
			logger.error("Failed to deserialize profile setting: " + list.get(0), e);
			return null;
		}
	}

}
