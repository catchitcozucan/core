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

package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.UNDERSCORE;

import javax.lang.model.element.Element;

import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;

public class ElementToWork extends BaseDomainObject {
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
