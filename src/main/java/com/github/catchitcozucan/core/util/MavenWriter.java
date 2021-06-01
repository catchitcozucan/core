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
package com.github.catchitcozucan.core.util;

import java.util.Arrays;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants;

public class MavenWriter {

	private static final int MAX_ERR_MSG_LEN = 82;
	private static final String INLINE_NEW_ERR = "    *   ";
	private static final String INLINE = "        ";
	public static final String MESSAGE_SEPARATOR = "@";
	private static final String SPACE = " ";
	private static final String EMPTY = "";
	private static final String WH_CHARS = ""       /* dummy empty string for homogeneity */ + "\\u0009" // CHARACTER TABULATION
			+ "\\u000A" // LINE FEED (LF)
			+ "\\u000B" // LINE TABULATION
			+ "\\u000C" // FORM FEED (FF)
			+ "\\u000D" // CARRIAGE RETURN (CR)
			//+ "\\u0020" // SPACE
			+ "\\u0085" // NEXT LINE (NEL)
			+ "\\u00A0" // NO-BREAK SPACE
			+ "\\u1680" // OGHAM SPACE MARK
			+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
			+ "\\u2000" // EN QUAD
			+ "\\u2001" // EM QUAD
			+ "\\u2002" // EN SPACE
			+ "\\u2003" // EM SPACE
			+ "\\u2004" // THREE-PER-EM SPACE
			+ "\\u2005" // FOUR-PER-EM SPACE
			+ "\\u2006" // SIX-PER-EM SPACE
			+ "\\u2007" // FIGURE SPACE
			+ "\\u2008" // PUNCTUATION SPACE
			+ "\\u2009" // THIN SPACE
			+ "\\u200A" // HAIR SPACE
			+ "\\u2028" // LINE SEPARATOR
			+ "\\u2029" // PARAGRAPH SEPARATOR
			+ "\\u202F" // NARROW NO-BREAK SPACE
			+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
			+ "\\u3000"; // IDEOGRAPHIC SPACE
	private static final String SPACE_CHARS_BUT_NOT_SPACE = "[ " + WH_CHARS + " ]"; //NOSONAR
	public static final String COLON = ":";
	public static final String I_CAN_ONLY_BE_INITIALIZED_WITH_A_JAVAX_ANNOTATION_PROCESSING_MESSAGER = "I can only be initialized with a javax.annotation.processing.Messager!";
	public static final String ERROR = "[ERROR] ";
	public static final String INFO = "[INFO] ";
	public static final String WARN = "[WARN] ";

	private final javax.annotation.processing.Messager messager;

	private MavenWriter() {
		throw new IllegalStateException(I_CAN_ONLY_BE_INITIALIZED_WITH_A_JAVAX_ANNOTATION_PROCESSING_MESSAGER);
	}

	private MavenWriter(Messager messager) {
		this.messager = messager;
	}

	private static MavenWriter instance;

	public static synchronized MavenWriter getInstance(Messager messager) {
		if (instance == null) {
			instance = new MavenWriter(messager);
		}
		return instance;
	}

	public static synchronized MavenWriter getInstance() {
		if (instance == null) {
			instance = new MavenWriter();
		}
		return instance; // we will already have thrown in case we were not initialized correctly
	}

	public void error(Element e, String msg, Object... args) { //NOSONAR
		messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
	}

	public void error(String msg) { //NOSONAR
		System.out.println(ERROR + msg); //NOSONAR this is how Maven outputs..
	}

	public void info(String msg) { //NOSONAR
		System.out.println(INFO + msg); //NOSONAR - this _is_ how primitive manve's output is..
	}

	public void warn(String msg) { //NOSONAR
		System.out.println(WARN + msg); //NOSONAR - this _is_ how primitive manve's output is..
	}

	public static String formattedErrors(String in) {
		StringBuilder formatted = new StringBuilder(DaProcessStepConstants.NL).append(DaProcessStepConstants.NL).append(in.substring(0, in.indexOf(COLON))).append(DaProcessStepConstants.NL);
		String body = in.substring(in.indexOf(COLON) + 2);
		String[] messages = body.split(MESSAGE_SEPARATOR);
		Arrays.stream(messages).forEach(m -> {
			m = m.replaceAll(SPACE_CHARS_BUT_NOT_SPACE, EMPTY);
			m = m.trim();
		});
		Arrays.stream(messages).forEach(m -> {
			formatted.append(DaProcessStepConstants.NL).append(INLINE_NEW_ERR);
			char[] chars = m.toCharArray();
			int inRowPos = -1;
			for (int i = 0; i < chars.length; i++) {
				inRowPos++;
				if (inRowPos < MAX_ERR_MSG_LEN + 1) {
					formatted.append(chars[i]);
				} else {
					int nextSpace = m.indexOf(SPACE, i);
					int charsToGrab = nextSpace - i;
					int pos = i;
					for (int ii = 0; ii < charsToGrab + 1; ii++) {
						formatted.append(chars[pos]);
						pos++;
					}
					formatted.append(DaProcessStepConstants.NL).append(INLINE);
					inRowPos = -1;
					i = i + charsToGrab; //NOSONAR
				}
			}
		});
		formatted.append(DaProcessStepConstants.NL);
		return formatted.toString();
	}
}

