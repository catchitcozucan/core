package com.github.catchitcozucan.core.histogram;

import com.github.catchitcozucan.core.impl.source.processor.Nameable;

public interface LifeCycleProvider {
	Nameable[] getCycle();
	Enum getCurrentStatus();
	String getCurrentProcess();
}