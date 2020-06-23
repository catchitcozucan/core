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
package com.github.catchitcozucan.core.histogram;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import com.github.catchitcozucan.core.impl.source.processor.Nameable;

public class LifeCycleHistogramCollector implements Collector<LifeCycleProvider, Map<String, Integer>, Map<String, Integer>> {

	private final LifeCycleProvider lifeCycle;

	public LifeCycleHistogramCollector(LifeCycleProvider lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public Nameable[] getCycle() {
		return lifeCycle.getCycleAsNameables();
	}

	@Override
	public java.util.function.Supplier<Map<String, Integer>> supplier() {
		Map<String, Integer> map = new LinkedHashMap<>();
		Arrays.stream(lifeCycle.getCycleAsNameables()).forEach(s -> map.put(s.name(), 0));
		return () -> map;
	}

	@Override
	public BiConsumer<Map<String, Integer>, LifeCycleProvider> accumulator() {
		return (Map<String, Integer> acc, LifeCycleProvider candidate) -> acc.put(candidate.getCurrentStatus().name(), acc.get(candidate.getCurrentStatus().name()) + 1);
	}

	@Override
	public BinaryOperator<Map<String, Integer>> combiner() {
		return (Map<String, Integer> result1, Map<String, Integer> result2) -> {
			Map<String, Integer> map = new LinkedHashMap<>();
			Arrays.stream(lifeCycle.getCycleAsNameables()).forEach(s -> map.put(s.name(), (result1.get(s.name()) + result2.get(s.name()))));
			return map;
		};
	}

	@Override
	public Function<Map<String, Integer>, Map<String, Integer>> finisher() {
		return Function.identity();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.of(CONCURRENT, UNORDERED);
	}

}