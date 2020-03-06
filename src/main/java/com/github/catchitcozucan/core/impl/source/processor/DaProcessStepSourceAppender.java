package com.github.catchitcozucan.core.impl.source.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class DaProcessStepSourceAppender extends BaseDomainObject {
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

	DaProcessStepSourceAppender(File srcFile, String originatingClass, String originatingAppendeSource, String commentHeaderStart) {
		elementsToWork = new HashSet<>();
		this.sourceEncoding = null;
		this.sourceToAppend = new StringBuilder();
		this.srcFile = srcFile;
		this.originatingClass = originatingClass;
		this.originatingShort = originatingClass.substring(originatingClass.lastIndexOf(DaProcessStepConstants.DOT) + 1, originatingClass.length()).toUpperCase();
		this.originatingAppendeSource = IO.hasContents(originatingAppendeSource) ? removeSpaceAndNewlines(originatingAppendeSource) : DaProcessStepConstants.EMPTY;
		if (!this.originatingAppendeSource.equals(DaProcessStepConstants.EMPTY) && this.originatingAppendeSource.contains(DaProcessStepConstants.CHKSUM)) {
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
			DaProcessStepConstants.warn("    Oooops - chksum overflow....");
			this.chkSum = chkSum.substring(0, DaProcessStepConstants.CHKSUM_LEN);
		} else {
			StringBuilder padding = new StringBuilder();
			while (toPad > 0) {
				padding.append("X");
				toPad--;
			}
			this.chkSum = chkSum + padding.toString();
		}
		DaProcessStepConstants.info("    new chksum is : " + this.chkSum);
	}

	private String removeSpaceAndNewlines(String in) {
		return in.replace("}" + DaProcessStepConstants.NL, "").replace(DaProcessStepConstants.NL, "").replace(" ", "").replace("}", "");
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
					String appendedSrc = sourceToAppend.toString();
					String completeSrc = new StringBuilder(newSource).append(appendedSrc).toString();
					writeToFile(completeSrc);
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

	private void removePreviousAddition() {
		try {
			String currentSource = IO.fileToString(srcFile.getAbsolutePath(), sourceEncoding);
			if (currentSource.contains(DaProcessStepConstants.COMMENT_HEADER_END)) {
				currentSource = currentSource.replace(originatingAppendeSource, "");
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
}

