/**
 * Original work by Ola Aronsson 2020
 * Courtesy of nollettnoll AB &copy; 2012 - 2020
 * <p>
 * Licensed under the Creative Commons Attribution 4.0 International (the "License")
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * https://creativecommons.org/licenses/by/4.0/
 * <p>
 * The software is provided “as is”, without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other liability,
 * whether in an action of contract, tort or otherwise, arising from, out of or
 * in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl.source.processor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.catchitcozucan.core.internal.util.io.IO;

public class EnumContainer {
	public static final String DOLLAR_SIGN = "$";
	public static final String PROCESS_STATUS_ENUM_PROVIDER_BASED_ON_S_HAS_ISSUES = "Process status enum provider based on %s has issues : ";
	public static final String PROPER_CLASSNAME_COULD_NOT_BE_DEDUCTED = "proper classname could not be deducted, ";
	public static final String ENUN_FIELD_NAME_COULD_NOT_BE_DEDUCTED = "enun field name could not be deducted";
	private final Nameable[] enums;
	private final String className;
	private final String enumFieldName;
	private final Class<?> sourceInspectionClass;
	private final String originatingClassPath;

	public EnumContainer(Class<?> enumCorrier, String originatingClassPath) {
		sourceInspectionClass = enumCorrier;
		this.originatingClassPath = originatingClassPath;
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
		this.enumFieldName = rawClassName.substring(rawClassName.indexOf(DOLLAR_SIGN) + 1); //NOSONAR
		this.className = rawClassName.substring(0, rawClassName.indexOf(DOLLAR_SIGN)); // NOSONAR
	}

	public Nameable[] values() {
		return enums;
	}

	public String getClassName() {
		return className;
	}

	public String getOriginatingClassPath() {
		return originatingClassPath;
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
			StringBuilder err = new StringBuilder(String.format(PROCESS_STATUS_ENUM_PROVIDER_BASED_ON_S_HAS_ISSUES, sourceInspectionClass.getName()));
			if (!IO.hasContents(className)) {
				err.append(PROPER_CLASSNAME_COULD_NOT_BE_DEDUCTED);
			}
			if (!IO.hasContents(enumFieldName)) {
				err.append(ENUN_FIELD_NAME_COULD_NOT_BE_DEDUCTED);
			}
			if (err.toString().endsWith(", ")) {
				err.delete(err.length() - 2, err.length());
			}
			return err.toString();
		}
	}

	public static Nameable[] enumsToNameableArray(Enum[] enums) {
		if (enums == null) {
			return null; //NOSONAR
		}
		if (enums.length == 0) {
			return new Nameable[] {};
		}

		Nameable[] nameables = new Nameable[enums.length];
		AtomicInteger index = new AtomicInteger(0);
		Arrays.stream(enums).forEach(e -> nameables[index.getAndIncrement()] = () -> e.name()); //NOSONAR
		return nameables;
	}
}
