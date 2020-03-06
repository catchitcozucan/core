package com.github.catchitcozucan.core.histogram;

public interface LifeCycleProvider {
	Enum[] getCycle();
	Enum getCurrentStatus();
	String getCurrentProcess();
}