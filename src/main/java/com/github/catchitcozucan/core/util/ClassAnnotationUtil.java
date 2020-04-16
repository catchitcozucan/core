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
