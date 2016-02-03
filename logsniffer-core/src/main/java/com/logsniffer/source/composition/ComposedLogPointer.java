package com.logsniffer.source.composition;

import java.util.Date;

import com.logsniffer.model.LogPointer;

public class ComposedLogPointer implements LogPointer {
	public static class PointerPart {
		private final long logSourceId;
		private final String logPath;
		private final LogPointer offset;

		public PointerPart(final long logSourceId, final String logPath, final LogPointer offset) {
			super();
			this.logSourceId = logSourceId;
			this.logPath = logPath;
			this.offset = offset;
		}

		/**
		 * @return the logSourceId
		 */
		public long getLogSourceId() {
			return logSourceId;
		}

		/**
		 * @return the logPath
		 */
		public String getLogPath() {
			return logPath;
		}

		/**
		 * @return the offset
		 */
		public LogPointer getOffset() {
			return offset;
		}

	}

	private final PointerPart[] parts;
	private final int currentPartIndex;
	private final Date currentTimestamp;

	public ComposedLogPointer(final PointerPart[] parts, final int currentPartIndex, final Date currentTimestamp) {
		super();
		this.parts = parts;
		this.currentPartIndex = currentPartIndex;
		this.currentTimestamp = currentTimestamp;
	}

	/**
	 * @return the parts
	 */
	public PointerPart[] getParts() {
		return parts;
	}

	/**
	 * @return the currentPartIndex
	 */
	public int getCurrentPartIndex() {
		return currentPartIndex;
	}

	/**
	 * @return the currentTimestamp
	 */
	public Date getCurrentTimestamp() {
		return currentTimestamp;
	}

	@Override
	public boolean isSOF() {
		for (final PointerPart p : parts) {
			if (p.offset != null && !p.offset.isSOF()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEOF() {
		for (final PointerPart p : parts) {
			if (p.offset == null || !p.offset.isEOF()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getJson() {
		// TODO Auto-generated method stub
		return null;
	}
}
