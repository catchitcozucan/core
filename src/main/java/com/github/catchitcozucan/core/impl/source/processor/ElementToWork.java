package com.github.catchitcozucan.core.impl.source.processor;

import javax.lang.model.element.Element;

import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;

public class ElementToWork extends BaseDomainObject {
	public static final String UNDERSCORE = "_";
	private final Element annotatedElement;
	private final Object classSymbol;
	private final String methodName;

	ElementToWork(Element annotatedElement, Object classSymbol, String methodName) {
		this.annotatedElement = annotatedElement;
		this.classSymbol = classSymbol;
		this.methodName = methodName;
	}

	Object getClassSymbol() {
		return classSymbol;
	}

	String getMethodName() {
		return methodName;
	}

	Element getAnnotatedElement() {
		return annotatedElement;
	}

	@Override
	public String doToString() {
		return new StringBuilder(classSymbol.toString()).append(UNDERSCORE).append(methodName).toString();
	}
}
