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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.interfaces.AsyncExecutor;
import com.github.catchitcozucan.core.interfaces.AsyncJobListener;
import com.github.catchitcozucan.core.interfaces.AsyncProcessListener;
import com.github.catchitcozucan.core.interfaces.AsyncTaskListener;
import com.github.catchitcozucan.core.interfaces.IsolationLevel;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.PoolConfig;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.Task;
import com.github.catchitcozucan.core.interfaces.TypedRelativeWithName;
import com.github.catchitcozucan.core.interfaces.WorkingEntity;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import com.github.catchitcozucan.core.internal.util.thread.ProcessThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Async implements AsyncExecutor, WorkingEntity {

    private static final int ID_LENGTH = 9;
    private static final int ID_DASHED_GROUPS = 3;
    private static final String SPACE = " ";
    private static final String EMPTY = "";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE = "%s %s was leaking exception - this is not how things should be..";
    private static final String NOPE_CALL_GET_INSTANCE_FIRST = "Nope - call getInstance() first!";
    private static final String AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE = "await termination was interrupted before carried out, no biggie :)";
    public static final String ID_SEPARATOR = "¤";
    private static Async INSTANCE; // NOSONAR
    private final ProcessThreadPool pool;
    private List<AsyncJobListener> listenersJobs;
    private List<AsyncProcessListener> listenersProcesses;
    private List<AsyncTaskListener> listenersTasks;
    private static Logger LOGGER = LoggerFactory.getLogger(JobBase.class);
    private HashSet<String> queuedIds;
    private HashSet<String> runningIds;
    private final int maxQueueSize;
    LinkedList<Task> waitingTasks;
    LinkedList<Process> waitingProcesses;
    LinkedList<Job> waitingJobs;

    private Async() {
        maxQueueSize = 0;
        pool = new ProcessThreadPool();
        initListenersAndQueues();
    }

    private Async(PoolConfig poolConfig) {
        pool = new ProcessThreadPool(poolConfig);
        initListenersAndQueues();
        maxQueueSize = poolConfig.maxQueueSize();
    }

    private void initListenersAndQueues() {
        listenersJobs = new ArrayList<>();
        listenersProcesses = new ArrayList<>();
        listenersTasks = new ArrayList<>();
        queuedIds = new HashSet<>();
        runningIds = new HashSet<>();
    }

    public static synchronized Async getInstance(PoolConfig poolConfig) {
        if (INSTANCE != null) {
            throw new ProcessRuntimeException("Async is already initiated - you cannot configure a runtime pool!");
        } else {
            INSTANCE = new Async(poolConfig);
        }
        return INSTANCE;
    }

    public static synchronized Async getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Async();
        }
        return INSTANCE;
    }

    @Override
    public boolean isExecuting() {
        if (pool == null || queuedIds == null || runningIds == null || (queuedIds.isEmpty() && runningIds.isEmpty())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isNamedJobRunningOrInQueue(String jobName) {
        return jobWithNameIsQueuedOrRunning(jobName);
    }

    @Override
    public Set<RunState> getCurrentState() {
        if (!isExecuting()) {
            return new HashSet<>();
        } else {
            Set<RunState> runStates = new HashSet<>();
            queuedIds.stream().forEach(q -> runStates.add(new RunState(RunState.State.InQueue, q)));
            runningIds.stream().forEach(r -> runStates.add(new RunState(RunState.State.InQueue, r)));
            return runStates;
        }
    }

    @Override
    public synchronized void kill() {
        try {
            pool.stopServer();
        } finally {
            INSTANCE = null;
        }
    }

    @Override
    public synchronized void killSilent() {
        try {
            pool.stopServer();
        }catch (Exception ignore){
        } finally {
            INSTANCE = null;
        }
    }

    @Override
    public synchronized void killAwaitTerminationBlocking(long timeout, TimeUnit unit) {
        try {
            pool.awaitTerminationBlocking(timeout, unit);
        } catch (InterruptedException e) { // NOSONAR I AM DOING STUFF
            LOGGER.info(AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE, e);
        } finally {
            INSTANCE = null;
        }
    }

    @Override
    public void killAwaitTerminationNonBlocking(long timeout, TimeUnit unit) {
        try {
            pool.awaitTerminationNonBlocking(timeout, unit);
        } finally {
            queuedIds = new HashSet<>();
            runningIds = new HashSet<>();
        }
    }

    @Override
    public synchronized void submitProcess(Process toExec) {
        if (INSTANCE == null) {
            throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
        }
        if (isolationLevelIsMet(toExec)) {
            ProcessRunnable process = new ProcessRunnable(toExec);
            addIdToQueue(process.id);
            pool.submit(process);
        } else {
            handleRejection(toExec);
        }
    }

    @Override
    public synchronized void submitJob(Job toExec) {
        if (INSTANCE == null) {
            throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
        }
        if (isolationLevelIsMet(toExec)) {
            JobRunnable job = new JobRunnable(toExec);
            addIdToQueue(job.id);
            pool.submit(job);
        } else {
            handleRejection(toExec);
        }
    }

    @Override
    public synchronized void submitTask(Task toExec) {
        if (INSTANCE == null) {
            throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
        }
        if (isolationLevelIsMet(toExec)) {
            TaskRunnable task = new TaskRunnable(toExec);
            addIdToQueue(task.id);
            pool.submit(task);
        } else {
            handleRejection(toExec);
        }
    }

    @Override
    public synchronized void submitJobWithTimeout(Job toExec, long timeout, TimeUnit unit) {
        if (INSTANCE == null) {
            throw new IllegalStateException(NOPE_CALL_GET_INSTANCE_FIRST);
        }
        JobRunnable job = new JobRunnable(toExec);
        addIdToQueue(job.id);
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
            id = generateId(job);
        }

        @Override
        public void run() {
            queuedIds.remove(id);
            runningIds.add(id);
            try {
                job.doJob();
            } catch (Exception ignore) {
                LOGGER.warn(String.format(LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, job.provideType().name(), job.name()), ignore);
            } finally {
                listenersJobs.stream().forEach(l -> l.jobExiting(job));
                queuedIds.remove(id);
                runningIds.remove(id);
                submitAwaitingTaskIfSafe();
            }
        }
    }

    private class TaskRunnable implements Runnable {
        private final Task task;
        private final String id;

        TaskRunnable(Task task) {
            this.task = task;
            id = generateId(task);
        }

        @Override
        public void run() {
            queuedIds.remove(id);
            runningIds.add(id);
            try {
                task.run();
            } catch (Exception ignore) {
                LOGGER.warn(String.format(LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, task.provideType().name(), task.name()), ignore);
            } finally {
                listenersTasks.stream().forEach(l -> l.taskExiting(task));
                queuedIds.remove(id);
                runningIds.remove(id);
                submitAwaitingTaskIfSafe();
            }
        }
    }

    private synchronized void submitAwaitingTaskIfSafe() {
        if (queuedIds.isEmpty() && runningIds.isEmpty() && !waitingTasks.isEmpty()) {
            if (!waitingTasks.isEmpty()) {
                submitTask(waitingTasks.poll());
            } else if (!waitingProcesses.isEmpty()) {
                submitProcess(waitingProcesses.poll());
            } else if (!waitingJobs.isEmpty()) {
                submitJob(waitingJobs.poll());
            }
        }
    }

    private class ProcessRunnable implements Runnable {
        private final Process process;
        private final String id;

        ProcessRunnable(Process process) {
            this.process = process;
            id = generateId(process);
        }

        @Override
        public void run() {
            queuedIds.remove(id);
            runningIds.add(id);
            try {
                process.process();
            } catch (Exception ignore) {
                LOGGER.warn(String.format(LEAKING_EXCEPTION_THIS_IS_NOT_HOW_THINGS_SHOULD_BE, process.provideType().name(), process.name()), ignore);
            } finally {
                listenersProcesses.stream().forEach(l -> l.processExiting(process));
                queuedIds.remove(id);
                runningIds.remove(id);
                submitAwaitingTaskIfSafe();
            }
        }
    }

    private boolean jobWithNameIsQueuedOrRunning(String jobName) {
        if (runningIds.isEmpty() && queuedIds.isEmpty() && waitingJobs.isEmpty()) {
            return false;
        } else {
            return isNamedTypeAndNameQuueedOrRunning(TypedRelativeWithName.Type.JOB.name(), jobName);
        }
    }

    private void addIdToQueue(String id) {
        if (maxQueueSize != 0 && queuedIds.size() >= maxQueueSize) {
            String message = String.format("Configured max allowed queue size %d reacher - submit is denied!", maxQueueSize);
            LOGGER.warn(message);
            throw new ProcessRuntimeException(message);
        }
        queuedIds.add(id);
    }

    //@formatter:off
    private String generateId(TypedRelativeWithName typedRelativeWithName) {
        return new StringBuilder(typedRelativeWithName.provideType().name())
                .append(ID_SEPARATOR)
                .append(typedRelativeWithName.name().replace(SPACE, EMPTY).toUpperCase())
                .append(ID_SEPARATOR)
                .append(IdGenerator.getInstance().getIdMoreRandom(ID_LENGTH, ID_DASHED_GROUPS))
                .toString();
    }
    //@formatter:on

    private boolean isolationLevelIsMet(TypedRelativeWithName toExec) {
        if (runningIds.isEmpty() && queuedIds.isEmpty()) {
            return true;
        }
        if (toExec.provideIsolationLevel().equals(IsolationLevel.Level.INCLUSIVE)) {
            return true;
        } else if (toExec.provideIsolationLevel().equals(IsolationLevel.Level.EXCLUSIVE)) {
            return !isExecuting();
        } else if (toExec.provideIsolationLevel().equals(IsolationLevel.Level.TYPE_EXCLUSIVE)) {
            return !isTypeQueuedOrRunning(toExec.provideType().name());
        } else if (toExec.provideIsolationLevel().equals(IsolationLevel.Level.KIND_EXCLUSIVE)) {
            return !isNamedTypeAndNameQuueedOrRunning(toExec.provideType().name(), toExec.name());
        } else {
            throw new ProcessRuntimeException(String.format("Yikes - sent in isolation type %s is NOT supported!", toExec.provideIsolationLevel().name()));
        }
    }

    private List<String> getAllKnownElements() {
        List<String> allIds = new ArrayList<>((Set<String>) runningIds.clone());
        allIds.addAll((Set<String>) queuedIds.clone());
        Collections.sort(allIds);
        return allIds;
    }

    private boolean isNamedTypeAndNameQuueedOrRunning(String type, String name) {
        return getAllKnownElements().stream().filter(id -> id.startsWith(type)).filter(id2 -> {
            String nameToCompare = id2.split(ID_SEPARATOR)[1];
            return (nameToCompare.equalsIgnoreCase(name));
        }).findFirst().isPresent();
    }

    private boolean isTypeQueuedOrRunning(String type) {
        return getAllKnownElements().stream().filter(id -> id.startsWith(type)).findFirst().isPresent();
    }

    private void handleRejection(TypedRelativeWithName toExec) {
        if (toExec.provideRejectionAction().equals(TypedRelativeWithName.RejectionAction.PUT_ON_WAITING_LIST)) {
            if (toExec.getClass().isAssignableFrom(Job.class)) {
                waitingJobs.addLast((Job) toExec);
            } else if (toExec.getClass().isAssignableFrom(Process.class)) {
                waitingProcesses.addLast((Process) toExec);
            } else if (toExec.getClass().isAssignableFrom(Task.class)) {
                waitingTasks.addLast((Task) toExec);
            } else {
                throw new ProcessRuntimeException(String.format("Yikes - what's this thing : %s is not of a supported type!", toExec.getClass().getName()));
            }
        } else if (toExec.provideRejectionAction().equals(TypedRelativeWithName.RejectionAction.REJECT)) {
            String message = String.format("Isolation level %s for %s of type %s is not met and as RejectionAction.REJECT is applied, we throw", toExec.provideIsolationLevel().name(), toExec.name(), toExec.provideType().name());
            LOGGER.warn(message);
            throw new ProcessRuntimeException(message);
        } else if (toExec.provideRejectionAction().equals(TypedRelativeWithName.RejectionAction.IGNORE)) {
            String message = String.format("Isolation level %s for %s of type %s is not met and we are told to ignore it", toExec.provideIsolationLevel().name(), toExec.name(), toExec.provideType().name());
            LOGGER.info(message);
        } else {
            String message = String.format("Isolation level %s for %s of type %s is not simply not supported!", toExec.provideIsolationLevel().name(), toExec.name(), toExec.provideType().name());
            LOGGER.error(message);
            throw new ProcessRuntimeException(message);
        }
    }
}