package com.github.catchitcozucan.core.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.interfaces.AsyncExecutor;
import com.github.catchitcozucan.core.interfaces.AsyncJobListener;
import com.github.catchitcozucan.core.interfaces.AsyncProcessListener;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.WorkingEntity;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import com.github.catchitcozucan.core.internal.util.thread.SimpleOneThreadedCacheableThreadPool;

public class JobAsync implements AsyncExecutor, WorkingEntity {

	private static final int ID_LENGTH = 9;
	private static final int ID_DASHED_GROUPS = 3;
	public static final String PROCESS_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE = "process %s was leaking exception - this is not how things should be..";
	public static final String JOB_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE = "job %s was leaking exception - this is not how things should be..";
	public static final String NOPE_CALL_GET_INSTANCE_FIRST = "Nope - call getInstance() first!";
	public static final String AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE = "await termination was interrupted before carried out, no biggie :)";
	private static JobAsync INSTANCE; // NOSONAR
	private final SimpleOneThreadedCacheableThreadPool pool;
	private final List<AsyncJobListener> listenersJobs;
	private final List<AsyncProcessListener> listenersProcesses;
	private HashSet<String> jobIds;
	private static Logger LOGGER = null; // NOSONAR

	static {
		ProcessLogging.initLogging();
		LOGGER = LoggerFactory.getLogger(JobBase.class);
	}

	private JobAsync() {
		pool = new SimpleOneThreadedCacheableThreadPool();
		listenersJobs = new ArrayList<>();
		listenersProcesses = new ArrayList<>();
		jobIds = new HashSet<>();
	}

	public static synchronized JobAsync getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JobAsync();
		}
		return INSTANCE;
	}

	@Override
	public boolean isExecuting() {
		if (pool == null || jobIds == null) {
			return false;
		}
		return !jobIds.isEmpty();
	}

	@Override
	public void kill() {
		pool.stopServer();
	}

	@Override
	public void killAwaitTerminationBlocking(long timeout, TimeUnit unit) {
		try {
			pool.awaitTerminationBlocking(timeout, unit);
		} catch (InterruptedException e) { // NOSONAR I AM DOING STUFF
			LOGGER.info(AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE, e);
		}
	}

	@Override
	public void killAwaitTerminationNonBlocking(long timeout, TimeUnit unit) {
		pool.awaitTerminationNonBlocking(timeout, unit);
	}

	@Override
	public void submitProcess(Process toExec) {
		if (INSTANCE == null) {
			throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
		}
		pool.submit(new ProcessRunnable(toExec));
	}

	@Override
	public void submitJob(Job toExec) {
		if (INSTANCE == null) {
			throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
		}
		pool.submit(new JobRunnable(toExec));
	}

	@Override
	public void addJobWithTimeout(Job toExec, long timeout, TimeUnit unit) {
		if (INSTANCE == null) {
			throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
		}
		pool.submitWithTimeout(new JobRunnable(toExec), timeout, unit);
	}

	@Override
	public synchronized void addJobListener(AsyncJobListener listener) {
		listenersJobs.add(listener);
	}

	@Override
	public void addProcessListener(AsyncProcessListener listener) {
		listenersProcesses.add(listener);
	}

	private class JobRunnable implements Runnable {
		private final Job job;
		private final String id;

		JobRunnable(Job job) {
			this.job = job;
			id = IdGenerator.getInstance().getIdMoreRandom(ID_LENGTH, ID_DASHED_GROUPS);
		}

		@Override
		public void run() {
			jobIds.add(id);
			try {
				job.doJob();
			} catch (Exception ignore) {
				LOGGER.warn(String.format(JOB_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, job.name()), ignore);
			} finally {
				jobIds.remove(id);
			}
			listenersJobs.stream().forEach(l -> l.jobExiting(job));
		}
	}

	private class ProcessRunnable implements Runnable {
		private final Process process;
		private final String id;

		ProcessRunnable(Process process) {
			this.process = process;
			id = IdGenerator.getInstance().getIdMoreRandom(ID_LENGTH, ID_DASHED_GROUPS);
		}

		@Override
		public void run() {
			jobIds.add(id);
			try {
				process.process();
			} catch (Exception ignore) {
				LOGGER.warn(String.format(PROCESS_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, process.name()), ignore);
			} finally {
				jobIds.remove(id);
			}
			listenersProcesses.stream().forEach(l -> l.processExiting(process));
		}
	}
}