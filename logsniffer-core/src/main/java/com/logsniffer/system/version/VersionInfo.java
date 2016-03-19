package com.logsniffer.system.version;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bean for version info. The version {@link #getName()} is interpreted as a
 * version split in parts delimited by a dot ".". A version instance is
 * comparable to another according the extracted version parts.
 * 
 * @author mbok
 *
 */
public class VersionInfo implements Comparable<VersionInfo> {
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class);
	private String name;
	private String info;
	@JsonIgnore
	private int[] versionParts;

	private boolean features;
	private boolean bugfixes;
	private boolean security;

	public VersionInfo() {
		super();
	}

	public VersionInfo(String name) {
		super();
		setName(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (name == null) {
			versionParts = new int[0];
		} else {
			List<Integer> vps = new ArrayList<>();
			for (String v : name.split("\\.")) {
				try {
					vps.add(((Number) NumberFormat.getInstance().parse(v)).intValue());
				} catch (ParseException e) {
					LOGGER.warn("Failed to parse version part '" + v + "' in '" + name + "', it'll be ignored", e);
				}
			}
			versionParts = new int[vps.size()];
			for (int i = 0; i < versionParts.length; i++) {
				versionParts[i] = vps.get(i);
			}
		}
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the features
	 */
	public boolean isFeatures() {
		return features;
	}

	/**
	 * @param features
	 *            the features to set
	 */
	public void setFeatures(boolean features) {
		this.features = features;
	}

	/**
	 * @return the bugfixes
	 */
	public boolean isBugfixes() {
		return bugfixes;
	}

	/**
	 * @param bugfixes
	 *            the bugfixes to set
	 */
	public void setBugfixes(boolean bugfixes) {
		this.bugfixes = bugfixes;
	}

	/**
	 * @return the security
	 */
	public boolean isSecurity() {
		return security;
	}

	/**
	 * @param security
	 *            the security to set
	 */
	public void setSecurity(boolean security) {
		this.security = security;
	}

	/**
	 * @return the versionParts
	 */
	public int[] getVersionParts() {
		return versionParts;
	}

	@Override
	public int compareTo(VersionInfo o) {
		for (int i = 0; i < Math.max(versionParts.length, o.versionParts.length); i++) {
			int v1 = i < versionParts.length ? versionParts[i] : 0;
			int v2 = i < o.versionParts.length ? o.versionParts[i] : 0;
			if (v1 != v2) {
				return v1 - v2;
			}
		}
		return 0;
	}

}
