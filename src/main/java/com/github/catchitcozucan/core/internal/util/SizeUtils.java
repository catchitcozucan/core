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
package com.github.catchitcozucan.core.internal.util;

public class SizeUtils {

	private static final String D_MILLIS = "%d millis";
	private static final String D_SECONDS_D_MILLIS = "%d seconds %d millis";
	private static final String D_MINUTES_D_SECONDS_D_MILLIS = "%d minutes %d seconds %d millis";
	private static final String D_HOURS_D_MINUTES_D_SECONDS_D_MILLIS = "%d hours %d minutes %d seconds %d millis";
	private static final String D_DAYS_D_HOURS_D_MINUTES_D_SECONDS_D_MILLIS = "%d days %d hours %d minutes %d seconds %d millis";

	private SizeUtils() {}

	public static String getFormattedMillisPrintoutFriendly(long execTimeInMillis) {
		long days = execTimeInMillis / 86400000l;
		if (days > 0) {
			execTimeInMillis = execTimeInMillis - (days * 86400000l);
		}
		long hours = execTimeInMillis / 36000000l;
		if (hours > 0) {
			execTimeInMillis = execTimeInMillis - (hours * 36000000);
		}
		long minutes = execTimeInMillis / 60000l;
		if (minutes > 0) {
			execTimeInMillis = execTimeInMillis - (minutes * 60000);
		}
		long seconds = execTimeInMillis / 1000l;
		if (seconds > 0) {
			execTimeInMillis = execTimeInMillis - (seconds * 1000);
		}

		if (days != 0) {
			return String.format(D_DAYS_D_HOURS_D_MINUTES_D_SECONDS_D_MILLIS, days, hours, minutes, seconds, execTimeInMillis);
		} else if (hours != 0) {
			return String.format(D_HOURS_D_MINUTES_D_SECONDS_D_MILLIS, hours, minutes, seconds, execTimeInMillis);
		} else if (minutes != 0) {
			return String.format(D_MINUTES_D_SECONDS_D_MILLIS, minutes, seconds, execTimeInMillis);
		} else if (seconds != 0) {
			return String.format(D_SECONDS_D_MILLIS, seconds, execTimeInMillis);
		} else {
			return String.format(D_MILLIS, execTimeInMillis);
		}
	}
}
