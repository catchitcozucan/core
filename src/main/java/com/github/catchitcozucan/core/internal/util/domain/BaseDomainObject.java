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
package com.github.catchitcozucan.core.internal.util.domain;

public abstract class BaseDomainObject implements ToStringAble {

	private static final String REGEXP_ONLY_NUMBERS = "^[0-9]+$";
	public static final String NULL = "null";

	@Override
	public final int hashCode() {
		String in = toString();
		if (in.matches(REGEXP_ONLY_NUMBERS)) {
			return Integer.parseInt(in);
		}
		return toString().hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || !this.getClass().isAssignableFrom(other.getClass())) {
			return false;
		}
		return hashCode() == other.hashCode();
	}

	@Override
	public final String toString() {
		String str = doToString();
		if (str == null) {
			return NULL;
		} else {
			return str;
		}
	}
}