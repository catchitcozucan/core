/**
 * Original work by Ola Aronsson 2020
 * Courtesy of nollettnoll AB &copy; 2012 - 2020
 * <p>
 * Licensed under the Creative Commons Attribution 4.0 International (the "License")
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * https://creativecommons.org/licenses/by/4.0/
 * <p>
 * The software is provided “as is”, without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other liability,
 * whether in an action of contract, tort or otherwise, arising from, out of or
 * in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.catchitcozucan.core.interfaces.AsyncExecutor;
import com.github.catchitcozucan.core.interfaces.AsyncJobListener;
import com.github.catchitcozucan.core.interfaces.AsyncProcessListener;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.WorkingEntity;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.thread.SimpleOneThreadedCacheableThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobAsync implements AsyncExecutor, WorkingEntity {

	private static final int ID_LENGTH = 9;
	private static final int ID_DASHED_GROUPS = 3;
	private static final String SPACE = " ";
	private static final String UNDERSCORE = "_";
	private static final String EMPTY = "";
	private static final String UNKNOWN = "UNKNOWN";
	private static final String JOB_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE = "job %s was leaking exception - this is not how things should be..";
	private static final String NOPE_CALL_GET_INSTANCE_FIRST = "Nope - call getInstance() first!";
	private static final String AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE = "await termination was interrupted before carried out, no biggie :)";
	private static final String PROCESS_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE = "process %s was leaking exception - this is not how things should be..";
	private static JobAsync INSTANCE; // NOSONAR
	private final SimpleOneThreadedCacheableThreadPool pool;
	private final List<AsyncJobListener> listenersJobs;
	private final List<AsyncProcessListener> listenersProcesses;
	private static Logger LOGGER = null; // NOSONAR
	private HashSet<String> jobIdsInQueue;
	private HashSet<String> jobIdsRunning;

	static {
		ProcessLogging.initLogging();
		LOGGER = LoggerFactory.getLogger(JobBase.class);
	}

	private JobAsync() {
		pool = new SimpleOneThreadedCacheableThreadPool();
		listenersJobs = new ArrayList<>();
		listenersProcesses = new ArrayList<>();
		jobIdsInQueue = new HashSet<>();
		jobIdsRunning = new HashSet<>();
	}

	public static synchronized JobAsync getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JobAsync();
		}
		return INSTANCE;
	}

	@Override
	public boolean isExecuting() {
		if (pool == null || jobIdsInQueue == null || jobIdsRunning == null || (jobIdsInQueue.isEmpty() && jobIdsRunning.isEmpty())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isNamedJobRunningOrInQueue(String jobName) {
		return jobIsMatchedInJobList(jobName);
	}

	@Override
	public Set<RunState> getCurrentState() {
		if (!isExecuting()) {
			return new HashSet<>();
		} else {
			Set<RunState> runStates = new HashSet<>();
			jobIdsInQueue.stream().forEach(q -> runStates.add(new RunState(RunState.State.InQueue, q)));
			jobIdsRunning.stream().forEach(r -> runStates.add(new RunState(RunState.State.InQueue, r)));
			return runStates;
		}
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
		ProcessRunnable process = new ProcessRunnable(toExec);
		jobIdsInQueue.add(process.id);
		pool.submit(process);
	}

	@Override
	public void submitJob(Job toExec) {
		if (INSTANCE == null) {
			throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
		}
		JobRunnable job = new JobRunnable(toExec);
		jobIdsInQueue.add(job.id);
		pool.submit(job);
	}

	@Override
	public void addJobWithTimeout(Job toExec, long timeout, TimeUnit unit) {
		if (INSTANCE == null) {
			throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
		}
		JobRunnable job = new JobRunnable(toExec);
		jobIdsInQueue.add(job.id);
		pool.submitWithTimeout(job, timeout, unit);
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
			id = new StringBuilder(jobNameToJobIdPrefix(job.name())).append(IdGenerator.getInstance().getIdMoreRandom(ID_LENGTH, ID_DASHED_GROUPS)).toString();
		}

		@Override
		public void run() {
			jobIdsInQueue.remove(id);
			jobIdsRunning.add(id);
			try {
				job.doJob();
			} catch (Exception ignore) {
				LOGGER.warn(String.format(JOB_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, job.name()), ignore);
			} finally {
				jobIdsRunning.remove(id);
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
			jobIdsInQueue.remove(id);
			jobIdsRunning.add(id);
			try {
				process.process();
			} catch (Exception ignore) {
				LOGGER.warn(String.format(PROCESS_S_WAS_LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, process.name()), ignore);
			} finally {
				jobIdsRunning.remove(id);
			}
			listenersProcesses.stream().forEach(l -> l.processExiting(process));
		}
	}

	private boolean jobIsMatchedInJobList(String jobName) {
		if (jobIdsRunning.isEmpty() && jobIdsInQueue.isEmpty()) {
			return false;
		} else {
			Optional<String> jobMatched = jobIdsRunning.stream().filter(jId -> jId.startsWith(jobNameToJobIdPrefix(jobName))).findFirst();
			if (jobMatched.isPresent()) {
				return true;
			}
			jobMatched = jobIdsInQueue.stream().filter(jId -> jId.startsWith(jobNameToJobIdPrefix(jobName))).findFirst();
			return jobMatched.isPresent();
		}
	}

	private String jobNameToJobIdPrefix(String jobName) {
		if (!IO.hasContents(jobName)) {
			return new StringBuilder(UNKNOWN).append(UNDERSCORE).toString();
		}
		return new StringBuilder(jobName.toUpperCase().replace(UNDERSCORE, EMPTY).replace(SPACE, EMPTY)).append(UNDERSCORE).toString();
	}
}