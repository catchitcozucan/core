/**
 *    Copyright [2020] [Ola Aronsson, courtesy of nollettnoll AB]
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

import java.lang.reflect.Array;
import java.util.Collection;

public class ToStringBuilder extends BaseDomainObject {

	private static final String LJAVA_LANG_OBJECT = "[Ljava.lang.Object;";
	private static final String COMMA_SPACE = ", ";
	private static final String SPACE_RIGHT_BRACKET = "] ";
	private static final String AT = "@";
	private static final String LEFT_BRACKET = "[";
	private static final String LEFT_CURLY = "{";
	private static final String RIGHT_CURLY = "}";
	private static final String RIGHT_BRACKET = "]";
	private static final String NULL1 = "null";
	private static final String LABEL = "label";
	private static final String CURLY_RIGHT_SPACE = "{ ";
	private static final String SPACE_RIGHT_CURLY = " }";
	private static final String SPACED_COMMA = " : ";
	private static final String ARRAY_MARKER = LEFT_BRACKET;
	private static final String EMPTY = "";

	private final StringBuilder builder;

	public ToStringBuilder(String label, Object object) {
		this.builder = new StringBuilder();
		append(label, object);
	}

	public ToStringBuilder() {
		this.builder = new StringBuilder();
	}

	public ToStringBuilder append(String label, Object object) {
		if (builder.length() == 0) {
			builder.append(CURLY_RIGHT_SPACE);
		}
		if (StringUtils.isBlank(label)) {
			label = LABEL;
		}
		builder.append(label).append(SPACED_COMMA).append(withNullRepresentation(object));
		builder.append(COMMA_SPACE);
		return this;
	}

	private String withNullRepresentation(Object input) {
		StringBuilder b = new StringBuilder();
		if (input == null) {
			b.append(NULL1);
		} else if (input instanceof Collection) {
			String className = extractClassNameFromAnArray(((Collection) input).toArray());
			b.append(className);
			b.append(input.toString().replace(LEFT_BRACKET, LEFT_CURLY).replace(RIGHT_BRACKET, RIGHT_CURLY).replace(className, EMPTY));
			return b.toString();
		} else {
			String i = input.toString();
			if (i.startsWith(ARRAY_MARKER)) {
				Object[] objects = objectToArray(input);
				b.append(extractArrayDescription(objects, extractClassNameFromAnArray(objects)));
			} else {
				b.append(i);
			}
		}
		return b.toString().trim();
	}

	private Object[] objectToArray(Object input) {
		Object[] objects = null;
		int len = Array.getLength(input);
		if (len > 0) {
			objects = new Object[1];
			objects[0] = Array.get(input, 0);
		} else {
			objects = new Object[] {};
		}
		return objects;
	}

	private String extractArrayDescription(Object[] input, String className) {
		StringBuilder b = new StringBuilder(className);
		b.append(LEFT_BRACKET);
		for (int i = 0; i < input.length; i++) {
			String in = input[i].toString();
			if (!in.contains(AT)) {
				b.append(EMPTY).append(in);
			} else {
				b.append(in.replace(className, EMPTY));
			}
			if (i < input.length - 1) {
				b.append(COMMA_SPACE);
			}
		}
		b.append(SPACE_RIGHT_BRACKET);
		return b.toString();
	}

	private String extractClassNameFromAnArray(Object[] array) {
		String arrStr = array.toString(); // NOSONAR - in this case it's part of what you actually get from toString()..
		if (arrStr.startsWith(LJAVA_LANG_OBJECT) && array.length > 0) {
			return array[0].getClass().getName();
		} else {
			String apa = arrStr.substring(2, arrStr.length());
			return apa.substring(0, apa.indexOf(';'));
		}
	}

	@Override
	public String doToString() {
		if (builder.length() > 0) {
			builder.delete(builder.length() - COMMA_SPACE.length(), builder.length());
			builder.append(SPACE_RIGHT_CURLY);
		}
		return builder.toString();
	}
}
