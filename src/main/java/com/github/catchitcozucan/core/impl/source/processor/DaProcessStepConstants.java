package com.github.catchitcozucan.core.impl.source.processor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;

import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.util.MavenWriter;

public class DaProcessStepConstants {
	public static final String NL = System.getProperty("line.separator");
	public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
	static final String COM_SUN_TOOLS_JAVAC_FILE_REGULAR_FILE_OBJECT = "com.sun.tools.javac.file.RegularFileObject";
	static final String CLASSFILE = "classfile";
	static final String FILE = "file";
	static final String INTRO_TEXT = "    // The following code is generated by the DaProcessStepProcessor ";
	static final String CHKSUMPREFIX = "    ///////CHKSUM: ";
	static final String HEADER_START_NEW = new StringBuilder(NL).append(CHKSUMPREFIX).toString();
	static final int CHKSUM_POS = HEADER_START_NEW.length() + 1;
	static final int CHKSUM_LEN = 40;
	static final String SPACES_AND_SLASHES = "    //";
	static final String COMMENT_HEADER = new StringBuilder().append(CHKSUMPREFIX).append("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX////////////////////////").append(NL).append(SPACES_AND_SLASHES).append(NL).append(INTRO_TEXT).append(NL).append("    // written by Ola Aronsson in 2020, courtesy of nollettnoll AB").append(NL).append(SPACES_AND_SLASHES).append(NL).append("    // DO NOT edit this " +
            "section. Modify @MakeStep or CHKSUM (then keep length)  to re-generate.").append(NL).append(SPACES_AND_SLASHES).append(NL).toString();
	static final String HEADER_START_OLD = new StringBuilder(NL).append("    ///////////////////////////////////////////////////////////////////////////////").toString();
	static final String ANNOT_MAKESTEP_JAVA_PATH = "com.github.catchitcozucan.core.MakeStep";
	static final String ANNOT_PROCESSSTATUS_JAVA_PATH = "com.github.catchitcozucan.core.ProcessStatus";
	static final Set<String> NEN_BLACK_PROCESS_MAKESTEP_SUPPORTED_TYPES = new HashSet<>(Arrays.asList(ANNOT_MAKESTEP_JAVA_PATH, ANNOT_PROCESSSTATUS_JAVA_PATH));
	static final String COMMENT_HEADER_SIGN = "written by Ola Aronsson";
	static final String COMMENT_HEADER_END = new StringBuilder().append(HEADER_START_OLD).append(NL).append(SPACES_AND_SLASHES).append(NL).append("    // End DaProcessStepProcessor generation").append(NL).append(SPACES_AND_SLASHES).append(NL).toString();
	static final String NONE = "NONE";
	static final String THEEND = COMMENT_HEADER_END + NL + "}";
	static final String OVERRIDE = "        @Override";
	static final String SPACES_AND_CURLY = "        }";
	static final String SIGN_PART = "    private final ProcessStep ";
	static final String STEP = "Step";
	static final String DOT = ".";
	static final String SLASH = "/";
	static final String CHKSUM = "CHKSUM";
	static final String CHKSUM_AND_COLON = "CHKSUM:";
	static final String EMPTY = "";
	static String CHKSUM_ORIG = "NOCHANGESXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"; // NOSONAR
	static final String OWNER = "owner";
	static final String TYPE = "type";
	static final String TSYM = "tsym";
	private static final String SPACEDDASH = " - ";
	private static final String MAKE_STEPS_SHORT_CLASS_NAME = MakeStep.class.getSimpleName();
	private static final String MAVEN_LOG_PREFIX = new StringBuilder(MAKE_STEPS_SHORT_CLASS_NAME).append(SPACEDDASH).toString();
	//@formatter:off
    static final String BODY = new StringBuilder("new ProcessStep(){ ")
            .append(NL)
            .append(NL)
            .append(OVERRIDE)
            .append(NL)
            .append("        public void execute() {")
            .append(NL)
            .append("            %s();")
            .append(NL)
            .append(SPACES_AND_CURLY)
            .append(NL)
            .append(NL)
            .append(OVERRIDE)
            .append(NL)
            .append("        public String processName() {")
            .append(NL)
            .append("            return \"%s\";")
            .append(NL)
            .append(SPACES_AND_CURLY)
            .append(NL)
            .append(NL)
            .append(OVERRIDE)
            .append(NL)
            .append("        public String description() {")
            .append(NL)
            .append("            return \"%s\";")
            .append(NL)
            .append(SPACES_AND_CURLY)
            .append(NL)
            .append(NL)
            .append(OVERRIDE)
            .append(NL)
            .append("        public Enum<?> statusUponSuccess() {")
            .append(NL)
            .append("            return %s.%s;")
            .append(NL)
            .append(SPACES_AND_CURLY)
            .append(NL)
            .append(NL)
            .append(OVERRIDE)
            .append(NL)
            .append("        public Enum<?> statusUponFailure() {")
            .append(NL)
            .append("            return %s.%s;")
            .append(NL)
            .append(SPACES_AND_CURLY)
            .append(NL)
            .append(NL)
            .append("    };")
            .append(NL).toString();
    //@formatter:on

	private DaProcessStepConstants() {}

	static void error(String msg) {
		MavenWriter.getInstance().error(mkMessage(msg));
	}

	static void info(String msg) {
		MavenWriter.getInstance().info(mkMessage(msg));
	}

	static void warn(String msg) {
		MavenWriter.getInstance().warn(mkMessage(msg));
	}

	static void error(Element e, String msg, Object... args) {
		MavenWriter.getInstance().error(e, mkMessage(msg), args);
	}

	private static String mkMessage(String mess) {
		return new StringBuilder(MAVEN_LOG_PREFIX).append(mess).toString();
	}
}
