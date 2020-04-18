package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.DOT;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.EMPTY;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.error;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.info;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.warn;
import static com.github.catchitcozucan.core.util.MavenWriter.MESSAGE_SEPARATOR;

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
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.ProcessBpmSchemeRepo;
import com.github.catchitcozucan.core.ProcessStatus;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.source.processor.bpm.BpmSchemeElementDescriptor;
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
	private static final String METHOD_NAME = "METHOD_NAME";
	private static final String PARENTHESIS = "()";
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
			info("DaProcessStepProcessor finished.");
			IO.deleteRecursively(new File(DaProcessStepLookup.TMP_COMP_PATH));
			return true;
		}
		Set<? extends Element> elementsToWorkMakeStep = roundEnv.getElementsAnnotatedWith(MakeStep.class);
		Set<? extends Element> elementsToWorkProcess = roundEnv.getElementsAnnotatedWith(ProcessStatus.class);
		Set<? extends Element> elementsToWorkBpmSchemeLocation = roundEnv.getElementsAnnotatedWith(ProcessBpmSchemeRepo.class);
		if (elementsToWorkMakeStep.isEmpty()) {
			return true;
		}

		info("DaProcessStepProcessor processing RoundEnvironment..");
		Set<DaProcessStepSourceAppender> appenders = DaProcessStepLookup.annotatedElementsToAppenders(elementsToWorkMakeStep, elementsToWorkProcess, elementsToWorkBpmSchemeLocation);
		if (appenders.isEmpty()) {
			info("Lookup finished : we have nothing to work, no annotated classes found..");
			return true;
		} else {
			info(String.format("Lookup finished. Found %d classes containing MakeSteps..", appenders.size()));
		}

		Set<DaProcessStepSourceAppender> appendersWorked = new HashSet<>();
		hasRecievedCommentHeader = new HashSet<>();

		appenders.stream().forEachOrdered(sourceAppender -> {
			info("Working appender : " + sourceAppender.toString());
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
			errors.append(String.format("%s is missing", label)).append(MESSAGE_SEPARATOR);
		}
	}

	private BpmSchemeElementDescriptor testLoadStatusesAndExctractBpmDescriptors(StringBuilder errors, String enumProvider, String statusUponSuccess, String statusUponFailure, DaProcessStepSourceAppender appender, String description, String processName) { //NOSONAR
		boolean goOnEvaluating = true;
		BpmSchemeElementDescriptor descriptor = null;
		if (!statusUponSuccess.contains(".")) {
			String perhapsCorrectClasspath = enumProvider.substring(0, enumProvider.lastIndexOf(DOT));
			errors.append(String.format("specified statusUponSuccess %s has to point to an enum field _WITHIN_ the appointed class : use format <enum>.<enum-value>. Also, and following this convention, double check whether the path to your enum-providing class is in fact %s rather then provided %s", statusUponSuccess, perhapsCorrectClasspath, enumProvider)).append(MESSAGE_SEPARATOR);
			goOnEvaluating = false;
		}
		if (!statusUponFailure.contains(".")) {
			String perhapsCorrectClasspath = enumProvider.substring(0, enumProvider.lastIndexOf(DOT));
			errors.append(String.format("specified statusUponFailure %s has to point to an enum field _WITHIN_ the appointed class : use format <enum>.<enum-value>. Also, and following this convention, double check whether the path to your enum-providing class is in fact %s rather then provided %s", statusUponFailure, perhapsCorrectClasspath, enumProvider)).append(MESSAGE_SEPARATOR);
			goOnEvaluating = false;
		}

		if (goOnEvaluating) {

			String className = enumProvider;
			info(String.format("    Trying to load %s", className));
			info("    @ProcessStatus class for @MakeStep validation.");
			Class<?> statusClass = null;
			String basePackage = className.substring(0, className.indexOf(DOT));
			try {
				statusClass = getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e1) {
				info("         *  Tried local classloader.");
				try {
					statusClass = Thread.currentThread().getContextClassLoader().loadClass(className);
				} catch (ClassNotFoundException e2) {
					info("         *  Tried local current thread classloader.");
					try {
						statusClass = new CustomClassLoader(basePackage, getClass().getClassLoader()).loadClass(className);
					} catch (ClassNotFoundException e3) {
						info("         *  Tried custom classloader.");
						try {
							statusClass = new CustomClassLoader(basePackage, Thread.currentThread().getContextClassLoader()).loadClass(className);
						} catch (ClassNotFoundException e4) {
							info("         *  Tried custom classloader based on current thread.");
						}
					}
				}
			}

			if (appender.hasMatchedJavaScrFileForStatusClass()) {
				statusClass = loadStatusClassBySheerForceTmpCompileFromFile(appender.getJavaScrFileForStatusClass());
				if (statusClass != null) {
					info(String.format("            Loaded class %s via on-the-fly compile.", statusClass.getSimpleName()));
				} else {
					info("         *  Tried on-the-fly compile.");
				}
			}

			if (statusClass == null) {
				warn(String.format("specified enumProvider class %s could not be loaded - does it exist?", enumProvider));
			} else {
				info(String.format("    Class %s is loaded. Inspecting status enum providers..", statusClass.getName()));
				CustomClassLoader.EnumContainer enums = new CustomClassLoader.EnumContainer(statusClass);

				if (!enums.isSane()) {
					errors.append(String.format("specified enumProvider class was not a good subject for process statuses : %s", enums.getProblemDescription())).append(MESSAGE_SEPARATOR);
					goOnEvaluating = false;
				}
				if (goOnEvaluating) {
					if (enums.values() == null || enums.values().length == 0) {
						errors.append(String.format("specified enumProvider class %s does not contain any status enums whatsoever", enumProvider)).append(MESSAGE_SEPARATOR);
						goOnEvaluating = false;
					}
					if (goOnEvaluating) {
						if ((enums.getClassName().contains(".") && !enums.getClassName().equals(enumProvider)) || !enumProvider.endsWith(enums.getClassName())) {
							errors.append(String.format("specified enumProvider class %s does not contain enum field %s defined as statusUponSuccess", enumProvider, statusUponSuccess)).append(MESSAGE_SEPARATOR);
							goOnEvaluating = false;
						}
						if (goOnEvaluating) {
							Optional<CustomClassLoader.Nameable> sucessField = Arrays.stream(enums.values()).filter(f -> statusUponSuccess.contains(f.name())).findFirst();
							Optional<CustomClassLoader.Nameable> failureField = Arrays.stream(enums.values()).filter(f -> statusUponFailure.contains(f.name())).findFirst();
							if (!sucessField.isPresent()) {
								errors.append(String.format("specified enumProvider class %s does not contain field %s defined as statusUponSuccess", enumProvider, statusUponSuccess)).append(MESSAGE_SEPARATOR);
								goOnEvaluating = false;
							}
							if (!failureField.isPresent()) {
								errors.append(String.format("specified enumProvider class %s does not contain field %s defined as statusUponFailure", enumProvider, statusUponFailure)).append(MESSAGE_SEPARATOR);
								goOnEvaluating = false;
							}
							if (goOnEvaluating) {
								String statusUponSuccessShort = statusUponSuccess.substring(statusUponSuccess.indexOf(DOT) + 1);
								String statusUponFailureShort = statusUponFailure.substring(statusUponFailure.indexOf(DOT) + 1);
								Optional<CustomClassLoader.Nameable> locatedFailureEnum = Arrays.stream(enums.values()).filter(o -> o.name().equals(statusUponFailureShort)).findFirst();
								Optional<CustomClassLoader.Nameable> locatedSuccessEnum = Arrays.stream(enums.values()).filter(o -> o.name().equals(statusUponSuccessShort)).findFirst();
								if (!locatedFailureEnum.isPresent()) {
									errors.append(String.format("statusUponFailure %s enum does not exist within %s in class %s", statusUponFailureShort, enums.getClassName(), statusClass.getName())).append(MESSAGE_SEPARATOR);
								}
								if (!locatedSuccessEnum.isPresent()) {
									errors.append(String.format("statusUponSuccess %s enum does not exist within %s in class %s", statusUponSuccessShort, enums.getClassName(), statusClass.getName())).append(MESSAGE_SEPARATOR);
								}
							}
						}
					}
				}
				// let's generate the BPM2 scheme element...
				if (goOnEvaluating) {
					descriptor = extractDescriptor(statusUponFailure, statusUponSuccess, description, enums, processName);
				}
			}
		}
		if (goOnEvaluating) {
			info(String.format("    Successfully finished inspecting Makestep enum definitions based on class %s!", enumProvider));
		} else {
			DaProcessStepConstants.error(String.format("    Failed inspecting Makestep enum definitions based on class %s!", enumProvider));
		}
		return descriptor;
	}

	private BpmSchemeElementDescriptor extractDescriptor(String statusUponFailure, String statusUponSuccess, String description, CustomClassLoader.EnumContainer enums, String processName) {
		String statusUponFailureShort = statusUponFailure.substring(statusUponFailure.indexOf(".") + 1);
		String statusUponSuccessShort = statusUponSuccess.substring(statusUponSuccess.indexOf(".") + 1);
		AtomicInteger index = new AtomicInteger(-1);
		AtomicInteger found = new AtomicInteger(-1);
		Arrays.stream(enums.values()).forEachOrdered(e -> {
			if (found.get() < 0 && !e.name().equals(statusUponFailureShort)) {
				index.incrementAndGet();
			} else if (found.get() < 0) {
				found.incrementAndGet();
			}
		});
		int indexCurrentState = index.get();
		String myStateName = enums.values()[indexCurrentState].name();
		BpmSchemeElementDescriptor.Type typeBefore;
		BpmSchemeElementDescriptor.Type typeAfter;
		if (indexCurrentState == 0) {
			typeBefore = BpmSchemeElementDescriptor.Type.StartEvent;
			typeAfter = BpmSchemeElementDescriptor.Type.Activity;
		} else if (indexCurrentState == enums.values().length - 3) {
			typeBefore = BpmSchemeElementDescriptor.Type.Activity;
			typeAfter = BpmSchemeElementDescriptor.Type.FINISH_STATE;
		} else {
			typeBefore = BpmSchemeElementDescriptor.Type.Activity;
			typeAfter = BpmSchemeElementDescriptor.Type.Activity;
		}
		// @formatter:off
		return BpmSchemeElementDescriptor.builder()
				.statusUponFailure(statusUponFailureShort)
				.statusUponSuccess(statusUponSuccessShort)
				.index(indexCurrentState)
				.expectedTypeAfter(typeAfter)
				.expectedTypeBefore(typeBefore)
				.myStateName(myStateName)
				.processName(processName)
				.taskName(description).build();
		// @formatter:on
	}

	private void workAppenderElements(DaProcessStepSourceAppender sourceAppender, List<Integer> chkSumIndex) { //NOSONAR
		List<BpmSchemeElementDescriptor> bpmDescriptors = new ArrayList<>();
		sourceAppender.getElementsToWork().stream().forEachOrdered(elementToWork -> {
			String statusUponFailure = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).statusUponFailure();
			String statusUponSuccess = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).statusUponSuccess();
			String description = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).description();
			if (description.equals(METHOD_NAME)) {
				description = elementToWork.getMethodName().replace(PARENTHESIS, EMPTY);
			}
			String encoding = elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).sourceEncoding();
			String processName = sourceAppender.getOriginatingClassShort();
			ClassAnnotationUtil.ClasspathSourceFile enumStateProvider = ClassAnnotationUtil.getValueOverTypeMirror(() -> elementToWork.getAnnotatedElement().getAnnotation(MakeStep.class).enumStateProvider());
			if (sourceAppender.getJavaScrFileForStatusClass() == null && enumStateProvider.hasClassFile()) {
				sourceAppender.setJavaScrFileForStatusClass(enumStateProvider.getFile());
			}

			if (IO.hasContents(encoding) && !encoding.equals(DaProcessStepConstants.NONE)) {
				try {
					sourceAppender.setSourceEncoding(Charset.forName(encoding));
				} catch (Exception e) {
					error(String.format("You have specified an encoding we simply cannot load : %s", encoding));
					return;
				}
			}

			StringBuilder errors = new StringBuilder(MAKE_STEP_ISSUES);
			appendPossibleErrors(errors, statusUponFailure, STATUS_UPON_FAILURE);
			appendPossibleErrors(errors, statusUponSuccess, STATUS_UPON_SUCCESS);
			appendPossibleErrors(errors, description, DESCRIPTION);
			appendPossibleErrors(errors, processName, PROCESS_NAME);
			appendPossibleErrors(errors, enumStateProvider.getClassPath(), ENUM_PATH);

			BpmSchemeElementDescriptor descriptor = testLoadStatusesAndExctractBpmDescriptors(errors, enumStateProvider.getClassPath(), statusUponSuccess, statusUponFailure, sourceAppender, description, processName);
			if (descriptor != null) {
				bpmDescriptors.add(descriptor);
			}

			if (errors.length() > MAKE_STEP_ISSUES.length()) {
				errors.delete(errors.length() - MESSAGE_SEPARATOR.length(), errors.length());
				error(MavenWriter.formattedErrors(errors.toString()));
				throw new ProcessRuntimeException("Could not build due to Makestep issues");
			} else {
				chkSumIndex.add(new StringBuilder(IO.hasContents(statusUponFailure) ? statusUponFailure : "null").append(IO.hasContents(statusUponSuccess) ? statusUponSuccess : "null").append(IO.hasContents(description) ? description : "null").append(IO.hasContents(enumStateProvider.getClassPath()) ? enumStateProvider.getClassPath() : "null").toString().hashCode());

				// add method
				info(String.format("    Step evaluated successfully : %s, enumpath  : %s, statusUponSuccess : %s, statusUponFailure : %s, description : \"%s\" ", elementToWork.getMethodName(), enumStateProvider.getClassPath(), statusUponSuccess, statusUponFailure, description));
				addMethod(sourceAppender, elementToWork.getMethodName().replace(PARENTHESIS, ""), enumStateProvider.getClassPath(), statusUponSuccess, statusUponFailure, description, processName);
			}
		});

		Collections.sort(bpmDescriptors);
		validateDescriptors(bpmDescriptors);
		sourceAppender.append(DaProcessStepConstants.THEEND, null);
		Collections.sort(chkSumIndex);
		StringBuilder chkSumBasis = new StringBuilder();
		chkSumIndex.stream().forEach(i -> chkSumBasis.append(i));  //NOSONAR
		sourceAppender.setChkSum(MD5Digest.getInstance().digestMessage(chkSumBasis.toString()));
		sourceAppender.setBpmDescriptors(bpmDescriptors);
		sourceAppender.doAppend();
		info(String.format("    DaProcessStepProcessor finished with %s", sourceAppender.toString()));
	}

	private void validateDescriptors(List<BpmSchemeElementDescriptor> descriptors) {
		if (!descriptors.isEmpty()) {
			StringBuilder descriptorErrors = new StringBuilder(MAKE_STEP_ISSUES);
			descriptors.stream().forEach(d -> descriptorErrors.append(d.validateForErrorOutput()));

			// first element ALWAYS preceded with the starter
			if (!descriptors.get(0).getExpectedTypeBefore().equals(BpmSchemeElementDescriptor.Type.StartEvent) || !descriptors.get(0).getExpectedTypeAfter().equals(BpmSchemeElementDescriptor.Type.Activity)) {
				descriptorErrors.append(String.format("The first non-failure state in your chain (%s) of taks should _always_ be preceded by a (BPM) StartEvent and be followed by a Task", descriptors.get(0).getMyStateName())).append(MESSAGE_SEPARATOR);
			}

			// mid-elements all connect to tasks..
			descriptors.stream().filter(d -> d.getIndex() != null && d.getIndex() > 0 && d.getIndex() < descriptors.size() - 1).forEach(dd -> {
				if (!dd.getExpectedTypeBefore().equals(BpmSchemeElementDescriptor.Type.Activity) || !dd.getExpectedTypeAfter().equals(BpmSchemeElementDescriptor.Type.Activity)) {
					descriptorErrors.append(String.format("The mid non-failure states (that is everything between start Task and last Task) such as this (%s) of should _always_ link (BPM) Task to Task", dd.getMyStateName())).append(MESSAGE_SEPARATOR);
				}
			});

			// last element ALWAYS followed with the finish_state
			if (!descriptors.get(descriptors.size() - 1).getExpectedTypeBefore().equals(BpmSchemeElementDescriptor.Type.Activity) || !descriptors.get(descriptors.size() - 1).getExpectedTypeAfter().equals(BpmSchemeElementDescriptor.Type.FINISH_STATE)) {
				descriptorErrors.append(String.format("The last non-failure state before finish state (%s) of should _always_ be preced with a (BPM) Task and be followed by the finish state", descriptors.get(descriptors.size() - 1).getMyStateName())).append(MESSAGE_SEPARATOR);
			}


			if (descriptorErrors.length() > MAKE_STEP_ISSUES.length()) {
				error(MavenWriter.formattedErrors(descriptorErrors.toString()));
				throw new ProcessRuntimeException("Could not build due to Status enum conceptual misunderstandings");
			}
		} else {
			warn("No BPM descriptors found - I will not generate BPM 2.0 XML schemes. Either the @ProcessBpmSchemeRepo annotation was not used or there where issues loading your status enumaration classes.");
		}
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
			warn("Could not make tmp compile folder..");
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
				Files.write(file.toPath(), source.getBytes(IO.UTF_8));  //NOSONAR
			} catch (IOException e) {
				warn(String.format("Could not prepare file %s for compilation", file.getAbsolutePath()));
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
				warn(String.format("Could not test compile file for class %s due to IO-issues..", className));
			} catch (ClassNotFoundException e) {
				warn(String.format("Could not load tmp compiled file for class %s..", className));
			}
		}
		return null;
	}
}
