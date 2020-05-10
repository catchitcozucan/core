package com.github.catchitcozucan.core.impl;

import java.util.Set;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.startup.NumberOfTimeUnits;
import com.github.catchitcozucan.core.interfaces.AsyncExecutor;
import com.github.catchitcozucan.core.interfaces.AsyncJobListener;
import com.github.catchitcozucan.core.interfaces.AsyncProcessListener;
import com.github.catchitcozucan.core.interfaces.CatchItConfig;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.Task;
import com.github.catchitcozucan.core.interfaces.WorkingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatchIt implements AsyncExecutor, WorkingEntity {

    private static CatchIt INSTANCE;
    private static Logger LOGGER = null; //NOSONAR

    private CatchIt(CatchItConfig config) {
        if (config != null && config.getPoolConfig() != null && config.getPoolConfig().maxNumberOfThreads() > 1) {
            ProcessLogging.initLogging(config.getLogConfig());
            Async.getInstance(config.getPoolConfig());
        } else {
            ProcessLogging.initLogging();
            Async.getInstance();
        }
        LOGGER = LoggerFactory.getLogger(CatchIt.class);
    }

    public static synchronized void init() {
        if (INSTANCE == null) {
            INSTANCE = new CatchIt(null);
        } else {
            LOGGER.warn("I am already initialized");
        }
    }

    public static synchronized void init(CatchItConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new CatchIt(config);
        } else {
            LOGGER.warn("I am already initialized");
        }
    }

    public static synchronized void reInit(CatchItConfig config) {
        halt();
        INSTANCE = new CatchIt(config);
    }

    public static synchronized CatchIt getInstance() {
        if (INSTANCE == null) {
            throw new ProcessRuntimeException("Initalize me first!");
        }
        return INSTANCE;
    }

    public static synchronized void halt() {
        killInternal(true);
        INSTANCE = null;
    }

    public static synchronized void reInitPool(CatchItConfig config) {
        killInternal(false);
        INSTANCE = null;
        init(config);
    }

    private static void killInternal(boolean killLogging) {
        try {
            Async.getInstance().kill();
        } catch (Exception e) {
            if (LOGGER != null) {
                LOGGER.warn("There were Issues during stop", e);
            }
        }
        if (killLogging) {
            try {
                ProcessLogging.halt();
            } catch (Exception e) {
                if (LOGGER != null) {
                    LOGGER.warn("There were Issues during logging stop", e);
                }
            }
        }
    }

    public static void killExecutions() {
        killInternal(false);
    }

    @Override
    public void submitProcess(Process toExec) {
        Async.getInstance().submitProcess(toExec);
    }

    @Override
    public void submitJob(Job toExec) {
        Async.getInstance().submitJob(toExec);
    }

    @Override
    public void submitTask(Task toExec) {
        Async.getInstance().submitTask(toExec);
    }

    @Override
    public void submitJobWithTimeout(Job toExec, NumberOfTimeUnits numberOfTimeUnits) {
        Async.getInstance().submitJobWithTimeout(toExec, numberOfTimeUnits.getNumber(), numberOfTimeUnits.getUnit());
    }

    @Override
    public void addJobListener(AsyncJobListener listener) {
        Async.getInstance().addJobListener(listener);
    }

    @Override
    public void addProcessListener(AsyncProcessListener listener) {
        Async.getInstance().addProcessListener(listener);
    }

    @Override
    public void kill() {
        Async.getInstance().kill();
    }

    @Override
    public void killSilent() {
        Async.getInstance().killSilent();
    }

    @Override
    public void killAwaitTerminationBlocking(NumberOfTimeUnits numberOfTimeUnits) {
        Async.getInstance().killAwaitTerminationBlocking(numberOfTimeUnits.getNumber(), numberOfTimeUnits.getUnit());
    }

    @Override
    public void killAwaitTerminationNonBlocking(NumberOfTimeUnits numberOfTimeUnits) {
        Async.getInstance().killAwaitTerminationNonBlocking(numberOfTimeUnits.getNumber(), numberOfTimeUnits.getUnit());
    }

    @Override
    public boolean isExecuting() {
        return CatchIt.currentlyExecuting();
    }

    @Override
    public boolean isNamedJobRunningOrInQueue(String jobName) {
        return Async.getInstance().isNamedJobRunningOrInQueue(jobName);
    }

    @Override
    public Set<RunState> getCurrentState() {
        return Async.getInstance().getCurrentState();
    }

    public static boolean currentlyExecuting() {
        return Async.getInstance().isExecuting();
    }
}