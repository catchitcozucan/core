package com.github.catchitcozucan.core.impl.startup;

import com.github.catchitcozucan.core.impl.Async;
import com.github.catchitcozucan.core.impl.ProcessLogging;
import com.github.catchitcozucan.core.interfaces.CatchItConfig;
import org.slf4j.Logger;

public class BasicControl {

    private static BasicControl INSTANCE;
    private static Logger LOGGER = null; //NOSONAR

    private BasicControl(CatchItConfig config) {
        ProcessLogging.initLogging(config.getLogConfig());
        if (config != null || config.getPoolConfig().maxNumberOfThreads() > 1) {
            Async.getInstance(config.getPoolConfig());
        } else {
            Async.getInstance();
        }
    }

    public static synchronized void init(CatchItConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new BasicControl(config);
        } else {
            LOGGER.warn("I am already initialized");
        }
    }

    public static synchronized void stop() {
        try {
            Async.getInstance().kill();
        } catch (Exception e) {
            if (LOGGER != null) {
                LOGGER.warn("There were Issues during stop", e);
            }
        }
        try {
            ProcessLogging.halt();
        } catch (Exception e) {
            if (LOGGER != null) {
                LOGGER.warn("There were Issues during logging stop", e);
            }
        }
    }
}
