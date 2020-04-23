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

package com.github.catchitcozucan.core.internal.util.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ReflectionUtils {

	private static final String NAME = "NAME ";
	private static final String COLON = ":";
	private static final String CLAZZ = "CLASS ";

	private ReflectionUtils() {}

	public static Field getFieldRecursivelySilentFromInstance(Object instance, String fieldName) {
		Field result = null;
		List<Class> orderedList = getAllClasses(instance);
		List<String> namez = new ArrayList<>();
		for (Class c : orderedList) {
			Arrays.stream(c.getDeclaredFields()).forEach(f -> namez.add(NAME + f.getName() + COLON + CLAZZ + f.getType().toString()));
			Field f = getFieldValueSilent(c, fieldName);
			if (f != null) {
				result = f;
				break;
			}
		}
		return result;
	}

	public static Object getFieldValueSilent(Object instance, String fieldName) {
		try {
			Field f = getFieldRecursivelySilentFromInstance(instance, fieldName);
			if (f == null) {
				return null;
			}
			f.setAccessible(true);
			return f.get(instance);
		} catch (IllegalAccessException ignore) {} //NOSONAR
		return null;
	}

	private static Field getFieldValueSilent(Class<?> clazz, String fieldName) {
		Field f = null;
		try {
			f = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ignore) {} // NOSONAR
		return f;
	}

	private static List<Class> getAllClasses(Object o) {
		List<Class> classList = new LinkedList<>();
		classList.add(o.getClass());
		Class superclass = o.getClass().getSuperclass();
		if (superclass != null) {
			classList.add(superclass);
			while (superclass != null) {
				Class clazz = superclass;
				superclass = clazz.getSuperclass();
				if (superclass != null) {
					classList.add(superclass);
				}
			}
		}
		return classList;
	}
}
