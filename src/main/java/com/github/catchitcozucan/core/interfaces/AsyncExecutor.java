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
package com.github.catchitcozucan.core.interfaces;

import java.util.concurrent.TimeUnit;

public interface AsyncExecutor {
	void submitProcess(Process toExec);
	void submitJob(Job toExec);
	void submitTask(Task toExec);
	void submitJobWithTimeout(Job toExec, long timeout, TimeUnit unit);
	void addJobListener(AsyncJobListener listener);
	void addProcessListener(AsyncProcessListener listener);
	void kill();
	void killSilent();
	void killAwaitTerminationBlocking(long timeout, TimeUnit unit);
	void killAwaitTerminationNonBlocking(long timeout, TimeUnit unit);
}
