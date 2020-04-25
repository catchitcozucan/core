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
package com.github.catchitcozucan.core.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.histogram.HistogramProvider;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.interfaces.WorkingEntity;

public class JobThreadSafe implements Job, HistogramProvider, WorkingEntity {

	private static Map<String, JobThreadSafe> INSTANCE_MAP = new HashMap<>(); //NOSONAR
	private final Job job;
	private final HistogramProvider histogramProvider;
	private boolean isExecuting;
	private final ThreadSafeJob threadSafeJobWrapper;

	private JobThreadSafe(Object clientImpl) {

		this.job = (Job) clientImpl;
		this.histogramProvider = (HistogramProvider) clientImpl;
		threadSafeJobWrapper = new ThreadSafeJob() {

			@Override
			public HistogramStatus getHistogram(Stream<ProcessSubject> subjectStream) {
				return histogramProvider.getHistogram(subjectStream);
			}

			@Override
			public HistogramStatus makeSampleHistogram(Integer[] data) {
				return histogramProvider.makeSampleHistogram(data);
			}

			@Override
			public String nameThreadSafe() {
				return job.name();
			}

			@Override
			public void doJobThreadSafe() {
				if (isExecuting) {
					throw new ProcessRuntimeException(String.format("Nonono - nope. Man, % is ALREADY running. Use AsyncJob if this is what you want to do!", job.name()));
				}
				isExecuting = true;
				job.doJob();
				isExecuting = false;
			}

			@Override
			public ProcessSubject provideSubjectSampleThreadSafe() {
				return job.provideSubjectSample();
			}
		};
	}

	public static synchronized JobThreadSafe init(Object clientImpl) {
		JobThreadSafe instance = null;
		if (INSTANCE_MAP.get(clientImpl.getClass()) == null) {
			if (!Job.class.isAssignableFrom(clientImpl.getClass())) {
				throw new ProcessRuntimeException(String.format("You job does NOT implements %s! Tip of the day : your Job impl should extend %s", Job.class.getName(), JobBase.class.getName()));
			}
			if (!HistogramProvider.class.isAssignableFrom(clientImpl.getClass())) {
				throw new ProcessRuntimeException(String.format("You job does NOT implements %s! Tip of the day : your Job impl should extend %s", HistogramProvider.class.getName(), JobBase.class.getName()));
			}
			instance = new JobThreadSafe(clientImpl);
			INSTANCE_MAP.put(clientImpl.getClass().getName(), instance);
		}
		return instance;
	}

	public static synchronized JobThreadSafe getInstance(Class<?> c) {
		JobThreadSafe instance = INSTANCE_MAP.get(c.getName());
		if (instance == null) {
			throw new ProcessRuntimeException("Instance is NOT initialized - please call init(Object clientImpl) instead!");
		}
		return instance;
	}

	@Override
	public HistogramStatus getHistogram(Stream<ProcessSubject> subjectStream) {
		return threadSafeJobWrapper.getHistogram(subjectStream);
	}

	@Override
	public HistogramStatus makeSampleHistogram(Integer[] data) {
		return threadSafeJobWrapper.makeSampleHistogram(data);
	}

	@Override
	public String name() {
		return threadSafeJobWrapper.nameThreadSafe();
	}

	@Override
	public void doJob() {
		threadSafeJobWrapper.doJobThreadSafe();
	}

	@Override
	public ProcessSubject provideSubjectSample() {
		return threadSafeJobWrapper.provideSubjectSampleThreadSafe();
	}

	@Override
	public boolean isExecuting() {
		return isExecuting;
	}

	interface ThreadSafeJob extends HistogramProvider {
		String nameThreadSafe();

		void doJobThreadSafe();

		ProcessSubject provideSubjectSampleThreadSafe();
	}
}
