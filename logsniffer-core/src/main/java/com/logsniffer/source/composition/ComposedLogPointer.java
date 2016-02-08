package com.logsniffer.source.composition;

import java.util.Arrays;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((logPath == null) ? 0 : logPath.hashCode());
			result = prime * result + (int) (logSourceId ^ (logSourceId >>> 32));
			result = prime * result + ((offset == null) ? 0 : offset.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PointerPart other = (PointerPart) obj;
			if (logPath == null) {
				if (other.logPath != null)
					return false;
			} else if (!logPath.equals(other.logPath))
				return false;
			if (logSourceId != other.logSourceId)
				return false;
			if (offset == null) {
				if (other.offset != null)
					return false;
			} else if (!offset.equals(other.offset))
				return false;
			return true;
		}

	}

	private final PointerPart[] parts;
	private final Date currentTimestamp;

	public ComposedLogPointer(final PointerPart[] parts, final Date currentTimestamp) {
		super();
		this.parts = parts;
		this.currentTimestamp = currentTimestamp;
	}

	/**
	 * @return the parts
	 */
	public PointerPart[] getParts() {
		return parts;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parts);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ComposedLogPointer other = (ComposedLogPointer) obj;
		if (!Arrays.equals(parts, other.parts))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComposedLogPointer [currentTimestamp=" + (currentTimestamp != null ? currentTimestamp.getTime() : null)
				+ ", parts=" + Arrays.toString(parts) + "]";
	}

}
