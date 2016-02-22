/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.model.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.model.Log;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.LogRawAccessor;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class DailyRollingLogAccess implements ByteLogAccess {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final static InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);

	private final DailyRollingLog log;
	private final Log[] parts;

	private final String[] allLogPathes;

	private final LogRawAccessor<ByteLogAccess, Log> rawAccessor;

	@SuppressWarnings("unchecked")
	public DailyRollingLogAccess(final LogRawAccessor<? extends ByteLogAccess, ? extends Log> rawAccessor,
			final DailyRollingLog log) {
		super();
		this.rawAccessor = (LogRawAccessor<ByteLogAccess, Log>) rawAccessor;
		this.log = log;
		this.parts = log.getParts();
		this.allLogPathes = new String[this.parts.length];
		int i = 0;
		for (final Log part : parts) {
			this.allLogPathes[i++] = part.getPath();
		}
	}

	/**
	 * Rolling log pointer combining the current path and the inside offset as a
	 * pointer.
	 * 
	 * @author mbok
	 * 
	 */
	private static class RollingLogPointer implements LogPointer {
		private final String path;
		private String liveNext;
		private LogPointer filePointer;
		private final boolean live;
		private final boolean first;
		private String json;
		private int allLogsHash;

		public RollingLogPointer(final String path, final String[] allLogPathes, final LogPointer filePointer,
				final boolean firstFile, final boolean liveFile) {
			super();
			this.path = path;
			this.liveNext = liveFile && allLogPathes.length > 1 ? allLogPathes[1] : null;
			this.allLogsHash = Arrays.hashCode(allLogPathes);
			this.filePointer = filePointer;
			this.live = liveFile;
			this.first = firstFile;
		}

		@Override
		public boolean isEOF() {
			return live && filePointer.isEOF();
		}

		@Override
		public boolean isSOF() {
			return first && filePointer.isSOF();
		}

		@Override
		public String toString() {
			return "RollingLogPointer [path=" + path + ", filePointer=" + filePointer + "]";
		}

		@Override
		public String getJson() {
			if (json == null) {
				final StringBuilder b = new StringBuilder("{\"p\":" + JSONUtils.quote(path) + ",\"l\":" + live
						+ ",\"f\":" + first + ",\"h\":" + allLogsHash + ",\"u\":" + filePointer.getJson());
				if (liveNext != null) {
					b.append(",\"n\":" + JSONUtils.quote(liveNext));
				}
				b.append("}");
				json = b.toString();
			}
			return json;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + allLogsHash;
			result = prime * result + (filePointer == null ? 0 : filePointer.hashCode());
			result = prime * result + (first ? 1231 : 1237);
			result = prime * result + (live ? 1231 : 1237);
			result = prime * result + (liveNext == null ? 0 : liveNext.hashCode());
			result = prime * result + (path == null ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof RollingLogPointer)) {
				return false;
			}
			final RollingLogPointer other = (RollingLogPointer) obj;
			if (allLogsHash != other.allLogsHash) {
				return false;
			}
			if (filePointer == null) {
				if (other.filePointer != null) {
					return false;
				}
			} else if (!filePointer.equals(other.filePointer)) {
				return false;
			}
			if (first != other.first) {
				return false;
			}
			if (live != other.live) {
				return false;
			}
			if (liveNext == null) {
				if (other.liveNext != null) {
					return false;
				}
			} else if (!liveNext.equals(other.liveNext)) {
				return false;
			}
			if (path == null) {
				if (other.path != null) {
					return false;
				}
			} else if (!path.equals(other.path)) {
				return false;
			}
			return true;
		}

	}

	private class RollingInputStream extends ByteLogInputStream {
		private int currentLogIndex;
		private SequenceInputStream seqStream;
		private ByteLogInputStream currentLogStream;
		private RollingLogPointer from;
		private boolean sequenceStreamClosed = false;

		private RollingInputStream(final RollingLogPointer _from) throws IOException {
			final PointerData fpd = getLogIndex(_from);
			this.currentLogIndex = fpd.index;
			this.from = fpd.pointer;
			this.seqStream = new SequenceInputStream(new Enumeration<InputStream>() {
				private boolean rolled = false;

				@Override
				public boolean hasMoreElements() {
					return !sequenceStreamClosed && (!rolled || currentLogIndex > 0);
				}

				@Override
				public InputStream nextElement() {
					try {
						if (rolled) {
							currentLogIndex--;
						}
						if (currentLogIndex < 0) {
							// Empty
							return EMPTY_STREAM;
						}
						logger.debug("Openning input stream from log #{}:{} at {}", currentLogIndex,
								parts[currentLogIndex], from != null ? from.filePointer : null);
						final LogRawAccess<ByteLogInputStream> newLogRawAccess = getPartLogAccess(
								parts[currentLogIndex]);
						if (newLogRawAccess == null) {
							logger.warn("Failed to open access to log #{}:{} at {}", currentLogIndex,
									parts[currentLogIndex], from != null ? from.filePointer : null);
							return nextElement();
						}
						currentLogStream = newLogRawAccess
								.getInputStream(rolled ? null : from != null ? from.filePointer : null);
						return currentLogStream;
					} catch (final IOException e) {
						throw new RuntimeException(
								"Failed to open input stream for log: " + parts[currentLogIndex].getPath(), e);
					} finally {
						rolled = true;
					}
				}
			}) {
				@Override
				public void close() throws IOException {
					sequenceStreamClosed = true;
					super.close();
				}

			};
		}

		@Override
		public LogPointer getPointer() throws IOException {
			if (currentLogStream != null) {
				// Already read something
				return new RollingLogPointer(parts[currentLogIndex].getPath(), allLogPathes,
						currentLogStream.getPointer(), currentLogIndex == parts.length - 1, currentLogIndex == 0);
			} else if (from != null) {
				// Nothing read up to now
				return from;
			} else {
				// No start given
				return createRelative(null, 0);
			}
		}

		@Override
		public int read() throws IOException {
			return seqStream.read();
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			return seqStream.read(b, off, len);
		}

		@Override
		public int read(final byte[] b) throws IOException {
			return seqStream.read(b);
		}

		@Override
		public void close() throws IOException {
			seqStream.close();
		}

	}

	private int getLogIndex(final String path) {
		int i = 0;
		for (final Log log : parts) {
			if (log.getPath().equals(path)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Used to reference the proper log in {@link DailyRollingLog#parts} and to
	 * reallocate a possible no more listed pointer.
	 * 
	 * @author mbok
	 * 
	 */
	private static class PointerData {
		private final int index;
		private final RollingLogPointer pointer;

		public PointerData(final int index, final RollingLogPointer pointer) {
			super();
			this.index = index;
			this.pointer = pointer;
		}

	}

	private PointerData getLogIndex(final LogPointer _source) throws IOException {
		final RollingLogPointer source = (RollingLogPointer) _source;
		if (source == null) {
			return new PointerData(parts.length - 1, null);
		}
		// Search for the proper log
		final int index = getLogIndex(source.path);
		if (index < 0) {
			logger.debug("Using start pointer for log '{}' due to '{}' is no more listed", log.getPath(), source.path);
			return new PointerData(parts.length - 1, null);
		} else {
			if (Arrays.hashCode(allLogPathes) == source.allLogsHash) {
				// Log list is still the same as referenced by pointer
				return new PointerData(index, source);
			} else {
				if (source.live) {
					// We were on the live log which is now rolled, lets check
					// for previous
					if (source.liveNext != null) {
						final int next = getLogIndex(source.liveNext) - 1;
						if (next > 0 && next < parts.length) {
							logger.debug("Using for rolled live log '{}' the next listed '{}'", log.getPath(),
									parts[next].getPath());
							return new PointerData(next, source);
						} else {
							logger.debug("Using start pointer for log '{}', because the next rolled wasn't found",
									log.getPath());
							return new PointerData(parts.length - 1, null);
						}
					}
					logger.debug("Using start pointer for log '{}', because the live one was rolled", log.getPath());
					return new PointerData(parts.length - 1, null);

				} else {
					return new PointerData(index, source);
				}
			}
		}
	}

	@Override
	public long getDifference(final LogPointer _source, final LogPointer _target) throws IOException {
		final PointerData spd = getLogIndex(_source);
		final PointerData tpd = getLogIndex(_target);
		RollingLogPointer source = spd.pointer;
		RollingLogPointer target = tpd.pointer;
		int start = spd.index;
		int end = tpd.index;
		int dir = 1;
		long diff = 0;
		if (start < end) {
			// Change sign and switch bounds
			dir = -1;
			final int tmp = start;
			start = end;
			end = tmp;
			final RollingLogPointer tmpp = source;
			source = target;
			target = tmpp;
		}
		if (start > end) {
			// Unread from source
			diff += parts[start].getSize()
					- (source != null ? getPartLogAccess(parts[start]).getDifference(null, source.filePointer) : 0);
			// Already read from target
			diff += target != null ? getPartLogAccess(parts[end]).getDifference(null, target.filePointer) : 0;
		} else if (start == end) {
			diff += getPartLogAccess(parts[start]).getDifference(source != null ? source.filePointer : null,
					target.filePointer);
		}
		for (int i = start - 1; i > end; i--) {
			diff += parts[i].getSize();
		}
		return diff * dir;
	}

	@Override
	public LogPointer createRelative(final LogPointer _source, long relativeBytePosition) throws IOException {
		final PointerData spd = getLogIndex(_source);
		RollingLogPointer source = spd.pointer;
		int start = spd.index;
		if (relativeBytePosition > 0) {
			long mv = 0;
			long localPos = source != null ? getPartLogAccess(parts[start]).getDifference(null, source.filePointer) : 0;
			int i = 0;
			while (relativeBytePosition > 0 && start >= 0) {
				if (i++ == 1) {
					source = null;
					localPos = 0;
				}
				mv = Math.min(relativeBytePosition, parts[start].getSize() - localPos);
				relativeBytePosition -= mv;
				start--;
			}
			start++;
			if (start > 0 && localPos + mv >= parts[start].getSize()) {
				start--;
				source = null;
				mv = 0;
			}
			return new RollingLogPointer(parts[start].getPath(), allLogPathes,
					getPartLogAccess(parts[start]).createRelative(source != null ? source.filePointer : null, mv),
					start == parts.length - 1, start == 0);
		} else if (relativeBytePosition < 0) {
			long mv = 0;
			long localPos = source != null ? getPartLogAccess(parts[start]).getDifference(null, source.filePointer) : 0;
			while (relativeBytePosition < 0 && start < parts.length) {
				if (source == null) {
					localPos = parts[start].getSize();
				}
				mv = Math.min(-relativeBytePosition, localPos);
				relativeBytePosition += mv;
				source = null;
				start++;
			}
			start--;
			return new RollingLogPointer(parts[start].getPath(), allLogPathes,
					getPartLogAccess(parts[start]).createRelative(null, localPos - mv), start == parts.length - 1,
					start == 0);
		} else {
			return new RollingLogPointer(parts[start].getPath(), allLogPathes,
					getPartLogAccess(parts[start]).createRelative(source != null ? source.filePointer : null, 0),
					start == parts.length - 1, start == 0);
		}
	}

	@Override
	public LogPointer getFromJSON(final String data) throws IOException {
		try {
			final JSONObject json = JSONObject.fromObject(data);
			if (json.size() > 0) {
				final RollingLogPointer rlp = new RollingLogPointer(json.getString("p"), new String[] {},
						new DefaultPointer(0, 0), json.getBoolean("f"), json.getBoolean("l"));
				rlp.allLogsHash = json.getInt("h");
				rlp.liveNext = json.optString("n", null);
				final PointerData spd = getLogIndex(rlp);
				if (spd.pointer != null) {
					rlp.filePointer = getPartLogAccess(parts[spd.index])
							.getFromJSON(json.getJSONObject("u").toString());
				}
				return createRelative(rlp, 0);
			} else {
				return createRelative(null, 0);
			}
		} catch (final JSONException e) {
			logger.warn("Invalid JSON pointer: " + data, e);
			return createRelative(null, 0);
		}
	}

	private ByteLogAccess getPartLogAccess(final Log part) throws IOException {
		return rawAccessor.getLogAccess(part);
	}

	@Override
	public ByteLogInputStream getInputStream(final LogPointer from) throws IOException {
		return new RollingInputStream((RollingLogPointer) from);
	}

	@Override
	public LogPointer end() throws IOException {
		return createRelative(null, this.log.getSize());
	}

	@Override
	public LogPointer start() throws IOException {
		return createRelative(null, 0);
	}

	@Override
	public NavigationFuture refresh(final LogPointer toRefresh) throws IOException {
		return new NavigationFuture() {
			@Override
			public LogPointer get() throws IOException {
				return createRelative(toRefresh, 0);
			}
		};
	}

	@Override
	public NavigationFuture absolute(final Long offset) {
		return new NavigationFuture() {
			@Override
			public LogPointer get() throws IOException {
				return createRelative(null, offset);
			}
		};
	}
}
