package com.github.catchitcozucan.core.demo.shoe.internal;


import com.github.catchitcozucan.core.demo.test.support.ArrayRotator;

public class LaceProvider {

	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String YELLOW = "yellow";

	private ArrayRotator<Laces> LACES = new ArrayRotator<>(new Laces[] { new Laces(RED, 11L), new Laces(GREEN, 12L), new Laces(YELLOW, 13L) });

	private LaceProvider() {
	}

	private static LaceProvider INSTANCE;

	public static synchronized LaceProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LaceProvider();
		}
		return INSTANCE;
	}

	public Laces getFreshLaces() {
		return LACES.getNextPhading();
	}

}
