package com.logsniffer.user.profile;

import com.logsniffer.fields.FieldsMap;
import com.logsniffer.user.UserTokenProvider;

/**
 * Stores arbitrary profile data associated with a user using logsniffer. Data
 * is organized in a hierarchical node structure, where each node is identified
 * by a path (/ as separator). Node data is always a JSON (FieldsMap) structure.
 * It should be possible to retrieve nested JSON objects from a located node and
 * its sub nodes.
 * 
 * @author mbok
 *
 */
public interface ProfileSettingsStorage {
	/**
	 * Stores data under a node located by the path for the given user token.
	 * 
	 * @param token
	 *            the user token, see {@link UserTokenProvider}
	 * @param settingsPath
	 *            the node path starting at root with a leading "/"
	 * @param data
	 *            not null data to store
	 */
	void storeSettings(String token, String settingsPath, FieldsMap data);

	/**
	 * Deletes settings located at given node for the user token.
	 * 
	 * @param token
	 *            the user token, see {@link UserTokenProvider}
	 * @param path
	 *            the node path starting at root with a leading "/"
	 * @param recursive
	 *            if true, also all child nodes will be deleted, otherwise only
	 *            the data on given node is deleted.
	 */
	void deleteSettings(String token, String path, boolean recursive);

	/**
	 * Returns stored data from given path.
	 * 
	 * @param token
	 *            the user token, see {@link UserTokenProvider}
	 * @param path
	 *            the node path starting at root with a leading "/"
	 * @param recursive
	 *            if false, only the data from current node is retrieved or null
	 *            if nothing is stored. If true, the data of all child node is
	 *            nested into the JSON response.
	 * @return stored data from given path
	 */
	FieldsMap getSettings(String token, String path, boolean recursive);
}
