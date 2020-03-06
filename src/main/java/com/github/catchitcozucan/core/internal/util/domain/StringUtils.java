package com.github.catchitcozucan.core.internal.util.domain;

public class StringUtils {

	private static final String EMPTY = "";

	private StringUtils() {}

	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}
		return str.equals(EMPTY);
	}

	public static boolean hasContents(String input) {
		return input != null && input.trim().length() > 0;
	}
}
