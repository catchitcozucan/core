package com.github.catchitcozucan.core.impl.source.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import com.github.catchitcozucan.core.ProcessStatus;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.reflect.ReflectionUtils;

public class DaProcessStepLookup {

	private DaProcessStepLookup() {}

	public static final String TMP_COMP_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testComp";

	public static Set<DaProcessStepSourceAppender> annotatedElementsToAppenders(Set<? extends Element> annotatatedElementsMakeStep, Set<? extends Element> annotatatedElementsProcessStatus) { // NOSONAR
		Set<DaProcessStepSourceAppender> appenders = new HashSet<>();
		Set<Object> classSymbols = new HashSet<>();
		annotatatedElementsMakeStep.stream().forEach(annotatedElement -> {

			ExecutableElement toExec = (ExecutableElement) annotatedElement;
			String annotationTypePath = toExec.getAnnotationMirrors().get(0).getAnnotationType().toString();
			if (isValidMethod(toExec, annotationTypePath)) {

				Object classSymbol = ReflectionUtils.getFieldValueSilent((ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(annotatedElement, DaProcessStepConstants.OWNER), DaProcessStepConstants.TYPE)), DaProcessStepConstants.TSYM);
				String originatingClass = ReflectionUtils.getFieldValueSilent(classSymbol, DaProcessStepConstants.TYPE).toString();

				if (annotationTypePath.equals(DaProcessStepConstants.ANNOT_MAKESTEP_JAVA_PATH)) {
					Optional<DaProcessStepSourceAppender> srcAppenderOptional = appenders.stream().filter(a -> a.getOriginatingClass().equals(originatingClass)).findFirst();
					DaProcessStepSourceAppender srcAppender = null;
					if (srcAppenderOptional.isPresent()) {
						srcAppender = srcAppenderOptional.get();
					}

					if (srcAppender == null) {
						File file = getFileViaSunClassesReflectionHack(classSymbol);
						// NOSONAR TODO - INTELLIJ GETS NULL IN HERE..!
						if (file == null) {
							return;
						}

						String currentSource = null;
						String headerStartToUser = DaProcessStepConstants.HEADER_START_NEW;
						try {
							String tmpSource = IO.fileToString(file.getAbsolutePath(), null);
							if (tmpSource.indexOf(DaProcessStepConstants.INTRO_TEXT) > -1) {
								currentSource = tmpSource.substring(tmpSource.indexOf(DaProcessStepConstants.CHKSUMPREFIX), tmpSource.indexOf(DaProcessStepConstants.HEADER_START_OLD) + DaProcessStepConstants.COMMENT_HEADER_END.length());
							}
							srcAppender = new DaProcessStepSourceAppender(file, originatingClass, currentSource, headerStartToUser);
							appenders.add(srcAppender);
						} catch (IOException e) {
							DaProcessStepConstants.error(annotatedElement, String.format("Could not readup sourcefile %s", file.getAbsolutePath()));
						}
					}

					// now append the thing to work per file!
					ElementToWork elementToWork = new ElementToWork(annotatedElement, classSymbol, annotatedElement.toString());
					DaProcessStepConstants.info(String.format("    Lookup : found step %s in %s", elementToWork.toString(), srcAppender.toString()));
					srcAppender.appendElementToWork(elementToWork);
				}
			}
		});

		annotatatedElementsProcessStatus.stream().forEach(annotatedElement -> classSymbols.add(ReflectionUtils.getFieldValueSilent((ReflectionUtils.getFieldValueSilent(annotatedElement, DaProcessStepConstants.TYPE)), DaProcessStepConstants.TSYM))); // SONAR..any good?

		appenders.stream().forEach(a -> {
			Optional<Object> matched = classSymbols.stream().filter(classSymbol -> a.matchStatusClass(classSymbol)).findFirst();  //NOSONAR
			if (matched.isPresent()) {
				a.setJavaScrFileForStatusClass(getFileViaSunClassesReflectionHack(matched.get()));
			}
		});
		return appenders;
	}

	private static boolean isValidMethod(ExecutableElement item, String annotationTypePath) { //NOSONAR

		if (annotationTypePath.equals(DaProcessStepConstants.ANNOT_MAKESTEP_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.METHOD)) {
				DaProcessStepConstants.error(item, "Only methods can be annotated with @%s", MakeStep.class.getSimpleName());
				return false;
			}

			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The method %s is abstract. You can't annotate abstract methods with @s%", item.getSimpleName(), MakeStep.class.getSimpleName());
				return false;
			}

			if (item.getParameters() != null && !item.getParameters().isEmpty()) {
				DaProcessStepConstants.error(item, "The method %s takes arguments - you can't annotate methods with arguments with @s%", item.getSimpleName(), MakeStep.class.getSimpleName());
				return false;
			}
			return true;
		} else if (annotationTypePath.equals(DaProcessStepConstants.ANNOT_PROCESSSTATUS_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.ANNOTATION_TYPE)) {
				DaProcessStepConstants.error(item, "Only classes can be annotated with @%s", ProcessStatus.class.getSimpleName());
				return false;
			}

			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The type %s is abstract. You can't annotate abstract types with @s%", item.getSimpleName(), ProcessStatus.class.getSimpleName());
				return false;
			}
			return true;
		} else {
			DaProcessStepConstants.info(String.format("Sent in annotation type %s will be ignored. We only process ", annotationTypePath));
			DaProcessStepConstants.NEN_BLACK_PROCESS_MAKESTEP_SUPPORTED_TYPES.stream().forEach(c -> DaProcessStepConstants.info("    *  " + c));
			return false;
		}
	}

	static File getFileViaSunClassesReflectionHack(Object classSymbol) {
		Object classFileRepresentation = ReflectionUtils.getFieldValueSilent(classSymbol, DaProcessStepConstants.CLASSFILE);
		if (classFileRepresentation != null && classFileRepresentation.getClass().getName().equals(DaProcessStepConstants.COM_SUN_TOOLS_JAVAC_FILE_REGULAR_FILE_OBJECT)) {
			return (File) ReflectionUtils.getFieldValueSilent(classFileRepresentation, DaProcessStepConstants.FILE);
		} else {
			return null; // what can we do?!
		}
	}
}
