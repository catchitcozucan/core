package com.github.catchitcozucan.core.internal.util.domain;

public abstract class BaseDomainObject implements ToStringAble {

	private static final String REGEXP_ONLY_NUMBERS = "^[0-9]+$";

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
			return "null";
		} else {
			return str;
		}
	}
}