package com.logsniffer.source.compound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logsniffer.event.Event;
import com.logsniffer.fields.FieldBaseTypes;
import com.logsniffer.model.Log;
import com.logsniffer.model.LogEntry;
import com.logsniffer.model.LogInputStream;
import com.logsniffer.model.LogPointer;
import com.logsniffer.model.LogPointerFactory;
import com.logsniffer.model.LogRawAccess;
import com.logsniffer.model.SeverityLevel;
import com.logsniffer.reader.FormatException;
import com.logsniffer.reader.LogEntryReader;
import com.logsniffer.source.compound.CompoundLogPointer.PointerPart;

/**
 * Composed reader accessing multiple logs in parallel and delegating to a
 * consumer ordered by the timestamp.
 * 
 * @author mbok
 *
 */
public class CompoundLogReader implements LogEntryReader<CompoundLogAccess> {
	protected static final int BUFFER_SIZE_PER_THREAD = 20;
	private static final Logger logger = LoggerFactory.getLogger(CompoundLogReader.class);

	private final List<LogInstance> composedLogs;

	public CompoundLogReader(final List<LogInstance> composedLogs) {
		super();
		this.composedLogs = composedLogs;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		final LinkedHashMap<String, FieldBaseTypes> composedFields = new LinkedHashMap<>();
		for (final LogInstance i : composedLogs) {
			composedFields.putAll(i.getReader().getFieldTypes());
		}
		return composedFields;
	}

	private final static class LogInstanceEntry implements Comparable<LogInstanceEntry> {
		private final int instanceIndex;
		private final LogEntry entry;
		private final long logSourceId;
		private final String logPath;
		private long tmst;
		private final boolean reverse;

		public LogInstanceEntry(final int instanceIndex, final LogEntry entry, final long logSourceId,
				final String logPath, final boolean reverse) {
			super();
			this.instanceIndex = instanceIndex;
			this.entry = entry;
			this.logSourceId = logSourceId;
			this.logPath = logPath;
			final Date tmstD = entry.getTimeStamp();
			if (tmstD != null) {
				tmst = tmstD.getTime();
			} else {
				tmst = 0;
			}
			this.reverse = reverse;
		}

		@Override
		public int compareTo(final LogInstanceEntry o) {
			if (tmst == o.tmst) {
				return reverse ? (o.instanceIndex - instanceIndex) : (instanceIndex - o.instanceIndex);
			} else {
				return (int) (reverse ? (o.tmst - tmst) : (tmst - o.tmst));
			}

		}
	}

	private abstract class CompositionReaderExecutor {
		private Semaphore threadSemaphore;
		private boolean running;
		private boolean blockedBuffer = true;
		private Exception terminationException;
		private final TreeSet<LogInstanceEntry> instanceEntries = new TreeSet<>();
		private final List<SubReaderThread> activeThreads = new ArrayList<>();
		private Semaphore[] instanceEntryBuffer;
		private int[] instanceCounters;
		private final PointerPart[] lastOffsets;
		private boolean consumptionActive = false;
		private final boolean reverse;
		// long sumFreeZycles = 0;
		// long countConsumptions = 0;

		public CompositionReaderExecutor(final PointerPart[] lastOffsets, final boolean reverse) {
			super();
			this.lastOffsets = lastOffsets;
			this.reverse = reverse;
		}

		protected boolean process(final LogInstanceEntry instanceEntry, final SubReaderThread subReaderThread) {
			try {
				boolean thisThreadShouldConsume = false;
				if (instanceEntry != null) {
					if (blockedBuffer) {
						instanceEntryBuffer[instanceEntry.instanceIndex].acquire();
					}
					synchronized (instanceEntries) {
						instanceEntries.add(instanceEntry);
						instanceCounters[instanceEntry.instanceIndex]++;
						if (!consumptionActive) {
							consumptionActive = true;
							thisThreadShouldConsume = true;
						}
					}
				} else {
					synchronized (instanceEntries) {
						if (!consumptionActive) {
							consumptionActive = true;
							thisThreadShouldConsume = true;
						}
					}
				}

				if (thisThreadShouldConsume) {
					try {
						synchronized (activeThreads) {
							int freeCycles = 0;
							while (running) {
								// Gather the next ordered entry, rewrite the
								// pointers
								// and
								// delegate to the consumer
								LogInstanceEntry nextEntryInstance = null;
								int instanceIndex;
								synchronized (instanceEntries) {
									if (instanceEntries.isEmpty()) {
										consumptionActive = false;
										return running;
									}
									// Check first all thread have filled the
									// buffers
									if (freeCycles <= 0) {
										freeCycles = Integer.MAX_VALUE;
										for (final SubReaderThread t : activeThreads) {
											final int c = instanceCounters[t.logInstanceIndex];
											if (c < freeCycles) {
												freeCycles = c;
											}
										}
										// sumFreeZycles += freeCycles;
										// countConsumptions++;
									}
									if (freeCycles <= 0) {
										consumptionActive = false;
										return running;
									}
									nextEntryInstance = instanceEntries.pollFirst();
									instanceIndex = nextEntryInstance.instanceIndex;
									instanceCounters[instanceIndex]--;
									freeCycles--;
								}
								final LogEntry nextEntry = nextEntryInstance.entry;
								lastOffsets[instanceIndex] = new PointerPart(nextEntryInstance.logSourceId,
										nextEntryInstance.logPath, nextEntry.getEndOffset());
								final CompoundLogPointer startPointer = new CompoundLogPointer(lastOffsets,
										nextEntry.getTimeStamp());
								lastOffsets[instanceIndex] = new PointerPart(nextEntryInstance.logSourceId,
										nextEntryInstance.logPath, nextEntry.getEndOffset());
								final CompoundLogPointer endPointer = new CompoundLogPointer(lastOffsets,
										nextEntry.getTimeStamp());
								nextEntry.setStartOffset(startPointer);
								nextEntry.setEndOffset(endPointer);
								nextEntry.put(Event.FIELD_SOURCE_ID, nextEntryInstance.logSourceId);
								nextEntry.put(Event.FIELD_LOG_PATH, nextEntryInstance.logPath);
								instanceEntryBuffer[instanceIndex].release();
								running &= consumeComposedReadingResult(nextEntry);
							}
						}
					} finally {
						// processingSemaphore.release();
						synchronized (instanceEntries) {
							consumptionActive = false;
						}
					}
				}
			} catch (final Exception e) {
				synchronized (activeThreads) {
					if (terminationException == null) {
						terminationException = e;
					} else {
						logger.warn("Consequential error", e);
					}
					running = false;
				}
			}
			return running;
		}

		protected abstract boolean consumeComposedReadingResult(LogEntry entry) throws IOException;

		/**
		 * Triggered when a thread has finished reading.
		 * 
		 * @param t
		 */
		protected void finished(final SubReaderThread t) {
			synchronized (activeThreads) {
				if (t.exception != null) {
					running = false;
					terminationException = t.exception;
				}
				activeThreads.remove(t);
				if (activeThreads.size() <= 1) {
					blockedBuffer = false;
				}
				if (!running) {
					// Finished, stop all threads
					for (final SubReaderThread tr : activeThreads) {
						instanceEntryBuffer[tr.logInstanceIndex].release(BUFFER_SIZE_PER_THREAD);
					}
					activeThreads.clear();
				}
			}
			// Call process to free possible blocked threads
			if (!blockedBuffer && running) {
				process(null, null);
			}
		}

		public void executeParallel() throws IOException {
			threadSemaphore = new Semaphore(composedLogs.size());
			synchronized (activeThreads) {
				running = true;
				instanceEntryBuffer = new Semaphore[composedLogs.size()];
				instanceCounters = new int[composedLogs.size()];
				for (int i = 0; i < composedLogs.size(); i++) {
					instanceEntryBuffer[i] = new Semaphore(BUFFER_SIZE_PER_THREAD);
					instanceCounters[i] = 0;
					try {
						activeThreads.add(new SubReaderThread(this, i, composedLogs.get(i), lastOffsets[i]));
					} catch (final InterruptedException e) {
						running = false;
						terminationException = e;
					}
				}
				for (final SubReaderThread t : activeThreads) {
					t.start();
				}
			}
			try {
				threadSemaphore.acquire(composedLogs.size());
			} catch (final InterruptedException e) {
				logger.error("Failed to wait for parallel threads", e);
			}
			// System.out.println((double) sumFreeZycles / countConsumptions);
			// Complete buffered if consumer is still listening
			process(null, null);
			if (terminationException != null) {
				if (terminationException instanceof IOException) {
					throw (IOException) terminationException;
				} else if (terminationException instanceof RuntimeException) {
					throw (RuntimeException) terminationException;
				} else {
					throw new RuntimeException("Composed reading aborted with an exception", terminationException);
				}
			}
		}
	}

	private class SubReaderThread extends Thread {
		private final String logPath;
		private final long logSourceId;
		private final LogInstance logInstance;
		private final CompositionReaderExecutor executor;
		private final int logInstanceIndex;
		private final LogPointer startOffset;
		private Exception exception;
		private final boolean reverse;

		public SubReaderThread(final CompositionReaderExecutor compositionReaderExecutor, final int logInstanceIndex,
				final LogInstance logInstance, final PointerPart start) throws InterruptedException {
			super("ComposedReader_" + logInstanceIndex + "_" + compositionReaderExecutor.instanceCounters.length);
			this.logInstance = logInstance;
			this.logSourceId = logInstance.getLogSourceId();
			this.logPath = logInstance.getLog().getPath();
			this.executor = compositionReaderExecutor;
			this.logInstanceIndex = logInstanceIndex;
			this.startOffset = start != null ? start.getOffset() : null;
			this.reverse = executor.reverse;
			executor.threadSemaphore.acquire(1);
		}

		@Override
		public void run() {
			try {
				final LogEntryReader<LogRawAccess<? extends LogInputStream>> reader = logInstance.getReader();
				final LogEntryConsumer consumer = new LogEntryConsumer() {
					@Override
					public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
							throws IOException {
						return executor.process(
								new LogInstanceEntry(logInstanceIndex, entry, logSourceId, logPath, reverse),
								SubReaderThread.this);
					}
				};
				if (reverse) {
					reader.readEntriesReverse(logInstance.getLog(), logInstance.getLogAccess(), startOffset, consumer);
				} else {
					reader.readEntries(logInstance.getLog(), logInstance.getLogAccess(), startOffset, consumer);
				}
			} catch (final Exception e) {
				logger.error("Failed to read from log instance " + logInstanceIndex + ": " + logInstance, e);
				exception = e;
			} finally {
				executor.finished(this);
				executor.threadSemaphore.release(1);
			}
		}

	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		final Set<SeverityLevel> levels = new HashSet<>();
		for (final LogInstance i : composedLogs) {
			levels.addAll(i.getReader().getSupportedSeverities());
		}
		final List<SeverityLevel> sortedLevels = new ArrayList<>(levels);
		Collections.sort(sortedLevels);
		return sortedLevels;
	}

	@Override
	public void readEntries(final Log log, final CompoundLogAccess logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException, FormatException {
		readEntries(log, logAccess, startOffset, consumer, false);
	}

	private void readEntries(final Log log, final CompoundLogAccess logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer, final boolean reverse) throws IOException, FormatException {
		final CompoundLogPointer clp = (CompoundLogPointer) logAccess.refresh(startOffset).get();
		final PointerPart[] parts = new PointerPart[composedLogs.size()];
		for (int i = 0; i < composedLogs.size(); i++) {
			parts[i] = clp != null ? clp.getPart(composedLogs.get(i).getLog().getPath()) : null;
		}
		new CompositionReaderExecutor(parts, reverse) {
			@Override
			protected boolean consumeComposedReadingResult(final LogEntry entry) throws IOException {
				return consumer.consume(log, logAccess, entry);
			}
		}.executeParallel();

	}

	@Override
	public void readEntriesReverse(final Log log, final CompoundLogAccess logAccess, final LogPointer startOffset,
			final com.logsniffer.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		readEntries(log, logAccess, startOffset, consumer, true);
	}

}
