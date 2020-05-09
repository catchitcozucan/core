package com.github.catchitcozucan.core.interfaces;

import com.github.catchitcozucan.core.impl.startup.NumberOfTimeUnits;

public interface PoolConfig {
    NumberOfTimeUnits maxExecTimePerRunnable();
    int maxQueueSize();
    int maxNumberOfThreads();
}
