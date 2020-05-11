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
    private static ProcessLogging INSTANCE; //NOSONAR

    private ProcessLogging(String sytemLogParentDir, String loggingApp, boolean logSeparately) {
        if (!logSeparately) {
            Slf4JSetup.init(sytemLogParentDir, loggingApp, false);
        } else {
            Slf4JSetup.init(sytemLogParentDir, loggingApp, false, DEF_PROCESSING_APP, logSeparately);
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
                Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), doLogSeparately);
            } else {
                Slf4JSetup.init(specfiedLogDir.getAbsolutePath(), getLoggingApp(), false, DEF_PROCESSING_APP, doLogSeparately);
            }
        } else {
            Slf4JSetup.initForApp(DEF_PROCESSING_APP, false);
        }
        Logger logger = LoggerFactory.getLogger(JobBase.class); //NOSONAR
        if (logPathError.length() > 0) {
            logger.error(String.format("Dismissed log dir set up via %s - caught %s", logVia, logPathError.toString())); //NOSONAR THIS IS BULL.
        }
    }

    public static synchronized void halt(){
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
            INSTANCE = new ProcessLogging(logConfig.getSytemLogParentDir(), logConfig.getLoggingApp(), logConfig.getLogSeparately());
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

