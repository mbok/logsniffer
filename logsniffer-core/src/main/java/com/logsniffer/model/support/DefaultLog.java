package com.logsniffer.model.support;

import com.logsniffer.model.Log;

/**
 * Default log bean.
 * 
 * @author mbok
 *
 */
public class DefaultLog implements Log {
	private final String name;
	private final String path;
	private final long lastModified;
	private final SizeMetric sizeMetric;
	private final long size;

	public DefaultLog(final String name, final String path, final long lastModified, final SizeMetric sizeMetric,
			final long size) {
		super();
		this.name = name;
		this.path = path;
		this.lastModified = lastModified;
		this.sizeMetric = sizeMetric;
		this.size = size;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the path
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @return the lastModified
	 */
	@Override
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @return the sizeMetric
	 */
	@Override
	public SizeMetric getSizeMetric() {
		return sizeMetric;
	}

	/**
	 * @return the size
	 */
	@Override
	public long getSize() {
		return size;
	}

}
