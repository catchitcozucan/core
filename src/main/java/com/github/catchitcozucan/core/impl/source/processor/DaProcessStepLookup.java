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

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.ANNOT_MAKESTEP_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.ANNOT_PROCESSSTATUS_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.CLASSFILE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.FILE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NL;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NONE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.ANNOT_COMPILEOPTIONS_JAVA_PATH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.TSYM;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.TYPE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.info;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.warn;

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

import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.CompileOptions;
import com.github.catchitcozucan.core.ProcessStatus;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.reflect.ReflectionUtils;

public class DaProcessStepLookup {

	private static final int ACTIVITIES_PER_COLUMN_DEFAULT = 3;
	private static final String PATH = "path";

	private DaProcessStepLookup() {
	}

	public static final String TMP_COMP_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testComp";

	public static Set<DaProcessStepSourceAppender> annotatedElementsToAppenders(Set<? extends Element> annotatatedElementsMakeStep, Set<? extends Element> annotatatedElementsProcessStatus, Set<? extends Element> annotatatedElementsBpmSchemeLocations) { // NOSONAR
		Set<DaProcessStepSourceAppender> appenders = new HashSet<>();
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
								int endIndex = tmpSource.indexOf(DaProcessStepConstants.HEADER_START_OLD) + DaProcessStepConstants.COMMENT_HEADER_END.length();
								if (endIndex > tmpSource.length()) {
									endIndex = tmpSource.lastIndexOf("//") + NL.length();
								}
								currentSource = tmpSource.substring(tmpSource.indexOf(DaProcessStepConstants.CHKSUMPREFIX), endIndex);
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
			Element elem = a;
			String annotationTypePath = elem.getAnnotationMirrors().get(0).getAnnotationType().toString();
			return isValidMethod(elem, annotationTypePath);
		}).forEach(annotatedElement -> {
			Element elem = annotatedElement;
			File classFile = (File) ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(elem, "sourcefile"), FILE);
			statusClassFileMap.put(elem.toString(), classFile);
		}); // SONAR..any good?

		Map<String, ClassPathActivitesPerColumn> bpmFolderPerProcessClass = new HashMap<>();
		annotatatedElementsBpmSchemeLocations.stream().filter(e -> {
			Element elem = e;
			String annotationTypePath = elem.getAnnotationMirrors().get(0).getAnnotationType().toString();
			return isValidMethod(elem, annotationTypePath);
		}).forEach(ee -> {
			File classSymbol = null;
			classSymbol = (File) ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(ee, CLASSFILE), FILE); // JDK-8
			if (classSymbol == null) {
				Object classSymbolObj = ReflectionUtils.getFieldValueSilent(ReflectionUtils.getFieldValueSilent(ee, CLASSFILE), PATH); // AFTER JDK-8
				classSymbol = new File(classSymbolObj.toString());
			}
			String completePath = classSymbol.getParent() + File.separator + ee.getAnnotation(CompileOptions.class).relativeBpmDirectoryPath();
			String mavenModulePath = ee.getAnnotation(CompileOptions.class).mavenModulePathToStatusEnumeration();
			if (mavenModulePath.equals(NONE)) {
				mavenModulePath = null;
			}
			String mavenRepoPath = ee.getAnnotation(CompileOptions.class).mavenRepoPath();
			if (mavenRepoPath.equals(NONE)) {
				mavenRepoPath = null;
			}
			boolean criteriaStateOnlyFailure = IO.looksLikeTrue(ee.getAnnotation(CompileOptions.class).criteriaStateOnlyFailure());
			boolean acceptFailures = IO.looksLikeTrue(ee.getAnnotation(CompileOptions.class).acceptStatusEvaluationFailures());
			String activitesPerColumnStr = ee.getAnnotation(CompileOptions.class).bpmActivitiesPerColumn();
			Integer activitiesPerColumn = ACTIVITIES_PER_COLUMN_DEFAULT;
			try {
				activitiesPerColumn = Integer.parseInt(activitesPerColumnStr);
				if (activitiesPerColumn < 0) {
					warn(String.format("ProcessBpmSchemeRepo.activitiesPerColumn was specifed as : %s which is below zero. Failing back to %d", activitesPerColumnStr, activitiesPerColumn));
				} else if (activitiesPerColumn > 666) {
					activitiesPerColumn = 666;
					warn(String.format("ProcessBpmSchemeRepo.activitiesPerColumn was specifed as  ridiculously high : %s. Failing back to %d", activitesPerColumnStr, activitiesPerColumn));
				}
			} catch (NumberFormatException e) {
				warn(String.format("ProcessBpmSchemeRepo.activitiesPerColumn was specifed as a non-parsable number : %s. Failing back to %d", activitesPerColumnStr, activitiesPerColumn));
			}
			info(String.format("Trying to use or create BPM repo folder : %s", completePath));
			try {
				IO.makeOrUseDir(completePath);
				bpmFolderPerProcessClass.put(ee.toString(), new ClassPathActivitesPerColumn(activitiesPerColumn, new File(completePath), mavenModulePath, mavenRepoPath, criteriaStateOnlyFailure, acceptFailures));
			} catch (RuntimeException e) {
				DaProcessStepConstants.error(ee, "Relativepath for @%s defined as %s in %s could not be created/used", CompileOptions.class.getSimpleName(), ee.toString(), completePath);
				return;
			}
		});

		appenders.stream().forEach(a -> {
			Optional<String> matched = statusClassFileMap.keySet().stream().filter(classSymbol -> a.matchStatusClass(classSymbol)).findFirst();  //NOSONAR
			if (matched.isPresent() && !bpmFolderPerProcessClass.isEmpty()) {
				a.setJavaScrFileForStatusClass(statusClassFileMap.get(matched.get()));
			}
			setBpmparams(bpmFolderPerProcessClass, a);
		});
		return appenders;
	}

	private static void setBpmparams(Map<String, ClassPathActivitesPerColumn> bpmFolderPerProcessClass, DaProcessStepSourceAppender a) {
		if (!bpmFolderPerProcessClass.isEmpty() && bpmFolderPerProcessClass.get(a.toString()) != null) {
			File bpmRepoFolder = bpmFolderPerProcessClass.get(a.toString()).pathToFile;
			if (bpmRepoFolder != null) {
				a.setBpmRepoFolder(bpmRepoFolder);
				a.setBpmActivitiesPerColumn(bpmFolderPerProcessClass.get(a.toString()).acivitiesPercolumn);
				a.setMavenModulePath(bpmFolderPerProcessClass.get(a.toString()).mavenModulePath);
				a.setMavenRepoPath(bpmFolderPerProcessClass.get(a.toString()).mavenRepoPath);
				a.setCriteriaStateOnlyFailure(bpmFolderPerProcessClass.get(a.toString()).criteriaStateOnlyFailure);
				a.setAcceptEnumFailures(bpmFolderPerProcessClass.get(a.toString()).acceptFailures);
			}
		}
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


			if (((ExecutableElement) item).getParameters() != null && !((ExecutableElement) item).getParameters().isEmpty()) {
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
		} else if (annotationTypePath.equals(ANNOT_COMPILEOPTIONS_JAVA_PATH)) {
			if (!item.getKind().equals(ElementKind.CLASS)) {
				DaProcessStepConstants.error(item, "Only classes can be annotated with @%s", CompileOptions.class.getSimpleName());
				return false;
			}
			if (item.getModifiers().contains(Modifier.ABSTRACT)) {
				DaProcessStepConstants.error(item, "The type %s is abstract. You can't annotate abstract types with @s%", item.getSimpleName(), CompileOptions.class.getSimpleName());
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

	static final class ClassPathActivitesPerColumn {
		final Integer acivitiesPercolumn;
		final File pathToFile;
		final String mavenModulePath;
		final String mavenRepoPath;
		final boolean criteriaStateOnlyFailure;
		final boolean acceptFailures;
		public ClassPathActivitesPerColumn(Integer acivitiesPercolumn, File pathToFile, String mavenModulePath, String mavenRepoPath, boolean criteriaStateOnlyFailure, boolean acceptFailures) {
			this.acivitiesPercolumn = acivitiesPercolumn;
			this.pathToFile = pathToFile;
			this.mavenModulePath = mavenModulePath;
			this.mavenRepoPath = mavenRepoPath;
			this.criteriaStateOnlyFailure = criteriaStateOnlyFailure;
			this.acceptFailures = acceptFailures;
		}

	}
}
