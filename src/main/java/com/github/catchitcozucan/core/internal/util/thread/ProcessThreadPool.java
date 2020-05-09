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
package com.github.catchitcozucan.core.internal.util.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.interfaces.PoolConfig;
import com.github.catchitcozucan.core.internal.util.SizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessThreadPool implements Exitable {

    private static final String TIMEOUT_SPECIFIED_AS_D_S_WAS_REACHED_JOB_WAS_CANCELLED = "Timeout specified as %d %s was reached - job was cancelled";
    private static final String AWAIT_TERMINATION_WAS_INTERRUPTED_BEFORE_CARRIED_OUT_NO_BIGGIE = "await termination was interrupted before carried out, no biggie :)";
    private static final String TIMEOUT_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE = "Timeout awaiting termination specified as %d %s was reached - pool is now killed 'by force'";
    private static final String TIMEOUT_POOL_REEPER = "timeoutPoolReeper";
    private static final String S_EXITING_AFTER_S = "%s exiting after %s";
    private static final String TIMEOUT_FOR_TIMEOUT_THREAD_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE = "Timeout for timeout-thread awaiting termination specified as %d %s was reached - pool is now killed 'by force'";
    private Logger LOGGER = LoggerFactory.getLogger(ProcessThreadPool.class); // NOSONAR BULL.
    private ExecutorService executor;
    private ScheduledExecutorService executorForTimeout;
    private final boolean timeOutThreadIsUsed;
    private final PoolConfig poolConfig;
    Set<Interuptable> interuptables = new HashSet<>();

    public ProcessThreadPool() {
        poolConfig = null;
        timeOutThreadIsUsed = false;
        init(null, false);
        ExitHook.addExitHook(this);
    }

    public ProcessThreadPool(PoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        timeOutThreadIsUsed = true;
        init(poolConfig, true);
        ExitHook.addExitHook(this);
    }

    private synchronized void init(PoolConfig poolConfig, boolean includeTimeoutExec) {
        if (poolConfig == null) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            if (includeTimeoutExec && executorForTimeout == null) {
                executorForTimeout = Executors.newScheduledThreadPool(1);
            }
        } else {
            if (executor == null) {
                executor = Executors.newFixedThreadPool(poolConfig.maxNumberOfThreads());
            }
            if (executorForTimeout == null) {
                executorForTimeout = Executors.newScheduledThreadPool(1);
            }
        }
    }

    public void submit(Runnable r) {
        init(poolConfig, timeOutThreadIsUsed);
        if (poolConfig == null) {
            Task t = new Task(r);
            interuptables.add(t);
            executor.submit(t);
        } else {
            submitWithTimeout(r, poolConfig.maxExecTimePerRunnable().getNumber(), poolConfig.maxExecTimePerRunnable().getUnit());
        }
    }

    public void submitWithTimeout(Runnable r, long timeout, TimeUnit unit) {
        init(poolConfig, true);
        Task t = new Task(r);
        interuptables.add(t);
        final Future handler = executor.submit(t);
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
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignore) {
        }

        if ((executor != null && !executor.isTerminated()) || (executorForTimeout != null && !executorForTimeout.isTerminated())) {
            try {
                if (executor != null) {
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                }
                if (executorForTimeout != null) {
                    executorForTimeout.awaitTermination(1, TimeUnit.SECONDS);
                }

                interuptables.stream().forEach(i -> i.hardInterupt());
                interuptables.clear();

            } catch (Exception ignore) {
            } finally {
                if (executor != null && executor.isTerminated()) {
                    executor = null;
                } else {
                    if (executor != null) {
                        throw new ProcessRuntimeException("Internal async-pool's executor would not die!"); // this is bad.
                    }
                }
                if (executorForTimeout != null && executorForTimeout.isTerminated()) {
                    executorForTimeout = null;
                } else {
                    if (executorForTimeout != null) {
                        throw new ProcessRuntimeException("Internal async-pool's executorForTimeout would not die!"); // this is bad.
                    }
                }
            }
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
        private final long reaperAwait;

        private ReaperThread(long reeperAwait) {
            this.reaperAwait = reeperAwait;
        }

        @Override
        public void run() {
            try {
                executorForTimeout.awaitTermination(reaperAwait, TimeUnit.SECONDS);
                executorForTimeout = null;
            } catch (InterruptedException e) { // NOSONAR IT IS NOT IGNORED
                executorForTimeout.shutdownNow();
                LOGGER.info(String.format(TIMEOUT_FOR_TIMEOUT_THREAD_AWAITING_TERMINATION_SPECIFIED_AS_D_S_WAS_REACHED_POOL_IS_NOW_KILLED_BY_FORCE, reaperAwait, TimeUnit.SECONDS.toString()));
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

    private static class Task implements Callable<Long>, Interuptable {

        private Logger LOG = LoggerFactory.getLogger(Task.class); // NOSONAR BULL.
        private final Runnable myRunnable;
        private final Thread handleForHardInterupt;

        private Task(Runnable r) {
            handleForHardInterupt = Thread.currentThread();
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

        public void hardInterupt() {
            if (handleForHardInterupt != null && handleForHardInterupt.isAlive()) {
                try {
                    handleForHardInterupt.stop(); // yes, what can we do..
                } catch (Exception e) {
                    LOG.warn("I was harshly interupted", e);
                }
            }
        }
    }
}
