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
package com.github.catchitcozucan.core.impl.source.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.catchitcozucan.core.internal.util.io.IO;

public class CustomClassLoader extends ClassLoader {

	private final String basePackage;

	public CustomClassLoader(String basePackage, ClassLoader parent) {
		super(parent);
		this.basePackage = basePackage;
	}

	private Class<?> getClass(String name) {
		byte[] b = null;
		b = loadClassData(name);
		if (b != null) {
			Class<?> c = defineClass(name, b, 0, b.length);
			resolveClass(c);
			return c;
		} else {
			return null;
		}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.startsWith(basePackage)) {
			Class c = getClass(name);
			if (c != null) {
				return c;
			}
		}
		return super.loadClass(name);
	}

	private byte[] loadClassData(String fileName) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName.replace('.', File.separatorChar) + ".class");
		if (inputStream == null) {
			return null; // NOSONAR
		}
		byte[] buffer;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		int nextValue = 0;
		try {
			while ((nextValue = inputStream.read()) != -1) {
				byteStream.write(nextValue);
			}
		} catch (IOException e) {
			return null;  // NOSONAR
		}
		buffer = byteStream.toByteArray();
		return buffer;
	}

	public static class EnumContainer {
		private final Nameable[] enums;
		private final String className;
		private final String enumFieldName;
		private final Class sourceInspectionClass;

		public EnumContainer(Class<?> enumCorrier) {
			sourceInspectionClass = enumCorrier;
			Object[] enumRaws = enumCorrier.getEnumConstants();
			if (enumRaws == null || enumRaws.length == 0) {
				enumRaws = enumCorrier.getDeclaredClasses()[0].getEnumConstants();
			}
			enums = new Nameable[enumRaws.length];
			AtomicInteger index = new AtomicInteger(0);
			Arrays.stream(enumRaws).forEach(e -> {
				enums[index.get()] = () -> e.toString(); //NOSONAR
				index.incrementAndGet();
			});
			String rawClassName = enumRaws[0].getClass().getName();
			this.enumFieldName = rawClassName.substring(rawClassName.indexOf("$") + 1); //NOSONAR
			this.className = rawClassName.substring(0, rawClassName.indexOf("$")); // NOSONAR
		}

		public Nameable[] values() {
			return enums;
		}

		public String getClassName() {
			return className;
		}

		public String getEnumFieldName() {
			return enumFieldName;
		}

		public boolean isSane() {
			return IO.hasContents(className) && IO.hasContents(enumFieldName);
		}

		public String getProblemDescription() {
			if (isSane()) {
				return DaProcessStepConstants.EMPTY;
			} else {
				StringBuilder err = new StringBuilder(String.format("Process status enum provider based on %s has issues : ", sourceInspectionClass.getName()));
				if (!IO.hasContents(className)) {
					err.append("proper classname could not be deducted, ");
				}
				if (!IO.hasContents(enumFieldName)) {
					err.append("enun field name could not be deducted");
				}
				if (err.toString().endsWith(", ")) {
					err.delete(err.length() - 2, err.length());
				}
				return err.toString();
			}
		}
	}
}