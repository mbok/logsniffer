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
package com.logsniffer.reader.support;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.support.ByteLogAccess;
import com.logsniffer.model.support.LineInputStream;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.Configured;

/**
 * Abstract line text reader based on pattern matching.
 * 
 * @author mbok
 * 
 */
public abstract class AbstractPatternLineReader<MatcherContext> implements LogEntryReader<ByteLogAccess> {
	private static final int STRING_BUILDER_CAPACITY = 4096;

	public static final String PROP_LOGSNIFFER_READER_MAX_MULTIPLE_LINES = "logsniffer.reader.pattern.maxUnformattedLines";

	public static final int DEFAULT_MAX_UNFORMATTED_LINES = 500;

	private static final Logger logger = LoggerFactory.getLogger(AbstractPatternLineReader.class);

	@Configured(value = PROP_LOGSNIFFER_READER_MAX_MULTIPLE_LINES, defaultValue = DEFAULT_MAX_UNFORMATTED_LINES + "")
	private ConfigValue<Integer> maxUnformattedLinesConfigValue;

	private int maxUnfomattedLines = DEFAULT_MAX_UNFORMATTED_LINES;

	private boolean initialized = false;

	@JsonProperty
	@NotEmpty
	private String charset = "UTF-8";

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public void setCharset(final String charset) {
		this.charset = charset;
	}

	/**
	 * Initializes a pattern before reading.
	 * 
	 * @throws ParseException
	 *             in case pattern initialization errors
	 */
	protected void init() throws FormatException {
		if (!initialized) {
			if (maxUnformattedLinesConfigValue != null) {
				maxUnfomattedLines = maxUnformattedLinesConfigValue.get();
			}
			logger.debug("Init {} with max multiple lines without matching pattern: {}", getClass(),
					maxUnfomattedLines);
			initialized = true;
		}
	}

	public interface ReadingContext<MatcherContext> {
		/**
		 * @return a matcher context in case of a matching line or null if
		 *         doesn't.
		 */
		MatcherContext matches(String line) throws FormatException;

		/**
		 * Fills the attributes from the matcher context.
		 * 
		 * @param entry
		 *            entry to fill attributes to
		 * @param ctx
		 *            the matcher context
		 */
		void fillAttributes(LogEntry entry, MatcherContext ctx) throws FormatException;
	}

	protected abstract ReadingContext<MatcherContext> getReadingContext() throws FormatException;

	/**
	 * Called in case of a line not matching the format pattern and which is
	 * associated with an overflow of the previous entry. Implement this method
	 * to attach the overflow line to a field.
	 * 
	 * @param entry
	 *            the previous entry
	 * @param overflowLine
	 *            the overflow line
	 */
	protected abstract void attachOverflowLine(LogEntry entry, String overflowLine);

	/**
	 * 
	 * @return pattern info for logging issues
	 */
	protected abstract String getPatternInfo();

	private abstract class ParallelReadingExecutor {
		private final LineInputStream inputStream;
		private final Semaphore threadSemaphore;
		private boolean finished;
		private final LinkedBlockingQueue<ParallelReadingContext> lineBuffer;
		private final Object processingSemaphore = new Object();
		private final Object readingSemaphore = new Object();
		private final Object threadDestinctorSemaphore = new Object();
		private Exception terminationException;
		private final int parallelCount;
		private PatternMatcherThread processingThread;

		public ParallelReadingExecutor(final LineInputStream lis, final int parallelCount) {
			inputStream = lis;
			this.parallelCount = parallelCount;
			threadSemaphore = new Semaphore(parallelCount);
			this.lineBuffer = new LinkedBlockingQueue<>(parallelCount * 10);
		}

		protected void processParsingResults(final PatternMatcherThread thread) {
			synchronized (threadDestinctorSemaphore) {
				if (this.processingThread != null) {
					return;
				}
				this.processingThread = thread;
			}
			synchronized (processingSemaphore) {
				final PatternMatcherThread processingThreadTemp = processingThread;
				ParallelReadingContext peek = null;
				try {
					while (!finished) {
						synchronized (threadDestinctorSemaphore) {
							if ((peek = lineBuffer.peek()) == null || !peek.finished) {
								this.processingThread = null;
								return;
							}
						}
						lineBuffer.poll();
						if (peek.exception != null) {
							terminationException = peek.exception;
							finished = true;
							break;
						}
						try {
							if (!processParsingResult(peek.line, peek.offset, thread.readingContext,
									peek.matcherResult)) {
								finished = true;
							}
						} catch (final Exception e) {
							finished = true;
							terminationException = e;
							break;
						}
					}
				} finally {
					synchronized (threadDestinctorSemaphore) {
						if (finished) {
							lineBuffer.clear();
						}
						if (processingThreadTemp == processingThread) {
							this.processingThread = null;
						}
					}
				}
			}

		}

		protected abstract boolean processParsingResult(String line, LogPointer offset,
				ReadingContext<MatcherContext> readingContext, MatcherContext mCtx) throws IOException;

		private ParallelReadingContext readNextLine() {
			synchronized (readingSemaphore) {
				try {
					final String line = inputStream.readNextLine();
					final LogPointer pointer = inputStream.getPointer();
					if (line != null && pointer != null) {
						final ParallelReadingContext pline = new ParallelReadingContext(line, pointer);
						lineBuffer.put(pline);
						return pline;
					}
				} catch (final IOException | InterruptedException e) {
					return new ParallelReadingContext(e);
				}
				return null;
			}
		}

		public void executeParallel() throws IOException {
			synchronized (processingSemaphore) {
				for (int i = 0; i < parallelCount; i++) {
					try {
						new PatternMatcherThread(this, getReadingContext()).start();
					} catch (final InterruptedException e) {
						finished = true;
						terminationException = e;
					}
				}
			}
			try {
				threadSemaphore.acquire(parallelCount);
			} catch (final InterruptedException e) {
				synchronized (processingSemaphore) {
					logger.error("Failed to wait for parallel threads", e);
					finished = true;
					if (terminationException == null) {
						terminationException = e;
					}
				}
			} finally {
				synchronized (processingSemaphore) {
					// Unblock possibly running threads
					lineBuffer.clear();
				}
			}
			// Probably not all processed lines
			processParsingResults(null);
			if (terminationException != null) {
				if (terminationException instanceof IOException) {
					throw (IOException) terminationException;
				} else if (terminationException instanceof RuntimeException) {
					throw (RuntimeException) terminationException;
				} else {
					throw new RuntimeException("Parallel reading aborted with an exception", terminationException);
				}
			}
		}
	}

	private class ParallelReadingContext {
		private String line;
		private LogPointer offset;
		private MatcherContext matcherResult;
		private boolean finished;
		private Exception exception;

		public ParallelReadingContext(final String line, final LogPointer offset) {
			super();
			this.line = line;
			this.offset = offset;
		}

		public ParallelReadingContext(final Exception e) {
			this.exception = e;
		}

	}

	private class PatternMatcherThread extends Thread {
		private final ParallelReadingExecutor executor;
		private long count = 0;
		final ReadingContext<MatcherContext> readingContext;

		public PatternMatcherThread(final AbstractPatternLineReader<MatcherContext>.ParallelReadingExecutor executor,
				final ReadingContext<MatcherContext> readingContext) throws InterruptedException {
			super(PatternMatcherThread.class.getSimpleName() + "_" + executor.threadSemaphore.availablePermits() + "_"
					+ executor.parallelCount);
			this.executor = executor;
			this.readingContext = readingContext;
			executor.threadSemaphore.acquire(1);
		}

		@Override
		public void run() {
			try {
				ParallelReadingContext lineCtx = null;
				while (!executor.finished && (lineCtx = executor.readNextLine()) != null) {
					count++;
					try {
						lineCtx.matcherResult = readingContext.matches(lineCtx.line);
					} catch (final FormatException e) {
						lineCtx.exception = e;
					} finally {
						lineCtx.finished = true;
						executor.processParsingResults(this);
					}
				}
			} finally {
				logger.debug("Thread {} processed {} lines", this, count);
				// Free another threads if they are blocked
				executor.processParsingResults(null);
				executor.threadSemaphore.release(1);
			}
		}

	}

	private static class SequentialContext {
		int linesWithoutPattern = 0;
		LogEntry entry = null;
		StringBuilder text = new StringBuilder(STRING_BUILDER_CAPACITY);
		LogPointer lastOffset;
		private boolean consumptionCancelled = false;

	}

	@Override
	public final void readEntries(final Log log, final ByteLogAccess logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException {
		init();
		LineInputStream lis = null;
		try {
			final SequentialContext sCtx = new SequentialContext();

			lis = new LineInputStream(logAccess, logAccess.getInputStream(startOffset), getCharset());
			sCtx.lastOffset = lis.getPointer();
			if (sCtx.lastOffset == null) {
				sCtx.lastOffset = logAccess.start();
			}

			final int coreSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
			logger.debug("Start reading log '{}' accoridng the pattern '{}' in parallel with {} threads", log.getPath(),
					getPatternInfo(), coreSize);
			new ParallelReadingExecutor(lis, coreSize) {
				@Override
				protected boolean processParsingResult(final String line, final LogPointer currentOffset,
						final ReadingContext<MatcherContext> readingContext, final MatcherContext ctx)
								throws IOException {
					if (ctx != null) {
						sCtx.linesWithoutPattern = 0;
						if (sCtx.entry != null) {
							sCtx.entry.setRawContent(sCtx.text.toString());
							sCtx.entry.setEndOffset(sCtx.lastOffset);
							if (!consumer.consume(log, logAccess, sCtx.entry)) {
								sCtx.consumptionCancelled = true;
								return false;
							}
						}
						sCtx.entry = new LogEntry();
						sCtx.text = new StringBuilder(STRING_BUILDER_CAPACITY).append(line);
						if (ctx != null) {
							readingContext.fillAttributes(sCtx.entry, ctx);
						}
						sCtx.entry.setStartOffset(sCtx.lastOffset);
					} else {
						sCtx.linesWithoutPattern++;
						if (sCtx.entry == null) {
							sCtx.entry = new LogEntry();
							sCtx.entry.setStartOffset(sCtx.lastOffset);
							sCtx.entry.setUnformatted(true);
						}
						if (sCtx.text.length() > 0) {
							sCtx.text.append("\n");
						}
						sCtx.text.append(line);
						attachOverflowLine(sCtx.entry, line);
						if (sCtx.linesWithoutPattern >= maxUnfomattedLines) {
							logger.warn(
									"Pattern {} for log '{}' didn't matched any of read {} lines, adding unmatching data to previous log entry",
									getPatternInfo(), log.getPath(), sCtx.linesWithoutPattern);
							sCtx.entry.setRawContent(sCtx.text.toString());
							sCtx.entry.setEndOffset(sCtx.lastOffset);
							if (!consumer.consume(log, logAccess, sCtx.entry)) {
								sCtx.consumptionCancelled = true;
								return false;
							}
							sCtx.entry = null;
							sCtx.text = new StringBuilder(STRING_BUILDER_CAPACITY);
							sCtx.linesWithoutPattern = 0;
						}

					}
					sCtx.lastOffset = currentOffset;
					return true;
				}
			}.executeParallel();

			if (sCtx.entry != null && !sCtx.consumptionCancelled) {
				sCtx.entry.setRawContent(sCtx.text.toString());
				sCtx.entry.setEndOffset(sCtx.lastOffset);
				consumer.consume(log, logAccess, sCtx.entry);
			}
		} finally {
			lis.close();
		}
	}

	@Override
	public void readEntriesReverse(final Log log, final ByteLogAccess logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException {
		new FluentReverseReader<>(this).readEntries(log, logAccess, startOffset, consumer);
	}

	public final void readEntriesOld(final Log log, final ByteLogAccess logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException, FormatException {
		init();
		final ReadingContext<MatcherContext> readingContext = getReadingContext();
		LineInputStream lis = null;
		try {
			int linesWithoutPattern = 0;
			lis = new LineInputStream(logAccess, logAccess.getInputStream(startOffset), getCharset());
			LogEntry entry = null;
			StringBuilder text = new StringBuilder();
			String line;
			LogPointer lastOffset = lis.getPointer();
			if (lastOffset == null) {
				lastOffset = logAccess.start();
			}
			LogPointer currentOffset = null;
			while ((line = lis.readNextLine()) != null && (currentOffset = lis.getPointer()) != null) {
				final MatcherContext ctx = readingContext.matches(line);
				if (ctx != null) {
					linesWithoutPattern = 0;
					if (entry != null) {
						entry.setRawContent(text.toString());
						entry.setEndOffset(lastOffset);
						if (!consumer.consume(log, logAccess, entry)) {
							return;
						}
					}
					entry = new LogEntry();
					text = new StringBuilder(line);
					if (ctx != null) {
						readingContext.fillAttributes(entry, ctx);
					}
					entry.setStartOffset(lastOffset);
				} else {
					linesWithoutPattern++;
					if (entry == null) {
						entry = new LogEntry();
						entry.setStartOffset(lastOffset);
						entry.setUnformatted(true);
					}
					if (text.length() > 0) {
						text.append("\n");
					}
					text.append(line);
					attachOverflowLine(entry, line);
					if (linesWithoutPattern >= maxUnfomattedLines) {
						logger.warn(
								"Pattern {} for log '{}' didn't matched any of read {} lines, adding unmatching data to previous log entry",
								getPatternInfo(), log.getPath(), linesWithoutPattern);
						entry.setRawContent(text.toString());
						entry.setEndOffset(lastOffset);
						if (!consumer.consume(log, logAccess, entry)) {
							return;
						}
						entry = null;
						text = new StringBuilder();
						linesWithoutPattern = 0;
					}

				}
				lastOffset = currentOffset;
			}
			if (entry != null) {
				entry.setRawContent(text.toString());
				entry.setEndOffset(lastOffset);
				consumer.consume(log, logAccess, entry);
			}
		} finally {
			lis.close();
		}
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		final LinkedHashMap<String, FieldBaseTypes> fields = new LinkedHashMap<String, FieldBaseTypes>();
		fields.put(LogEntry.FIELD_RAW_CONTENT, FieldBaseTypes.STRING);
		return fields;
	}

	/**
	 * @return the maxUnfomattedLines
	 */
	public int getMaxUnfomattedLines() {
		return maxUnfomattedLines;
	}

	/**
	 * @param maxUnfomattedLines
	 *            the maxUnfomattedLines to set
	 */
	public void setMaxUnfomattedLines(final int maxUnfomattedLines) {
		this.maxUnfomattedLines = maxUnfomattedLines;
	}
}
