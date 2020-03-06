package com.github.catchitcozucan.core.interfaces;

import java.io.Serializable;

import com.github.catchitcozucan.core.histogram.LifeCycleProvider;

public interface ProcessSubject extends Serializable, LifeCycleProvider {
    Integer id();
    String subjectIdentifier();
    int getErrorCode();
}
