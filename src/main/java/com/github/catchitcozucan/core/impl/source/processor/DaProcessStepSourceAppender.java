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

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.CRITERIA_STATES;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.CURLY_RIGHT;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.DOT;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.EMPTY;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.FINISH_STATE;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.HEADER_START_OLD;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NAME;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NL;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.PROCESS_INTERNAL;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.SPACE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.source.processor.bpm.BpmSchemeElementDescriptor;
import com.github.catchitcozucan.core.impl.source.processor.bpm.BpmSchemeGenerator;
import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class DaProcessStepSourceAppender extends BaseDomainObject {
	public static final String BPM_2_0_SCHEME_XML = "_BPM_2.0_Scheme.xml";
	public static final String X = "X";
	public static final String NEW_CHKSUM_IS = "    new chksum is : ";
	public static final String OOOOPS_CHKSUM_OVERFLOW = "    Oooops - chksum overflow....";
	private static final String NEW_AND_FAIL_STATES = "NEW_AND_FAIL_STATES";
	private static final String FORMATTER = "FORMATTER";
	private static final String STATUSES_AND_STEPS = "STATUSES_AND_STEPS";
	private static final String CASE = "                case ";
	private static final String EXECUTE_STEP = "                    executeStep(";
	private static final String BREAK = "                    break;";
	private static final String COLON = ":";
	private static final String PARENTHESIS_END_SEMI_COLON = ");";
	private static final String STRING_FORMAT = "%s";
	private static final String COMMA = ",";
	private static final String SPACE = "        ";
	private StringBuilder sourceToAppend;
	private boolean hasAppended;
	private Set<ElementToWork> elementsToWork;
	private final String completeSourceOrigSource;
	private final File srcFile;
	private final String originatingClass;
	private final String originatingShort;
	private final String originatingAppendeSource;
	private String chkSumOrig = DaProcessStepConstants.CHKSUM_ORIG;
	private String chkSum = DaProcessStepConstants.CHKSUM_ORIG;
	private Charset sourceEncoding;
	private File javaScrFileForStatusClass;
	private List<BpmSchemeElementDescriptor> bpmDescriptors;
	private File bpmRepoFolder;
	private Integer bpmActivitiesPercolumn;
	private static final String FORMATTER_OFF = new StringBuilder(NL).append("    //@formatter:off DO_NOT_FORMAT").append(NL).toString();
    private static final String FORMATTER_ON = new StringBuilder("    //@formatter:on END DO_NOT_FORMAT").append(NL).append(NL).toString();
	private boolean formatterNotAppended;
	private String mavenModulePath;
	private String mavenRepoPath;
	private boolean criteriaStateOnlyFailure;
	private boolean acceptEnumFailures;

	DaProcessStepSourceAppender(File srcFile, String originatingClass, String originatingAppendeSource, String commentHeaderStart) {
		elementsToWork = new HashSet<>();
		this.sourceEncoding = null;
		this.sourceToAppend = new StringBuilder();
		this.srcFile = srcFile;
		this.originatingClass = originatingClass;
		this.originatingShort = originatingClass.substring(originatingClass.lastIndexOf(DaProcessStepConstants.DOT) + 1, originatingClass.length()).toUpperCase();
		this.originatingAppendeSource = IO.hasContents(originatingAppendeSource) ? removeSpaceAndNewlines(originatingAppendeSource) : EMPTY;
		if (!this.originatingAppendeSource.equals(EMPTY) && this.originatingAppendeSource.contains(DaProcessStepConstants.CHKSUM)) {
			int chksumStart = this.originatingAppendeSource.indexOf(DaProcessStepConstants.CHKSUM_AND_COLON) + DaProcessStepConstants.CHKSUM_AND_COLON.length();
			chkSumOrig = this.originatingAppendeSource.substring(chksumStart, (this.originatingAppendeSource.indexOf(DaProcessStepConstants.SLASH, chksumStart + 1)));
		}
		DaProcessStepConstants.info(String.format("Looking into class %s : chksum pre-process is : %s", originatingClass, chkSumOrig));

		String source = null;
		String sourceBefore = null;
		String sourceAfter = null;
		try {
			source = IO.fileToString(srcFile.toString(), sourceEncoding);
			if (source.indexOf(commentHeaderStart) > -1) {
				sourceBefore = source.substring(0, source.indexOf(commentHeaderStart));
				sourceAfter = source.substring(source.indexOf(DaProcessStepConstants.COMMENT_HEADER_END) + DaProcessStepConstants.COMMENT_HEADER_END.length(), source.length());
			}
		} catch (IOException e) {
			throw new ProcessRuntimeException(String.format("Could not read source file %s", srcFile.getAbsolutePath()), e);
		}
		if (sourceBefore != null && sourceAfter != null) {
			this.completeSourceOrigSource = new StringBuilder(sourceBefore).append(sourceAfter).toString();
		} else {
			this.completeSourceOrigSource = source.replace(DaProcessStepConstants.COMMENT_HEADER_END, "");
		}
	}

	String getOriginatingClass() {
		return originatingClass;
	}

	String getOriginatingClassShort() {
		return originatingShort;
	}

	void setChkSum(String chkSum) {
		int toPad = DaProcessStepConstants.CHKSUM_LEN - chkSum.length();
		if (toPad < 0) {
			chkSum = chkSum.substring(0, DaProcessStepConstants.CHKSUM_LEN);
			DaProcessStepConstants.warn(OOOOPS_CHKSUM_OVERFLOW);
			this.chkSum = chkSum.substring(0, DaProcessStepConstants.CHKSUM_LEN);
		} else {
			StringBuilder padding = new StringBuilder();
			while (toPad > 0) {
				padding.append(X);
				toPad--;
			}
			this.chkSum = chkSum + padding.toString();
		}
		DaProcessStepConstants.info(NEW_CHKSUM_IS + this.chkSum);
	}

	private String removeSpaceAndNewlines(String in) {
		return in.replace(CURLY_RIGHT + NL, EMPTY).replace(NL, EMPTY).replace(DaProcessStepConstants.SPACE, EMPTY).replace(CURLY_RIGHT, EMPTY);
	}

	void appendElementToWork(ElementToWork annotatedElement) {
		elementsToWork.add(annotatedElement);
	}

	Set<ElementToWork> getElementsToWork() {
		return elementsToWork;
	}

	File getSrcFile() {
		return srcFile;
	}

	void append(String s, String significantPart) {
		if (!IO.hasContents(significantPart) || (IO.hasContents(significantPart) && !completeSourceOrigSource.contains(significantPart))) {
			if (!completeSourceOrigSource.contains(s) && !sourceToAppend.toString().contains(s)) { // NOSONAR this is easier to read for me
				sourceToAppend.append(s);
				if (!s.contains("///////")) {
					hasAppended = true;
				}
			}
			if (!formatterNotAppended) {
				sourceToAppend.append(FORMATTER_OFF);
				formatterNotAppended = true;
			}
		}
	}

	void doAppend() {
		try {
			if (hasAppended || !chkSum.equals(chkSumOrig)) { // rewrite to the new format even if nothing was appended!
				String newSource = completeSourceOrigSource.substring(0, completeSourceOrigSource.lastIndexOf('}'));
				if (sourceEncoding != null) {
					newSource = new String(newSource.getBytes(), sourceEncoding);
				}
				if (!chkSum.equals(chkSumOrig)) {
					removePreviousAddition();
					DaProcessStepConstants.info("  - steps have changed : RE-WRITING");
					sourceToAppend.replace(DaProcessStepConstants.CHKSUM_POS - 2, DaProcessStepConstants.CHKSUM_POS + DaProcessStepConstants.CHKSUM_LEN + 1, chkSum);
					String appendedSrc;
					if (!bpmDescriptors.isEmpty() && !sourceToAppend.toString().contains(FORMATTER_ON)) {
						appendedSrc = sourceToAppend.toString().replace(HEADER_START_OLD, appendCriteriaStatesAndFinalStateAndToggleFormatting());
					} else {
						appendedSrc = sourceToAppend.toString().replace(HEADER_START_OLD, new StringBuilder(FORMATTER_ON).append(HEADER_START_OLD).toString());
					}
					String completeSrc = new StringBuilder(newSource).append(appendedSrc).toString();
					writeToFile(completeSrc);
					generateBpmScheme();
				} else {
					DaProcessStepConstants.info("    steps have not changed");
				}
			} else {
				DaProcessStepConstants.info("    steps have not changed, nothing appended");
			}
		} catch (IOException e) {
			throw new ProcessRuntimeException("Could not append source", e);
		}
	}

	private String appendCriteriaStatesAndFinalStateAndToggleFormatting() {
		StringBuilder addtionals = new StringBuilder();
		addtionals.append(NL).append(String.format(NAME, originatingClass)).append(NL);
		if (!bpmDescriptors.isEmpty() && bpmDescriptors.get(0) != null) {

			String originatingClassPlusEnumFieldName = new StringBuilder(bpmDescriptors.get(0).getEnumContainer().getOriginatingClassPath()).append(".").append(bpmDescriptors.get(0).getEnumContainer().getEnumFieldName()).toString();
			addtionals.append(NL).append(String.format(FINISH_STATE, originatingClassPlusEnumFieldName, originatingClassPlusEnumFieldName)).append(NL);

			String criteriaStates = String.format(CRITERIA_STATES, originatingClassPlusEnumFieldName);
			List<String> statesShort = new ArrayList<>();
			if(!criteriaStateOnlyFailure) {
				statesShort.add(bpmDescriptors.get(0).getMyStateName());
			}
			bpmDescriptors.stream().forEachOrdered(e -> statesShort.add(e.getStatusUponFailure()));
			criteriaStates = criteriaStates.replace(NEW_AND_FAIL_STATES, makeStateDescritions(originatingClassPlusEnumFieldName, statesShort));
			addtionals.append(criteriaStates).append(NL);

			String processInternal = String.format(PROCESS_INTERNAL, originatingClassPlusEnumFieldName);
			processInternal = processInternal.replace(FORMATTER, STRING_FORMAT);
			StringBuilder stepsAndStatuses = new StringBuilder();
			bpmDescriptors.stream().forEach(s -> { //NOSONAR
				stepsAndStatuses.append(CASE).append(s.getMyStateName()).append(COLON).append(NL)
						.append(CASE).append(s.getStatusUponFailure()).append(COLON).append(NL)
						.append(EXECUTE_STEP).append(s.getStepMethodName()).append(PARENTHESIS_END_SEMI_COLON).append(NL)
						.append(BREAK).append(NL);
			});
			processInternal = processInternal.replace(STATUSES_AND_STEPS, stepsAndStatuses.toString());
			addtionals.append(processInternal).append(NL);
			addtionals.append(FORMATTER_ON).append(HEADER_START_OLD);
		}
		return addtionals.toString();
	}

	private CharSequence makeStateDescritions(String originatingClassPlusEnumFieldName, List<String> statesShort) {
		StringBuilder b = new StringBuilder();
		statesShort.stream().forEachOrdered(s -> b.append(new StringBuilder(NL).append(SPACE).append(originatingClassPlusEnumFieldName).append(DOT).append(s).append(COMMA)));
		b.delete(b.length()-1, b.length());
		return b.toString();
	}

	private void removePreviousAddition() {
		try {
			String currentSource = IO.fileToString(srcFile.getAbsolutePath(), sourceEncoding);
			if (currentSource.contains(DaProcessStepConstants.COMMENT_HEADER_END)) {
				currentSource = currentSource.replace(originatingAppendeSource, EMPTY);
				writeToFile(currentSource);
			}
		} catch (IOException e) {
			throw new ProcessRuntimeException(String.format("Could not re-write original source file : %s ", srcFile.getAbsolutePath()), e);
		}
	}

	private void writeToFile(String completeSrc) throws IOException {
		if (sourceEncoding != null) {
			IO.overwriteStringToFileWithEncoding(srcFile.getAbsolutePath(), completeSrc, sourceEncoding.displayName());
		} else {
			IO.overwriteStringToFileWithEncoding(srcFile.getAbsolutePath(), completeSrc, null);
		}
	}

	@Override
	public String doToString() {
		return originatingClass;
	}

	void clear() {
		sourceToAppend = new StringBuilder();
	}

	void setSourceEncoding(Charset specifiedSourceCharset) {
		this.sourceEncoding = specifiedSourceCharset;
	}

	boolean matchStatusClass(Object classSymbol) {
		return completeSourceOrigSource.contains(classSymbol.toString());
	}

	boolean hasMatchedJavaScrFileForStatusClass() {
		return javaScrFileForStatusClass != null;
	}

	File getJavaScrFileForStatusClass() {
		return javaScrFileForStatusClass;
	}

	public void setJavaScrFileForStatusClass(File fileViaSunClassesReflectionHack) {
		this.javaScrFileForStatusClass = fileViaSunClassesReflectionHack;
	}

	public void setBpmDescriptors(List<BpmSchemeElementDescriptor> bpmDescriptors) {
		this.bpmDescriptors = bpmDescriptors;
	}

	public void setBpmRepoFolder(File bpmRepoFolder) {
		this.bpmRepoFolder = bpmRepoFolder;
	}

	private void generateBpmScheme() {
		if (bpmRepoFolder != null && !acceptEnumFailures) {
			File xmlFile = new File(new StringBuilder(bpmRepoFolder.getAbsolutePath()).append(File.separator).append(this.originatingShort.toUpperCase()).append(BPM_2_0_SCHEME_XML).toString());
			if (xmlFile.exists() && !xmlFile.delete()) { //NOSONAR
				throw new ProcessRuntimeException(String.format("Could not remove old XML-file : %s", xmlFile.getAbsolutePath()));
			}
			BpmSchemeGenerator generator = new BpmSchemeGenerator(xmlFile, bpmDescriptors, bpmActivitiesPercolumn);
			generator.generateAndWriteScheme();
		}
	}

	public void setBpmActivitiesPerColumn(Integer activitiesPercolumn) {
		this.bpmActivitiesPercolumn = activitiesPercolumn;
	}

	public void setMavenModulePath(String mavenModulePath) {
		this.mavenModulePath = mavenModulePath;
	}

	public String getMavenModulePath() {
		return mavenModulePath;
	}

	public void setCriteriaStateOnlyFailure(boolean criteriaStateOnlyFailure) {
		this.criteriaStateOnlyFailure = criteriaStateOnlyFailure;
	}

	public void setAcceptEnumFailures(boolean acceptFailures) {
		this.acceptEnumFailures = acceptFailures;
	}

	public boolean getAcceptEnumFilures() {
		return acceptEnumFailures;
	}

	public void setMavenRepoPath(String mavenRepoPath) {
		this.mavenRepoPath = mavenRepoPath;
	}

	public String getMavenRepoPath() {
		return mavenRepoPath;
	}
}

