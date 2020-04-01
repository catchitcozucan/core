package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.ANNOT_MAKESTEP_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.ANNOT_PROCESSSTATUS_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.CLASSFILE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.FILE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.PROCESSBPMSCHEMEREPO_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.TSYM;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.TYPE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.info;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import com.github.catchitcozucan.core.ProcessBpmSchemeRepo;
import com.github.catchitcozucan.core.ProcessStatus;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.reflect.ReflectionUtils;

public class DaProcessStepLookup {

	private DaProcessStepLookup() {}

	public static final String TMP_COMP_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testComp";

	public static Set<DaProcessStepSourceAppender> annotatedElementsToAppenders(Set<? extends Element> annotatatedElementsMakeStep, Set<? extends Element> annotatatedElementsProcessStatus, Set<? extends Element> annotatatedElementsBpmSchemeLocations) { // NOSONAR
		Set<DaProcessStepSourceAppender> appenders = new HashSet<>();
		Set<Object> classSymbols = new HashSet<>();
		annotatatedElementsMakeStep.stream().forEach(annotatedElement -> {

			ExecutableElement toExec = (ExecutableElement) annotatedElement;
			String annotationTypePath = toExec.getAnnotationMirrors().get(0).getAnnotationType().toString();
			if (isValidMethod(toExec, annotationTypePath)) {

				Object classSymbol = ReflectionUtils.getFieldValueSilent((ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(annotatedElement, DaProcessStepConstants.OWNER), TYPE)), TSYM);
				String originatingClass = ReflectionUtils.getFieldValueSilent(classSymbol, TYPE).toString();

				if (annotationTypePath.equals(ANNOT_MAKESTEP_JAVA_PATH)) {
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
					info(String.format("    Lookup : found step %s in %s", elementToWork.toString(), srcAppender.toString()));
					srcAppender.appendElementToWork(elementToWork);
				}
			}
		});

		Map<String, File> statusClassFileMap = new HashMap<>();
		annotatatedElementsProcessStatus.stream().filter(a -> {
			Element elem = (Element) a;
			String annotationTypePath = elem.getAnnotationMirrors().get(0).getAnnotationType().toString();
			return isValidMethod(elem, annotationTypePath);
		}).forEach(annotatedElement -> {
			Element elem = (Element) annotatedElement;
			File classFile = (File) ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(elem, "sourcefile"), FILE);
			statusClassFileMap.put(elem.toString(), classFile);
		}); // SONAR..any good?

		Map<String, File> bpmFolderPerProcessClass = new HashMap<>();
		annotatatedElementsBpmSchemeLocations.stream().filter(e -> {
			Element elem = (Element) e;
			String annotationTypePath = elem.getAnnotationMirrors().get(0).getAnnotationType().toString();
			return isValidMethod(elem, annotationTypePath);
		}).forEach(ee -> {
			File classSymbol = (File) ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(ee, CLASSFILE), FILE);
			String completePath = classSymbol.getParent() + File.separator + ee.getAnnotation(ProcessBpmSchemeRepo.class).relativePath();
			info(String.format("Trying to use or create BPM repo folder : %s", completePath));
			try {
				IO.makeOrUseDir(completePath);
				bpmFolderPerProcessClass.put(ee.toString(), new File(completePath));
			}catch (RuntimeException e){
				DaProcessStepConstants.error(ee, "Relativepath for @%s defined as %s in %s could not be created/used", ProcessBpmSchemeRepo.class.getSimpleName(), ee.toString(), completePath);
				return;
			}
		});

		appenders.stream().forEach(a -> {
			Optional<String> matched = statusClassFileMap.keySet().stream().filter(classSymbol -> a.matchStatusClass(classSymbol)).findFirst();  //NOSONAR
			if (matched.isPresent()) {
				a.setJavaScrFileForStatusClass(statusClassFileMap.get(matched.get()));
			}
			File bpmRepoFolder = bpmFolderPerProcessClass.get(a.toString());
			if(bpmRepoFolder!=null) {
				a.setBpmRepoFolder(bpmRepoFolder);
			}
		});
		return appenders;
	}

	private static boolean isValidMethod(Element item, String annotationTypePath) { //NOSONAR

		if (annotationTypePath.equals(ANNOT_MAKESTEP_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.METHOD)) {
				DaProcessStepConstants.error(item, "Only methods can be annotated with @%s", MakeStep.class.getSimpleName());
				return false;
			}

			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The method %s is abstract. You can't annotate abstract methods with @s%", item.getSimpleName(), MakeStep.class.getSimpleName());
				return false;
			}


			if (((ExecutableElement)item).getParameters() != null && !((ExecutableElement)item).getParameters().isEmpty()) {
				DaProcessStepConstants.error(item, "The method %s takes arguments - you can't annotate methods with arguments with @s%", item.getSimpleName(), MakeStep.class.getSimpleName());
				return false;
			}
			return true;
		} else if (annotationTypePath.equals(ANNOT_PROCESSSTATUS_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.CLASS)) {
				DaProcessStepConstants.error(item, "Only classes can be annotated with @%s", ProcessStatus.class.getSimpleName());
				return false;
			}

			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The type %s is abstract. You can't annotate abstract types with @s%", item.getSimpleName(), ProcessStatus.class.getSimpleName());
				return false;
			}
			return true;
		} else if (annotationTypePath.equals(PROCESSBPMSCHEMEREPO_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.CLASS)) {
				DaProcessStepConstants.error(item, "Only classes can be annotated with @%s", ProcessBpmSchemeRepo.class.getSimpleName());
				return false;
			}
			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The type %s is abstract. You can't annotate abstract types with @s%", item.getSimpleName(), ProcessBpmSchemeRepo.class.getSimpleName());
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
		Object classFileRepresentation = ReflectionUtils.getFieldValueSilent(classSymbol, CLASSFILE);
		if (classFileRepresentation != null && classFileRepresentation.getClass().getName().equals(DaProcessStepConstants.COM_SUN_TOOLS_JAVAC_FILE_REGULAR_FILE_OBJECT)) {
			return (File) ReflectionUtils.getFieldValueSilent(classFileRepresentation, FILE);
		} else {
			return null; // what can we do?!
		}
	}
}
