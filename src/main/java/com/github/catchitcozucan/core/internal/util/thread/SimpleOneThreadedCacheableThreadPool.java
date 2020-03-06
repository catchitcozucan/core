package com.github.catchitcozucan.core.internal.util.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.catchitcozucan.core.internal.util.SizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleOneThreadedCacheableThreadPool implements Exitable {

	private static final String TIMEOUT_SPECIFIED_AS_D_S_WAS_REACHED_JOB_WAS_CANCELLED = "Timeout specified as %d %s was reached - job was cancelled";
	private static final String AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE = "await termination was interrupted before carried out, no biggie :)";
	private static final String TIMEOUT_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE = "Timeout awaiting termination specified as %d %s was reached - pool is now killed 'by force'";
	private static final String TIMEOUT_POOL_REEPER = "timeoutPoolReeper";
	private static final String S_EXITING_AFTER_S = "%s exiting after %s";
	private static final String TIMEOUT_FOR_TIMEOUT_THREAD_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE = "Timeout for timeout-thread awaiting termination specified as %d %s was reached - pool is now killed 'by force'";
	private Logger LOGGER = LoggerFactory.getLogger(SimpleOneThreadedCacheableThreadPool.class); // NOSONAR BULL.
	private ExecutorService executor;
	private ScheduledExecutorService executorForTimeout;

	public SimpleOneThreadedCacheableThreadPool() {
		init(false);
		ExitHook.addExitHook(this);
	}

	private synchronized void init(boolean includeTimeoutExec) {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}
		if (includeTimeoutExec && executorForTimeout == null) {
			executorForTimeout = Executors.newScheduledThreadPool(1);
		}
	}

	public void submit(Runnable r) {
		init(false);
		executor.submit(new Task(r));
	}

	public void submitWithTimeout(Runnable r, long timeout, TimeUnit unit) {
		init(true);
		final Future handler = executor.submit(new Task(r));
		executorForTimeout.schedule(() -> {
			if (handler != null && !handler.isDone()) {
				handler.cancel(true);
				LOGGER.info(String.format(TIMEOUT_SPECIFIED_AS_D_S_WAS_REACHED_JOB_WAS_CANCELLED, timeout, unit.toString()));
			}
		}, timeout, unit);
	}


	public synchronized void stopServer() {
		if (executorForTimeout != null) {
			executorForTimeout.shutdownNow();
			executorForTimeout = null;
		}
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

	public synchronized void awaitTerminationNonBlocking(final long timeout, final TimeUnit unit) {
		if (executorForTimeout != null) {
			executorForTimeout.shutdownNow();
		}
		executorForTimeout = Executors.newScheduledThreadPool(1);
		final Future handler = executor.submit(new Task(() -> {
			try {
				executor.awaitTermination(timeout, unit);
			} catch (InterruptedException e) { // NOSONAR IT IS CLEARLY NOT IGNORED
				LOGGER.info(AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE, e);
			}
		}));
		executorForTimeout.schedule(() -> {
			if (handler != null && !handler.isDone()) {
				handler.cancel(true);
				executor.shutdownNow();
				executor = null;
				LOGGER.info(String.format(TIMEOUT_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE, timeout, unit.toString()));
			}

		}, timeout, unit);
		double value = unit.toSeconds(timeout);
		ReaperThread reeper = new ReaperThread((long) value);
		reeper.setName(TIMEOUT_POOL_REEPER);
		reeper.start();
		ExitHook.addExitHook((Exitable) reeper);
	}

	public synchronized void awaitTerminationBlocking(long timeout, TimeUnit unit) throws InterruptedException {
		if (executorForTimeout != null) {
			executorForTimeout.shutdownNow();
			executorForTimeout = null;
		}
		executor.awaitTermination(timeout, unit);
		executor = null;
	}

	@Override
	public void exitz() {
		stopServer();
	}

	private class ReaperThread extends Thread implements Exitable {
		private final long reeperAwait;

		private ReaperThread(long reeperAwait) {
			this.reeperAwait = reeperAwait;
		}

		@Override
		public void run() {
			try {
				executorForTimeout.awaitTermination(reeperAwait, TimeUnit.SECONDS);
				executorForTimeout = null;
			} catch (InterruptedException e) { // NOSONAR IT IS NOT IGNORED
				executorForTimeout.shutdownNow();
				LOGGER.info(String.format(TIMEOUT_FOR_TIMEOUT_THREAD_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE, reeperAwait, TimeUnit.SECONDS.toString()));
			}
		}

		@Override
		public void exitz() {
			this.interrupt();
			if (executorForTimeout != null) {
				executorForTimeout.shutdownNow();
				executorForTimeout = null;
			}
		}
	}

	private static class Task implements Callable<Long> {

		private Logger LOG = LoggerFactory.getLogger(Task.class); // NOSONAR BULL.
		private final Runnable myRunnable;

		private Task(Runnable r) {
			this.myRunnable = r;
		}

		@Override
		public Long call() throws Exception {
			long start = System.currentTimeMillis();
			try {
				myRunnable.run();
			} finally { // NOSONAR
				long execTimeInMillis = System.currentTimeMillis() - start;
				LOG.info(String.format(S_EXITING_AFTER_S, Thread.currentThread().getName(), SizeUtils.getFormattedMillisPrintoutFriendly(execTimeInMillis))); // NOSONAR BULL.
				return execTimeInMillis; // NOSONAR
			}
		}
	}
}
