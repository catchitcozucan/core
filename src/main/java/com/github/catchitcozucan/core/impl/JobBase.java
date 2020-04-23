/**
 *    Copyright [2020] [Ola Aronsson, courtesy of nollettnoll AB]
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

package com.github.catchitcozucan.core.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.github.catchitcozucan.core.histogram.HistogramProvider;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.histogram.LifeCycleHistogramCollector;
import com.github.catchitcozucan.core.ErrorCodeCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.internal.util.SizeUtils;


public abstract class JobBase implements Job, HistogramProvider {

	private LifeCycleHistogramCollector cycleHistogramCollector;
	private long processInstanceExectime;
	private long totalExectime;
	private int numberOfProcesses;
	private static final String TIME_REPORTING = "JOB EXEC TIME : %s PROCESS AVARAGE EXEC TIME : %s";
	private boolean amIWorking;
	private static Logger LOGGER = null; // NOSONAR

	static {
		ProcessLogging.initLogging();
		LOGGER = LoggerFactory.getLogger(JobBase.class);
	}

	@Override
	public boolean isExecuting() {
		return amIWorking;
	}

	@Override
	public HistogramStatus getHistogram(Stream<ProcessSubject> subjectStream) {
		if (cycleHistogramCollector == null) {
			cycleHistogramCollector = new LifeCycleHistogramCollector(provideSubjectSample());
		}
		return new HistogramStatus(name(), subjectStream.collect(cycleHistogramCollector), null);
	}

	@Override
	public HistogramStatus makeSampleHistogram(Integer[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("You are required to pass in some data - you sent null or an empty array");
		}
		return new HistogramStatus(name(), makeUpData(data), null);
	}

	protected void exec(Process proc) {
		if (cycleHistogramCollector == null) {
			cycleHistogramCollector = new LifeCycleHistogramCollector(((ProcessBase) proc).getSubject());
			if (!ProcessBase.class.isAssignableFrom(proc.getClass())) {
				throw new IllegalArgumentException(String.format("Your process HAS TO extend %s!", ProcessBase.class.getName()));
			}
		}
		if (processInstanceExectime == 0) {
			processInstanceExectime = System.currentTimeMillis();
		}
		String prefix = String.format("Item %s for subject %s", ((ProcessBase) proc).getSubject().id(), ((ProcessBase) proc).getSubject().subjectIdentifier());
		try {
			LOGGER.info(String.format("%s entering job '%s'. Pre-process state : %s [%s]", prefix, name(), ((ProcessBase) proc).getCurrentStatus(), ((ProcessBase) proc).getStatusDescription()));//NOSONAR
			amIWorking = true;
			proc.process();
		} catch (Exception e) {
			ProcessBase processBase = ((ProcessBase) proc);
			String messageSuffix = String.format("'%s' on item %s for subject %s", proc.name(), ((ProcessBase) proc).getSubject().id(), ((ProcessBase) proc).getSubject().subjectIdentifier());
			Enum<?> currentStatusUponFailure = processBase.getCurrentStatusUponFailure();
			if (currentStatusUponFailure == null) {
				currentStatusUponFailure = processBase.getCurrentStatus();
			}
			if (e instanceof ErrorCodeCarrier) { // NOSONAR BULL.
				processBase.bail(messageSuffix, new InternalJobNonRuntimeException(messageSuffix, e), currentStatusUponFailure, processBase.evalutateForErrorCode((ErrorCodeCarrier) e));
			} else {
				processBase.bail(messageSuffix, new InternalJobNonRuntimeException(messageSuffix, e), currentStatusUponFailure, null);
			}
		} finally {
			amIWorking = false;
			if (processInstanceExectime != 0) {
				totalExectime += System.currentTimeMillis() - processInstanceExectime;
				processInstanceExectime = 0;
				numberOfProcesses++;
			}
			LOGGER.info(String.format("%s exiting job '%s' as finished : %b after %s. Exit state is : [%s]", prefix, name(), ((ProcessBase) proc).finished(), totalExectime, ((ProcessBase) proc).getStatusDescription()));//NOSONAR
		}
	}

	protected String getTotalExectime() {
		String execTimeFriendly = SizeUtils.getFormattedMillisPrintoutFriendly(totalExectime);
		String perProcessMillis;
		if (numberOfProcesses > 0) {
			perProcessMillis = SizeUtils.getFormattedMillisPrintoutFriendly((totalExectime / numberOfProcesses));
		} else {
			perProcessMillis = "0 millis";
		}
		totalExectime = 0;
		return String.format(TIME_REPORTING, execTimeFriendly, perProcessMillis);
	}

	private Map<String, Integer> makeUpData(Integer[] data) {
		List<String> labels = new ArrayList<>();
		Arrays.stream(provideSubjectSample().getCycle()).forEachOrdered(s -> labels.add(s.name()));
		String[] labelz = labels.toArray(new String[labels.size()]);
		Map<String, Integer> datan = new LinkedHashMap<>();
		for (int i = 0; i < labelz.length; i++) {
			Integer value = i < data.length ? data[i] : 0;
			datan.put(labelz[i], value);
		}
		return datan;
	}
}
