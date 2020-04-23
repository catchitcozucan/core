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

package com.github.catchitcozucan.core.util;

import java.io.File;
import java.util.List;

import javax.lang.model.type.MirroredTypesException;

import com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants;
import com.github.catchitcozucan.core.internal.util.reflect.ReflectionUtils;

public class ClassAnnotationUtil {

	public static final String OOOOPS_COULD_NOT_EXTRACT_THE_STATUS_SOURCE_FILE = "Oooops. Could _not_ extract the status source file for %s..";

	@FunctionalInterface
	public interface GetClassValue {
		void execute();
	}

	public static ClasspathSourceFile getValueOverTypeMirror(GetClassValue c) {
		try {
			c.execute();
		} catch (MirroredTypesException ex) {
			String sourcePath = null;
			try {
				sourcePath = ex.getTypeMirrors().get(0).toString();
				File sourceFile = (File) ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(((List) ReflectionUtils.getFieldValueSilent(ex, DaProcessStepConstants.TYPES)).get(0), DaProcessStepConstants.TSYM), DaProcessStepConstants.CLASSFILE), DaProcessStepConstants.FILE);
				return new ClasspathSourceFile(sourceFile, sourcePath);
			} catch (Exception e) {
				DaProcessStepConstants.warn(String.format(OOOOPS_COULD_NOT_EXTRACT_THE_STATUS_SOURCE_FILE, sourcePath));
				return new ClasspathSourceFile(null, ex.getTypeMirrors().get(0).toString());
			}
		}
		return null;
	}

	public static class ClasspathSourceFile {
		final File file;
		final String classPath;

		public ClasspathSourceFile(File file, String classPath) {
			this.file = file;
			this.classPath = classPath;
		}

		public boolean hasClassFile() {
			return file != null;
		}

		public File getFile() {
			return file;
		}

		public String getClassPath() {
			return classPath;
		}
	}
}
