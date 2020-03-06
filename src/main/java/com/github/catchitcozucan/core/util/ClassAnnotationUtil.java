package com.github.catchitcozucan.core.util;

import javax.lang.model.type.MirroredTypesException;

public class ClassAnnotationUtil {

	@FunctionalInterface
	public interface GetClassValue {
		void execute();
	}

	public static String getValueOverTypeMirror(GetClassValue c) {
		try {
			c.execute();
		} catch (MirroredTypesException ex) {
			return ex.getTypeMirrors().get(0).toString();
		}
		return null;
	}
}
