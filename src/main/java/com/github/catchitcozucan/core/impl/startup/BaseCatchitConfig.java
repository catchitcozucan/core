package com.github.catchitcozucan.core.impl.startup;

import com.github.catchitcozucan.core.interfaces.CatchItConfig;
import com.github.catchitcozucan.core.interfaces.PoolConfig;

public abstract class BaseCatchitConfig implements CatchItConfig {

    public static PoolConfig DEFAULT_ONE_THREADED_CONFIG = new PoolConfig() {
        @Override
        public NumberOfTimeUnits maxExecTimePerRunnable() {
            return null;
        }

        @Override
        public int maxQueueSize() {
            return 0;
        }

        @Override
        public int maxNumberOfThreads() {
            return 1;
        }
    };

    @Override
    public PoolConfig getPoolConfig() {
        return DEFAULT_ONE_THREADED_CONFIG;
    }
}
