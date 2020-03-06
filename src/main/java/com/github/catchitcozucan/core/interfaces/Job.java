package com.github.catchitcozucan.core.interfaces;

public interface Job {
	String name();
	void doJob();
	ProcessSubject provideSubjectSample();
	boolean isExecuting();
}
