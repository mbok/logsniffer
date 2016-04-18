package com.logsniffer.source.compound;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.model.LogPointer;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class CompoundLogPointer implements LogPointer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompoundLogPointer.class);

	public static interface LogInstanceResolver {
		LogInstance resolveForPathHash(long sourceId, int pathHash);
	}

	/**
	 * Pointer related to a single log.
	 * 
	 * @author mbok
	 *
	 */
	public static class PointerPart {
		private final long logSourceId;
		private final int logPathHash;
		private final LogPointer offset;

		public PointerPart(final long logSourceId, final String logPath, final LogPointer offset) {
			super();
			this.logSourceId = logSourceId;
			this.logPathHash = logPath.hashCode();
			this.offset = offset;
		}

		public PointerPart(final long logSourceId, final int logPathHash, final LogPointer offset) {
			super();
			this.logSourceId = logSourceId;
			this.logPathHash = logPathHash;
			this.offset = offset;
		}

		/**
		 * @return the logSourceId
		 */
		public long getLogSourceId() {
			return logSourceId;
		}

		/**
		 * @return the logPathHash
		 */
		public int getLogPathHash() {
			return logPathHash;
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
			result = prime * result + logPathHash;
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
			if (logPathHash != other.logPathHash)
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

		@Override
		public String toString() {
			return "[logSourceId=" + logSourceId + ", logPathHash=" + logPathHash + ", offset=" + offset + "]";
		}

		private void appendJson(final StringBuilder b) {
			b.append("{\"s\":");
			b.append(logSourceId);
			b.append(",\"p\":");
			b.append(logPathHash);
			b.append(",\"o\":");
			b.append(offset != null ? offset.getJson() : "null");
			b.append("}");
		}

		public static PointerPart fromJson(final JSONObject jsonObject, final LogInstanceResolver lir)
				throws IOException {
			if (jsonObject.has("s") && jsonObject.has("p") && jsonObject.has("o")) {
				final long ls = jsonObject.getLong("s");
				final int ph = jsonObject.getInt("p");
				LogPointer offset = null;
				final LogInstance li = lir.resolveForPathHash(ls, ph);
				if (li != null) {
					offset = li.getLogAccess().getFromJSON(jsonObject.getString("o"));
				}
				return new PointerPart(ls, ph, offset);
			}
			return null;
		}

	}

	private final PointerPart[] parts;
	private final Date currentTimestamp;

	public CompoundLogPointer(final PointerPart[] parts, final Date currentTimestamp) {
		super();
		this.parts = parts;
		this.currentTimestamp = currentTimestamp;
	}

	/**
	 * Returns index of the correlating pointer part for given log path or -1 if
	 * no part matches the path. Part values could be null.
	 * 
	 * @param path
	 * @return index of the correlating pointer part for given log path or -1 if
	 *         no part matches the path.
	 */
	public static int getPartIndex(final PointerPart[] parts, final String path) {
		final int hash = path.hashCode();
		int i = 0;
		for (final PointerPart part : parts) {
			if (part != null && part.logPathHash == hash) {
				return i;
			}
			i++;
		}
		return -1;
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
			if (p != null && p.offset != null && !p.offset.isSOF()) {
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

	static CompoundLogPointer fromJson(final String jsonStr, final LogInstanceResolver lir) throws IOException {
		try {
			final JSONObject json = JSONObject.fromObject(jsonStr);
			Date tmst = null;
			PointerPart[] parts = null;
			if (json.has("d")) {
				tmst = new Date(json.getLong("d"));
			} else {
				tmst = new Date(0);
			}
			if (json.has("p")) {
				final JSONArray jp = json.getJSONArray("p");
				parts = new PointerPart[jp.size()];
				for (int i = 0; i < parts.length; i++) {
					parts[i] = PointerPart.fromJson(jp.getJSONObject(i), lir);
				}
			} else {
				parts = new PointerPart[0];
			}
			final CompoundLogPointer cp = new CompoundLogPointer(parts, tmst);
			LOGGER.debug("Transfered JSON '{}' into pointer: {}", jsonStr, cp);
			return cp;
		} catch (final JSONException e) {
			LOGGER.warn("Failed to read pointer from invalid JSON: " + jsonStr, e);
			return new CompoundLogPointer(new PointerPart[0], new Date(0));
		}
	}

	@Override
	public String getJson() {
		final StringBuilder b = new StringBuilder("{\"d\":");
		b.append(currentTimestamp != null ? currentTimestamp.getTime() : 0);
		b.append(",\"p\":[");
		int i = 0;
		for (final PointerPart p : parts) {
			if (p != null) {
				if (i++ > 0) {
					b.append(",");
				}
				p.appendJson(b);
			}
		}
		b.append("]}");
		return b.toString();
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
		final CompoundLogPointer other = (CompoundLogPointer) obj;
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
