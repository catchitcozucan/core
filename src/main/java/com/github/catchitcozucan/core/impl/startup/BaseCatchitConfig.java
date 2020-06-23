/**
 *    Original work by Ola Aronsson 2020
 *    Courtesy of nollettnoll AB &copy; 2012 - 2020
 *
 *    Licensed under the Creative Commons Attribution 4.0 International (the "License")
 *    you may not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *                https://creativecommons.org/licenses/by/4.0/
 *
 *    The software is provided “as is”, without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other liability,
 *    whether in an action of contract, tort or otherwise, arising from, out of or
 *    in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl.startup;

import com.github.catchitcozucan.core.interfaces.CatchItConfig;
import com.github.catchitcozucan.core.interfaces.PoolConfig;

public abstract class BaseCatchitConfig implements CatchItConfig {

	public static final PoolConfig DEFAULT_ONE_THREADED_CONFIG = new PoolConfig() { //NOSONAR
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
