package com.crawljax.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import com.crawljax.core.configuration.CrawljaxConfiguration;

@Singleton
@ThreadSafe
public class ExitNotifier {

	/**
	 * Represents the reason Crawljax stopped.
	 */
	public enum ExitStatus {

		/**
		 * The maximum number of states is reached as defined in
		 * {@link CrawljaxConfiguration#getMaximumStates()}.
		 */
		MAX_STATES("Maximum states passed"),

		/**
		 * The maximum crawl time is reached as defined in
		 * {@link CrawljaxConfiguration#getMaximumRuntime()}.
		 */
		MAX_TIME("Maximum time passed"),

		/**
		 * The crawl is done.
		 */
		EXHAUSTED("Exausted"),

		/**
		 * The crawler quite because of an error.
		 */
		ERROR("Errored"),

		/**
		 * When {@link CrawljaxRunner#stop()} has been called.
		 */
		STOPPED("Stopped manually");

		private final String readableName;

		private ExitStatus(String readableName) {
			this.readableName = readableName;

		}

		@Override
		public String toString() {
			return readableName;
		}
	}

	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicInteger states = new AtomicInteger();
	private final int maxStates;

	private ExitStatus reason = ExitStatus.ERROR;
	private volatile boolean exit;

	public ExitNotifier(int maxStates) {
		this.maxStates = maxStates;
	}

	/**
	 * Waits until the crawl has to stop.
	 * 
	 * @throws InterruptedException
	 *             When the wait is interrupted.
	 */
	public ExitStatus awaitTermination() throws InterruptedException {
		latch.await();
		return reason;
	}

	/**
	 * @return The new number of states.
	 */
	public int incrementNumberOfStates() {
		int count = states.incrementAndGet();
		if (count == maxStates) {
			done(ExitStatus.MAX_STATES);
		}
		return count;
	}

	public void signalTimeIsUp() {
		done(ExitStatus.MAX_TIME);
	}

	/**
	 * Signal that all {@link CrawlTaskConsumer}s are done.
	 */
	public void signalCrawlExhausted() {
		done(ExitStatus.EXHAUSTED);
	}

	/**
	 * Manually stop the crawl.
	 */
	public void stop() {
		done(ExitStatus.STOPPED);
	}

	private void done(ExitStatus reason) {
		this.reason = reason;
		this.exit = true;
		latch.countDown();
	}

	boolean isExitCalled() {
		return exit;
	}
}
