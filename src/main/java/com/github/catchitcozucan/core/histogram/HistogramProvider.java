package com.github.catchitcozucan.core.histogram;

public interface HistogramProvider {
	HistogramStatus getHistogram();
	HistogramStatus makeSampleHistogram(Integer[] data);
}
