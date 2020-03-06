package com.github.catchitcozucan.core.histogram;

import java.util.stream.Stream;

import com.github.catchitcozucan.core.interfaces.ProcessSubject;

public interface HistogramProvider {
	HistogramStatus getHistogram(Stream<ProcessSubject> subjectStream);
	HistogramStatus makeSampleHistogram(Integer[] data);
}
