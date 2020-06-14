package com.github.catchitcozucan.core.histogram;

import com.github.catchitcozucan.core.impl.source.processor.Nameable;

public interface LifeCycleProvider {
	Enum[] getCycle();
	Nameable[] getCycleAsNameables();
	Enum getCurrentStatus();
	String getCurrentProcess();
}