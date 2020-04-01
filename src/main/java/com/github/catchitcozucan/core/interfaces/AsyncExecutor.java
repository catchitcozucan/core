package com.github.catchitcozucan.core.interfaces;

import java.util.concurrent.TimeUnit;

public interface AsyncExecutor {
	void submitProcess(Process toExec);
	void submitJob(Job toExec);
	void addJobWithTimeout(Job toExec, long timeout, TimeUnit unit);
	void addJobListener(AsyncJobListener listener);
	void addProcessListener(AsyncProcessListener listener);
	void kill();
	void killAwaitTerminationBlocking(long timeout, TimeUnit unit);
	void killAwaitTerminationNonBlocking(long timeout, TimeUnit unit);
}
