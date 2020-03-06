package com.github.catchitcozucan.core.demo.test.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unchecked")
public class ArrayRotator<T> {

	private final T[] array;
	private int index;
	private static final Random RANDOM = new Random(731111245);

	private enum Direction {
		FORWARD, BACKWARD
	}

	public enum RenderMethod {
		NEXT, RANDOM, PHASED_NEXT
	}

	private Direction direction;

	public ArrayRotator(T[] array) {
		if (array == null || array.length < 2) {
			throw new IllegalArgumentException("You need top ship an array of no less than 2 elements!");
		}
		this.array = array;
		index = -1;
		direction = Direction.FORWARD;
	}

	public T getNext() {
		index++;
		if (index > array.length - 1) {
			index = 0;
		}
		return array[index];
	}

	public T getRandom() {
		return array[RANDOM.nextInt((array.length - 1) + 1)];
	}

	public T getNextPhading() {
		if (direction == Direction.FORWARD) {
			index++;
		} else {
			index--;
			if (index < 0) {
				direction = Direction.FORWARD;
				index++;
			}
		}
		if (index > array.length - 1) {
			direction = Direction.BACKWARD;
			index--;
		}
		return array[index];
	}

	public T[] get(RenderMethod method, int numberOfItems) {
		List<T> list = new ArrayList<>();
		switch (method) {
			case NEXT:
				for (int i = 0; i < numberOfItems; i++) {
					list.add(getNext());
				}
				return (T[]) list.toArray();
			case RANDOM:
				for (int i = 0; i < numberOfItems; i++) {
					list.add(getRandom());
				}
				return (T[]) list.toArray();
			case PHASED_NEXT:
				for (int i = 0; i < numberOfItems; i++) {
					list.add(getNextPhading());
				}
				return (T[]) list.toArray();
			default:
				throw new RuntimeException(String.format("Not supported mode %s", method.name()));
		}
	}

	public T getActual(int atIndex) {
		return array[atIndex];
	}

}
