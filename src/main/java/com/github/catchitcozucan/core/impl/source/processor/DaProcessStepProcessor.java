package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.error;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.github.catchitcozucan.core.ProcessStatus;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.internal.util.MD5Digest;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.util.ClassAnnotationUtil;
import com.github.catchitcozucan.core.util.MavenWriter;

@SupportedAnnotationTypes({ "MakeStep" })
public class DaProcessStepProcessor extends AbstractProcessor {

	private static final String MAKE_STEP_ISSUES = "MakeStep issues : ";
	private static final String STATUS_UPON_FAILURE = "statusUponFailure";
	private static final String STATUS_UPON_SUCCESS = "statusUponSuccess";
	private static final String DESCRIPTION = "description";
	private static final String PROCESS_NAME = "processName";
	private static final String ENUM_PATH = "enumStateProvider";
	private static final String PACKAGE = "package";
	private static final String IMPORT = "import";
	private static final String SEMI_COLON = ";";
	private Set<String> hasRecievedCommentHeader;

	public DaProcessStepProcessor() {
		super();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		MavenWriter.getInstance(processingEnv.getMessager());
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) { //NOSONAR

		if (roundEnv.processingOver()) {
			DaProcessStepConstants.info("DaProcessStepProcessor finished.");
			IO.deleteRecursively(new File(DaProcessStepLookup.TMP_COMP_PATH));
			return true;
		}
		Set<? extends Element> elementsToWorkMakeStep = roundEnv.getElementsAnnotatedWith(MakeStep.class);
		Set<? extends Element> elementsToWorkProcess = roundEnv.getElementsAnnotatedWith(ProcessStatus.class);
		if (elementsToWorkMakeStep.isEmpty()) {
			return true;
		}

		DaProcessStepConstants.info("DaProcessStepProcessor processing RoundEnvironment..");
		Set<DaProcessStepSourceAppender> appenders = DaProcessStepLookup.annotatedElementsToAppenders(elementsToWorkMakeStep, elementsToWorkProcess);
		if (appenders.isEmpty()) {
			DaProcessStepConstants.info("Lookup finished : we have nothing to work, no annotated classes found..");
			return true;
		} else {
			DaProcessStepConstants.info(String.format("Lookup finished. Found %d classes containing MakeSteps..", appenders.size()));
		}

		Set<DaProcessStepSourceAppender> appendersWorked = new HashSet<>();
		hasRecievedCommentHeader = new HashSet<>();

		appenders.stream().forEachOrdered(sourceAppender -> {
			DaProcessStepConstants.info("Working appender : " + sourceAppender.toString());
			sourceAppender.clear();
			if (!appendersWorked.contains(sourceAppender)) {
				workAppenderElements(sourceAppender, new ArrayList<Integer>());
				appendersWorked.add(sourceAppender);
			}
		});
		return true;
	}

	private void appendPossibleErrors(StringBuilder errors, String value, String label) {
		if (!IO.hasContents(value)) {
			errors.append(String.format("%s is missing", label)).append(MavenWriter.MESSAGE_SEPARATOR);
		}
	}

	private void testLoadStatuses(StringBuilder errors, String enumProvider, String statusUponSuccess, String statusUponFailure, DaProcessStepSourceAppender appender) { //NOSONAR
		boolean goOnEvaluating = true;

		if (!statusUponSuccess.contains(".")) {
			String perhapsCorrectClasspath = enumProvider.substring(0, enumProvider.lastIndexOf(DaProcessStepConstants.DOT));
			errors.append(String.format("specified statusUponSuccess %s has to point to an enum field _WITHIN_ the appointed class : use format <enum>.<enum-value>. Also, and following this convention, double check whether the path to your enum-providing class is in fact %s rather then provided %s", statusUponSuccess, perhapsCorrectClasspath, enumProvider)).append(MavenWriter.MESSAGE_SEPARATOR);
			goOnEvaluating = false;
		}
		if (!statusUponFailure.contains(".")) {
			String perhapsCorrectClasspath = enumProvider.substring(0, enumProvider.lastIndexOf(DaProcessStepConstants.DOT));
			errors.append(String.format("specified statusUponFailure %s has to point to an enum field _WITHIN_ the appointed class : use format <enum>.<enum-value>. Also, and following this convention, double check whether the path to your enum-providing class is in fact %s rather then provided %s", statusUponFailure, perhapsCorrectClasspath, enumProvider)).append(MavenWriter.MESSAGE_SEPARATOR);
			goOnEvaluating = false;
		}

		if (goOnEvaluating) {

			String className = enumProvider;
			DaProcessStepConstants.info(String.format("    Trying to load %s", className));
			DaProcessStepConstants.info("    @ProcessStatus class for @MakeStep validation.");
			Class<?> statusClass = null;
			String basePackage = className.substring(0, className.indexOf(DaProcessStepConstants.DOT));
			try {
				statusClass = getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e1) {
				DaProcessStepConstants.info("         *  Tried local classloader.");
				try {
					statusClass = Thread.currentThread().getContextClassLoader().loadClass(className);
				} catch (ClassNotFoundException e2) {
					DaProcessStepConstants.info("         *  Tried local current thread classloader.");
					try {
						statusClass = new CustomClassLoader(basePackage, getClass().getClassLoader()).loadClass(className);
					} catch (ClassNotFoundException e3) {
						DaProcessStepConstants.info("         *  Tried custom classloader.");
						try {
							statusClass = new CustomClassLoader(basePackage, Thread.currentThread().getContextClassLoader()).loadClass(className);
						} catch (ClassNotFoundException e4) {
							DaProcessStepConstants.info("         *  Tried custom classloader based on current thread.");
						}
					}
				}
			}

			if (appender.hasMatchedJavaScrFileForStatusClass()) {
				statusClass = loadStatusClassBySheerForceTmpCompileFromFile(appender.getJavaScrFileForStatusClass());
				if (statusClass != null) {
					DaProcessStepConstants.info(String.format("            Loaded class %s via on-the-fly compile.", statusClass.getSimpleName()));
				} else {
					DaProcessStepConstants.info("         *  Tried on-the-fly compile.");
				}
			}

			if (statusClass == null) {
				errors.append(String.format("specified enumProvider class %s could not be loaded - does it exist?", enumProvider)).append(MavenWriter.MESSAGE_SEPARATOR);
				goOnEvaluating = false;
			} else {
				DaProcessStepConstants.info(String.format("    Class %s is loaded. Inspecting status enum providers..", statusClass.getName()));
				CustomClassLoader.EnumContainer enums = new CustomClassLoader.EnumContainer(statusClass);

				if (!enums.isSane()) {
					errors.append(String.format("specified enumProvider class was not a good subject for process statuses : %s", enums.getProblemDescription())).append(MavenWriter.MESSAGE_SEPARATOR);
					goOnEvaluating = false;
				}
				if (goOnEvaluating) {
					if (enums.values() == null || enums.values().length == 0) {
						errors.append(String.format("specified enumProvider class %s does not contain any status enums whatsoever", enumProvider)).append(MavenWriter.MESSAGE_SEPARATOR);
						goOnEvaluating = false;
					}
					if (goOnEvaluating) {
						if ((enums.getClassName().contains(".") && !enums.getClassName().equals(enumProvider)) || !enumProvider.endsWith(enums.getClassName())) {
							errors.append(String.format("specified enumProvider class %s does not contain enum field %s defined as statusUponSuccess", enumProvider, statusUponSuccess)).append(MavenWriter.MESSAGE_SEPARATOR);
							goOnEvaluating = false;
						}
						if (goOnEvaluating) {
							Optional<CustomClassLoader.Nameable> sucessField = Arrays.stream(enums.values()).filter(f -> statusUponSuccess.contains(f.name())).findFirst();
							Optional<CustomClassLoader.Nameable> failureField = Arrays.stream(enums.values()).filter(f -> statusUponFailure.contains(f.name())).findFirst();
							if (!sucessField.isPresent()) {
								errors.append(String.format("specified enumProvider class %s does not contain field %s defined as statusUponSuccess", enumProvider, statusUponSuccess)).append(MavenWriter.MESSAGE_SEPARATOR);
								goOnEvaluating = false;
							}
							if (!failureField.isPresent()) {
								errors.append(String.format("specified enumProvider class %s does not contain field %s defined as statusUponFailure", enumProvider, statusUponFailure)).append(MavenWriter.MESSAGE_SEPARATOR);
								goOnEvaluating = false;
							}
							if (goOnEvaluating) {
								String statusUponSuccessShort = statusUponSuccess.substring(statusUponSuccess.indexOf(DaProcessStepConstants.DOT) + 1);
								String statusUponFailureShort = statusUponFailure.substring(statusUponFailure.indexOf(DaProcessStepConstants.DOT) + 1);
								Optional<CustomClassLoader.Nameable> locatedFailureEnum = Arrays.stream(enums.values()).filter(o -> o.name().equals(statusUponFailureShort)).findFirst();
								Optional<CustomClassLoader.Nameable> locatedSuccessEnum = Arrays.stream(enums.values()).filter(o -> o.name().equals(statusUponSuccessShort)).findFirst();
								if (!locatedFailureEnum.isPresent()) {
									errors.append(String.format("statusUponFailure %s enum does not exist within %s in class %s", statusUponFailureShort, enums.getClassName(), statusClass.getName())).append(MavenWriter.MESSAGE_SEPARATOR);
								}
								if (!locatedSuccessEnum.isPresent()) {
									errors.append(String.format("statusUponSuccess %s enum does not exist within %s in class %s", statusUponSuccessShort, enums.getClassName(), statusClass.getName())).append(MavenWriter.MESSAGE_SEPARATOR);
								}
							}
						}
					}
				}
			}
		}
		if (goOnEvaluating) {
			DaProcessStepConstants.info(String.format("    Successfully finished inspecting Makestep enum definitions based on class %s!", enumProvider));
		} else {
			DaProcessStepConstants.error(String.format("    Failed inspecting Makestep enum definitions based on class %s!", enumProvider));
		}
	}


	private void workAppenderElements(DaProcessStepSourceAppender sourceAppender, List<Integer> chkSumIndex) { //NOSONAR
		sourceAppender.getElementsToWork().stream().forEachOrdered(elementToWork -> {

			String statusUponFailure = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).statusUponFailure();
			String statusUponSuccess = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).statusUponSuccess();
			String description = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).description();
			String encoding = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).sourceEncoding();
			String processName = sourceAppender.getOriginatingClassShort();
			String enumStateProvider = ClassAnnotationUtil.getValueOverTypeMirror(() -> elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).enumStateProvider());

			if (IO.hasContents(encoding) && !encoding.equals(DaProcessStepConstants.NONE)) {
				try {
					sourceAppender.setSourceEncoding(Charset.forName(encoding));
				} catch (Exception e) {
					DaProcessStepConstants.error(String.format("You have specified an encoding we simply cannot load : %s", encoding));
					return;
				}
			}

			StringBuilder errors = new StringBuilder(MAKE_STEP_ISSUES);
			appendPossibleErrors(errors, statusUponFailure, STATUS_UPON_FAILURE);
			appendPossibleErrors(errors, statusUponSuccess, STATUS_UPON_SUCCESS);
			appendPossibleErrors(errors, description, DESCRIPTION);
			appendPossibleErrors(errors, processName, PROCESS_NAME);
			appendPossibleErrors(errors, enumStateProvider, ENUM_PATH);

			testLoadStatuses(errors, enumStateProvider, statusUponSuccess, statusUponFailure, sourceAppender);

			if (errors.length() > MAKE_STEP_ISSUES.length()) {
				errors.delete(errors.length() - MavenWriter.MESSAGE_SEPARATOR.length(), errors.length());
				DaProcessStepConstants.error(MavenWriter.formattedErrors(errors.toString()));
				throw new ProcessRuntimeException("Could not build due to Makestep issues");
			} else {
				chkSumIndex.add(new StringBuilder(IO.hasContents(statusUponFailure) ? statusUponFailure : "null").append(IO.hasContents(statusUponSuccess) ? statusUponSuccess : "null").append(IO.hasContents(description) ? description : "null").append(IO.hasContents(enumStateProvider) ? enumStateProvider : "null").toString().hashCode());

				// add method
				DaProcessStepConstants.info(String.format("    Step evaluated successfully : %s, enumpath  : %s, statusUponSuccess : %s, statusUponFailure : %s, description : \"%s\" ", elementToWork.getMethodName(), enumStateProvider, statusUponSuccess, statusUponFailure, description));
				addMethod(sourceAppender, elementToWork.getMethodName().replace("()", ""), enumStateProvider, statusUponSuccess, statusUponFailure, description, processName);
			}
		});

		sourceAppender.append(DaProcessStepConstants.THEEND, null);
		Collections.sort(chkSumIndex);
		StringBuilder chkSumBasis = new StringBuilder();
		chkSumIndex.stream().forEach(i -> chkSumBasis.append(i));  //NOSONAR
		sourceAppender.setChkSum(MD5Digest.getInstance().digestMessage(chkSumBasis.toString()));
		sourceAppender.doAppend();
		DaProcessStepConstants.info(String.format("    DaProcessStepProcessor finished with %s", sourceAppender.toString()));
	}

	private void addMethod(DaProcessStepSourceAppender sourceAppender, String toCall, String enumPath, String statusUponSuccess, String statusUponFailure, String description, String processName) {
		String instanceName = new StringBuilder(toCall).append(DaProcessStepConstants.STEP).toString();
		String body = String.format(DaProcessStepConstants.BODY, toCall, processName, description, enumPath, statusUponSuccess, enumPath, statusUponFailure);
		writeSourceFile(sourceAppender, body, instanceName);
	}

	private void writeSourceFile(DaProcessStepSourceAppender sourceAppender, String body, String instanceName) {
		if (!hasRecievedCommentHeader.contains(sourceAppender.getSrcFile().getAbsolutePath())) {
			sourceAppender.append(DaProcessStepConstants.COMMENT_HEADER, DaProcessStepConstants.COMMENT_HEADER_SIGN);
			hasRecievedCommentHeader.add(sourceAppender.getSrcFile().getAbsolutePath());
		}
		String significantPart = new StringBuilder(DaProcessStepConstants.SIGN_PART).append(instanceName).toString();
		String source = new StringBuilder(DaProcessStepConstants.NL).append(significantPart).append(" = ").append(body).toString();
		sourceAppender.append(source, significantPart);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return DaProcessStepConstants.NEN_BLACK_PROCESS_MAKESTEP_SUPPORTED_TYPES;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	private static Class<?> loadStatusClassBySheerForceTmpCompileFromFile(File file) {
		File tmpDir = new File(DaProcessStepLookup.TMP_COMP_PATH);
		boolean tmpDirIsAvail = true;
		if (!tmpDir.exists() && !tmpDir.mkdirs()) {
			DaProcessStepConstants.warn("Could not make tmp compile folder..");
			tmpDirIsAvail = false;
		}
		String className = "unknown"; //NOSONAR it is not pointless, does not compile otherwise...
		if (tmpDirIsAvail) {
			// package decl has to be removed
			String packageDecl = "";
			try {
				String source = IO.fileToString(file.getAbsolutePath(), DaProcessStepConstants.UTF8_CHARSET);
				if (source.contains(PACKAGE)) {
					int startIndex = source.indexOf(PACKAGE);
					int stopIndex = source.indexOf(SEMI_COLON, startIndex) + 1;
					packageDecl = source.substring(startIndex, stopIndex);
					source = source.replace(packageDecl, DaProcessStepConstants.EMPTY);
				}

				// We shall allow only ONE import, that is, of the
				// ProcessStatus annotation itself - NOTHING else
				// shall be accepted.
				int importIndex = source.indexOf(IMPORT);
				int stopIndex = source.indexOf(SEMI_COLON, importIndex) + 1;
				String importDecl = source.substring(importIndex, stopIndex);
				boolean processStatusImportFound = importDecl.contains(DaProcessStepConstants.ANNOT_PROCESSSTATUS_JAVA_PATH);
				if (!processStatusImportFound || source.indexOf(IMPORT, (importIndex + importDecl.length())) > -1) {
					DaProcessStepConstants.error("Your status class cannot contain ANY other import that of ths ProcessStatus annotation. It is not allowed.");
					return null;
				}
				source = source.replace(importDecl, "").replace("@ProcessStatus", "");
				file = new File(DaProcessStepLookup.TMP_COMP_PATH + File.separator + file.getName());
				Files.write(file.toPath(), source.getBytes(IO.DEF_ENCODING));  //NOSONAR
			} catch (IOException e) {
				DaProcessStepConstants.warn(String.format("Could not prepare file %s for compilation", file.getAbsolutePath()));
				return null;
			}
			try {
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				compiler.run(null, null, null, file.getAbsolutePath());
				String compiledName = file.getName().replace("java", "class");
				className = compiledName.replace(".class", "");
				File compiled = new File(file.getAbsolutePath().replace("java", "class"));
				URL classUrl = compiled.getParentFile().toURI().toURL();
				URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
				return Class.forName(className, true, classLoader);
			} catch (IOException ignore) {
				DaProcessStepConstants.warn(String.format("Could not test compile file for class %s due to IO-issues..", className));
			} catch (ClassNotFoundException e) {
				DaProcessStepConstants.warn(String.format("Could not load tmp compiled file for class %s..", className));
			}
		}
		return null;
	}
}
