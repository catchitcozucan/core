/**
 * Original work by Ola Aronsson 2020
 * Courtesy of nollettnoll AB &copy; 2012 - 2020
 * <p>
 * Licensed under the Creative Commons Attribution 4.0 International (the "License")
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * https://creativecommons.org/licenses/by/4.0/
 * <p>
 * The software is provided “as is”, without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other liability,
 * whether in an action of contract, tort or otherwise, arising from, out of or
 * in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl;

import java.io.File;

import com.github.catchitcozucan.core.interfaces.LogConfig;
import com.github.catchitcozucan.core.internal.util.io.IO;
import com.github.catchitcozucan.core.internal.util.io.Slf4JSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessLogging {

	private static final String DEF_PROCESSING_APP = "processing";
	public static final String NOAPP = "noapp";
	public static final String NOPATH = "nopath";
	public static final String DISMISSED_LOG_DIR_SET_UP_VIA_S_CAUGHT_S = "Dismissed log dir set up via %s - caught %s";
	public static final String VIA_SYSTEM_ENV_PROPERTY = " via system env property";
	public static final String VIA_JVM_SYSTEM_PROPERTY = " via JVM system property";
	public static final String EQUALS = "=";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	private static ProcessLogging INSTANCE; //NOSONAR

	public enum LoggingSetupStrategy {
		SETUP_LOG_SEPARATELY, SETUP_LOGGING_LOG_INCLUSIVELY, NO_LOGGING_SETUP
	}

	private ProcessLogging(String sytemLogParentDir, String loggingApp, LoggingSetupStrategy loggingSetupStrategy) {
		if (loggingSetupStrategy.equals(LoggingSetupStrategy.SETUP_LOGGING_LOG_INCLUSIVELY)) {
			Slf4JSetup.init(sytemLogParentDir, loggingApp, false);
		} else if (loggingSetupStrategy.equals(LoggingSetupStrategy.SETUP_LOG_SEPARATELY)) {
			Slf4JSetup.init(sytemLogParentDir, loggingApp, false, DEF_PROCESSING_APP, true);
		}
	}

	private ProcessLogging() { //NOSONAR
		String systemLogPathProperty = System.getProperty(ProcessingFlags.NEN_PROCESSING_LOG_DIR);
		String systemLogPathEnv = System.getenv(ProcessingFlags.NEN_PROCESSING_LOG_DIR);
		String logSeparatelyPatEnv = System.getenv(ProcessingFlags.NEN_PROCESSING_LOGGING_SEPARATE_FILE);
		String logSeparatelyPathProperty = System.getProperty(ProcessingFlags.NEN_PROCESSING_LOGGING_SEPARATE_FILE);
		Boolean doLogSeparately = null;
		StringBuilder logPathError = new StringBuilder();
		File specfiedLogDir = null;
		String logVia = null;

		// First lookup system env
		if (IO.hasContents(systemLogPathEnv)) {
			try {
				logVia = ProcessingFlags.NEN_PROCESSING_LOG_DIR + EQUALS + systemLogPathEnv + VIA_SYSTEM_ENV_PROPERTY;
				specfiedLogDir = IO.makeOrUseDir(systemLogPathEnv);
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// ..yet a system property will ALWAYS override
		if (IO.hasContents(systemLogPathProperty)) {
			try {
				logVia = ProcessingFlags.NEN_PROCESSING_LOG_DIR + EQUALS + systemLogPathProperty + VIA_JVM_SYSTEM_PROPERTY;
				specfiedLogDir = IO.makeOrUseDir(systemLogPathProperty);
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// First lookup system env
		if (IO.hasContents(logSeparatelyPatEnv)) {
			try {
				if (logSeparatelyPatEnv.equalsIgnoreCase(TRUE)) {
					doLogSeparately = Boolean.TRUE;
				}
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}

		// ..yet a system property will ALWAYS override
		if (IO.hasContents(logSeparatelyPathProperty)) {
			try {
				if (logSeparatelyPathProperty.equalsIgnoreCase(TRUE)) {
					doLogSeparately = Boolean.TRUE;
				} else if (logSeparatelyPathProperty.equalsIgnoreCase(FALSE)) {
					doLogSeparately = Boolean.FALSE;
				}
			} catch (Exception e) {
				logPathError.append(e.getClass().getName());
			}
		}


		if (specfiedLogDir != null) {
			if (doLogSeparately != null) {
				if (!doLogSeparately.booleanValue()) {
					Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), doLogSeparately);
				} else {
					Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), false, DEF_PROCESSING_APP, doLogSeparately);
				}
			}
		} else if (doLogSeparately != null) {
			Slf4JSetup.initForApp(DEF_PROCESSING_APP, false);
		}
		Logger logger = LoggerFactory.getLogger(JobBase.class); //NOSONAR
		if (logPathError.length() > 0) {
			logger.error(String.format(DISMISSED_LOG_DIR_SET_UP_VIA_S_CAUGHT_S, logVia, logPathError.toString())); //NOSONAR THIS IS BULL.
		}
	}

	public static synchronized void halt() {
		Slf4JSetup.getInstance().halt();
		INSTANCE = null;
	}

	public static synchronized void initLogging() {
		if (INSTANCE == null) {
			INSTANCE = new ProcessLogging();
		}
	}

	public static synchronized void initLogging(LogConfig logConfig) {
		if (INSTANCE == null) {
			if (logConfig != null) {
				INSTANCE = new ProcessLogging(logConfig.getSytemLogParentDir(), logConfig.getLoggingApp(), logConfig.getLoggingSetupStrategy());
			} else {
				INSTANCE = new ProcessLogging(NOPATH, NOAPP, LoggingSetupStrategy.NO_LOGGING_SETUP);
			}
		}
	}

	public static synchronized void initLogging(String sytemLogParentDir, String loggingApp, LoggingSetupStrategy logSeparately) {
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

