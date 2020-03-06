package com.github.catchitcozucan.core.util;

public class ThrowableUtils {
	private ThrowableUtils() {}

	public static String getTopStackInfo(Throwable t) {
		return new StringBuilder(t.getStackTrace()[0].getClassName()).append(".").append(t.getStackTrace()[0].getMethodName()).append(".").append(t.getStackTrace()[0].getLineNumber()).toString();
	}
}
