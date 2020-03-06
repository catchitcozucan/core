package com.github.catchitcozucan.core.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.io.Slf4JSetup;

public class ProcessLogging {

	private static final String DEF_PROCESSING_APP = "processing";
	private static ProcessLogging INSTANCE; //NOSONAR

	private ProcessLogging(String sytemLogParentDir, String loggingApp, boolean logSeparately) {
		if (!logSeparately) {
			Slf4JSetup.init(sytemLogParentDir, loggingApp, false);
		} else {
			Slf4JSetup.init(sytemLogParentDir, loggingApp, false, DEF_PROCESSING_APP);
		}
	}

	private ProcessLogging() { //NOSONAR
		String systemLogPathProperty = System.getProperty(ProcessingFlags.NEN_PROCESSING_LOG_DIR);
		String systemLogPathEnv = System.getenv(ProcessingFlags.NEN_PROCESSING_LOG_DIR);
		String logSeparatelyPatEnv = System.getenv(ProcessingFlags.NEN_PROCESSING_LOGGING_SEPARATE_FILE);
		String logSeparatelyPathProperty = System.getProperty(ProcessingFlags.NEN_PROCESSING_LOGGING_SEPARATE_FILE);
		boolean doLogSeparately = false;
		StringBuilder logPathError = new StringBuilder();
		File specfiedLogDir = null;
		String logVia = null;

		// First lookup system env
		if (IO.hasContents(systemLogPathEnv)) {
			try {
				logVia = ProcessingFlags.NEN_PROCESSING_LOG_DIR + "=" + systemLogPathEnv + " via system env property";
				specfiedLogDir = IO.makeOrUseDir(systemLogPathEnv);
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// ..yet a system property will ALWAYS override
		if (IO.hasContents(systemLogPathProperty)) {
			try {
				logVia = ProcessingFlags.NEN_PROCESSING_LOG_DIR + "=" + systemLogPathProperty + " via JVM system property";
				specfiedLogDir = IO.makeOrUseDir(systemLogPathProperty);
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// First lookup system env
		if (IO.hasContents(logSeparatelyPatEnv)) {
			try {
				if (logSeparatelyPatEnv.equalsIgnoreCase("true")) {
					doLogSeparately = true;
				}
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// ..yet a system property will ALWAYS override
		if (IO.hasContents(logSeparatelyPathProperty)) {
			try {
				if (logSeparatelyPathProperty.equalsIgnoreCase("true")) {
					doLogSeparately = true;
				} else if (logSeparatelyPathProperty.equalsIgnoreCase("false")) {
					doLogSeparately = false;
				}
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}


		if (specfiedLogDir != null) {
			if (!doLogSeparately) {
				Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), false);
			} else {
				Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), false, DEF_PROCESSING_APP);
			}
		} else {
			Slf4JSetup.initForApp(DEF_PROCESSING_APP, false);
		}
		Logger logger = LoggerFactory.getLogger(JobBase.class); //NOSONAR
		if (logPathError.length() > 0) {
			logger.error(String.format("Dismissed log dir set up via %s - caught %s", logVia, logPathError.toString())); //NOSONAR THIS IS BULL.
		}
	}

	public static synchronized void initLogging() {
		if (INSTANCE == null) {
			INSTANCE = new ProcessLogging();
		}
	}

	public static synchronized void initLogging(String sytemLogParentDir, String loggingApp, boolean logSeparately) {
		if (INSTANCE == null) {
			INSTANCE = new ProcessLogging(sytemLogParentDir, loggingApp, logSeparately);
		}
	}

	private static String getLoggingApp() {
		String systemLoggingAppProperty = System.getProperty(ProcessingFlags.NEN_PROCESSING_LOGGING_APP);
		String systemLoggingAppEnv = System.getenv(ProcessingFlags.NEN_PROCESSING_LOGGING_APP);

		// First lookup JVM sys property app
		if (IO.hasContents(systemLoggingAppProperty)) {
			return systemLoggingAppProperty;
		}
		// then lookup env app
		if (IO.hasContents(systemLoggingAppEnv)) {
			return systemLoggingAppEnv;
		}

		// give up..
		return DEF_PROCESSING_APP;
	}
}

