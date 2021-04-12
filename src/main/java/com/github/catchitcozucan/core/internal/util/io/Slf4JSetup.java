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
package com.github.catchitcozucan.core.internal.util.io;

import java.io.File;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;


public class Slf4JSetup implements LoggingService {

    private static final String LOG_PATH_INFO = "tot_%s.log";
    private static final String LOG_PATH_INFO_PATTERN = "info.log.%i.gz";
    private static final String LOG_PATH_DEBUG = "debug_%s.log";
    private static final String LOG_PATH_DEBUG_PATTERN = "debug.log.%i.gz";
    private static final String LOG_PATH_ERROR = "error_%s.log";
    private static final String LOG_PATH_ERROR_PATTERN = "error.log.%i.gz";
    private static final int MAX_LOG_FILES = 20;
    private static final String MAX_LOG_FILE_SIZE = "100MB";
    private static final String LOG_PATTERN = "%date %level [%thread] %logger{10}.%line %msg%n";
    public static final String COM_GITHUB_CATCHITCOZUCAN = "com.github.catchitcozucan";
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String THE_LOGGING_SERVICE_IS_NOT_INITIALIZED = "The LoggingService is NOT initialized!";
    public static final String CANNOT_CREATE_LOG_FOLDER_S = "Cannot create log folder %s";
    public static final String PROVIDED_LOG_PATH_S_IS_AN_EXISTING_FILE_IT_SHOULD_BE_A_DIRECTORY = "Provided log path %s is an existing FILE. It should be a directory!";
    public static final String SETTING_NEW_LOGLEVEL_S = "Setting new loglevel : %s";
    public static final String GOT_NON_PARSEABLE_LOGGING_LEVEL_S_RETURNING_INFO = "Got non-parseable logging level %s - returning INFO!";
    public static final String GOT_EMPTY_LOGGING_LEVEL_RETURNING_INFO = "Got empty logging level - returning INFO!";
    public static final String ROLLING_FILE_APPENDER_S = "RollingFileAppender_%s";
    private static Slf4JSetup INSTANCE; //NOSONAR
    private LoggerContext context;
    private static org.slf4j.Logger LOGGER; //NOSONAR - this _is_ bull.
    private static final String LOGS = "logs";

    private Slf4JSetup(File logFolder, String applicationName, boolean rundebug, String logFilePrefix, boolean logSeparately) {
        context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // den SKA ha skapats tidigare men den finns inte på jenkins -> loggga till tempdir
        if (!logFolder.exists()) {
            logFolder = new File(System.getProperty(JAVA_IO_TMPDIR));
        }

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern(LOG_PATTERN);
        ple.setContext(context);
        ple.setCharset(StandardCharsets.UTF_8);

        String folderPrefix = new StringBuilder(logFolder.getAbsolutePath()).append(File.separator).toString();

        if (IO.hasContents(logFilePrefix)) {
            applicationName = logFilePrefix;
        }

        RollingFileAppender<ILoggingEvent> fileAppenderInfo = getFilteredAppender(context, ple, folderPrefix + String.format(LOG_PATH_INFO, applicationName), Level.INFO, logSeparately);
        FixedWindowRollingPolicy rollingPolicyInfo = getRollingPolicy(context, fileAppenderInfo, LOG_PATH_INFO_PATTERN);
        setTriggerPolicy(fileAppenderInfo, rollingPolicyInfo);

        RollingFileAppender<ILoggingEvent> fileAppenderDebug = null;
        if (rundebug) {
            fileAppenderDebug = getFilteredAppender(context, ple, folderPrefix + String.format(LOG_PATH_DEBUG, applicationName), Level.DEBUG, logSeparately);
            FixedWindowRollingPolicy rollingPolicyDebug = getRollingPolicy(context, fileAppenderDebug, LOG_PATH_DEBUG_PATTERN);
            setTriggerPolicy(fileAppenderDebug, rollingPolicyDebug);
        }

        RollingFileAppender<ILoggingEvent> fileAppenderError = getFilteredAppender(context, ple, folderPrefix + String.format(LOG_PATH_ERROR, applicationName), Level.ERROR, logSeparately);
        FixedWindowRollingPolicy rollingPolicyError = getRollingPolicy(context, fileAppenderError, LOG_PATH_ERROR_PATTERN);
        setTriggerPolicy(fileAppenderError, rollingPolicyError);

        context.start();
        ple.start();

        fileAppenderInfo.start();
        if (fileAppenderDebug != null) {
            fileAppenderDebug.start();
        }
        fileAppenderError.start();

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME); // NOSONAR BULL.
        root.addAppender(fileAppenderInfo);
        if (fileAppenderDebug != null) {
            root.addAppender(fileAppenderDebug);
        }
        root.addAppender(fileAppenderError);
        root.setLevel(Level.INFO);

        StatusPrinter.print(context);
        LOGGER = LoggerFactory.getLogger(Slf4JSetup.class); //NOSONAR - this _is_ bull.
    }

    public static synchronized LoggingService getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            throw new IllegalStateException(THE_LOGGING_SERVICE_IS_NOT_INITIALIZED);
        }
    }

    public static synchronized void initForApp(String applicationName, boolean logSeparately) {
        init(new StringBuilder(System.getProperty("user.home")).append(".nenLogs").toString(), applicationName, logSeparately);
    }

    public static synchronized void initForApp(String applicationName, boolean runDebug, boolean logSeparately) {
        initInternal(new StringBuilder(System.getProperty("user.home")).append(".nenLogs").toString(), applicationName, runDebug, null, logSeparately);
    }

    public static synchronized void init(String pathToFolder, String applicationName, boolean logSeparately) {
        initInternal(pathToFolder, applicationName, true, null, logSeparately);
    }

    public static synchronized void init(String pathToFolder, String applicationName, boolean runDebug, boolean logSeparately) {
        initInternal(pathToFolder, applicationName, runDebug, null, logSeparately);
    }

    public static synchronized void init(String pathToFolder, String applicationName, boolean runDebug, String logFilePrefix, boolean logSeparately) {
        initInternal(pathToFolder, applicationName, runDebug, logFilePrefix, logSeparately);
    }

    private static void initInternal(String pathToFolder, String applicationName, boolean runDebug, String logFilePrefix, boolean logSeparately) {
        if (INSTANCE == null) {
            File logFolder = new File(new StringBuilder(pathToFolder).append(File.separator).append(applicationName).append(File.separator).append(LOGS).toString());
            if (!logFolder.exists()) {
                try {
                    logFolder.mkdirs();
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format(CANNOT_CREATE_LOG_FOLDER_S, pathToFolder));
                }
            } else if (logFolder.isFile()) {
                throw new IllegalArgumentException(String.format(PROVIDED_LOG_PATH_S_IS_AN_EXISTING_FILE_IT_SHOULD_BE_A_DIRECTORY, pathToFolder));
            }
            INSTANCE = new Slf4JSetup(logFolder, applicationName, runDebug, logFilePrefix, logSeparately);
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        if (context != null && context.isStarted() && weHaveALoglevelAndItIsNew(level)) {
            LOGGER.info(String.format(SETTING_NEW_LOGLEVEL_S, level.levelStr)); //NOSONAR - this _is_ bull.
            ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(level);  // NOSONAR BULL.
        }
    }

    @Override
    public void halt() {
        if (context != null && context.isStarted()) {
            StatusPrinter.print(context);
            context.stop();
            INSTANCE = null; //NOSONAR
        }
    }

    public static Level resolveLevel(String levelProperty) {
        if (IO.hasContents(levelProperty)) {
            String configuredVal = levelProperty.trim().toUpperCase();
            try {
                return Level.valueOf(levelProperty.trim().toUpperCase());
            } catch (Exception e) {
                if (INSTANCE != null && LOGGER != null) {
                    LOGGER.warn(String.format(GOT_NON_PARSEABLE_LOGGING_LEVEL_S_RETURNING_INFO, configuredVal));
                }
                return Level.INFO;
            }
        } else {
            LOGGER.warn(GOT_EMPTY_LOGGING_LEVEL_RETURNING_INFO);
            return Level.INFO;
        }
    }

    private static class ThresholdLoggerFilter extends Filter<ILoggingEvent> {
        private Level level;
        private final boolean logSeparately;

        public ThresholdLoggerFilter(boolean logSeparately) {
            this.logSeparately = logSeparately;
        }

        @Override
        public FilterReply decide(ILoggingEvent event) {
            if (logSeparately && !event.getLoggerName().startsWith(COM_GITHUB_CATCHITCOZUCAN)) {
                return FilterReply.DENY;
            }
            if (level == null) {
                return FilterReply.NEUTRAL;
            }
            if (event.getLevel().isGreaterOrEqual(level)) {
                return FilterReply.NEUTRAL;
            } else {
                return FilterReply.DENY;
            }
        }

        public void setLevel(Level level) {
            this.level = level;
        }
    }

    private static RollingFileAppender<ILoggingEvent> getFilteredAppender(LoggerContext lc, PatternLayoutEncoder ple, String path, Level level, boolean logSeparately) {
        RollingFileAppender<ILoggingEvent> fileAppenderInfo = new RollingFileAppender<>();
        fileAppenderInfo.setFile(path);
        fileAppenderInfo.setEncoder(ple);
        fileAppenderInfo.setContext(lc);
        ThresholdLoggerFilter filterInfo = new ThresholdLoggerFilter(logSeparately);
        filterInfo.setLevel(level);
        filterInfo.start();
        fileAppenderInfo.addFilter(filterInfo);
        fileAppenderInfo.setName(String.format(ROLLING_FILE_APPENDER_S, level.levelStr));
        return fileAppenderInfo;
    }

    private static FixedWindowRollingPolicy getRollingPolicy(LoggerContext lc, RollingFileAppender<ILoggingEvent> appender, String pattern) {
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(lc);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(pattern);
        rollingPolicy.setMaxIndex(MAX_LOG_FILES);
        appender.setRollingPolicy(rollingPolicy);
        rollingPolicy.start();
        return rollingPolicy;
    }

    private static void setTriggerPolicy(RollingFileAppender<ILoggingEvent> appender, FixedWindowRollingPolicy rollingPolicy) {
        SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy(); //NOSONAR
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE));
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE));
        appender.setTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.start();
        appender.setRollingPolicy(rollingPolicy);
        rollingPolicy.start();
    }

    private boolean weHaveALoglevelAndItIsNew(Level level) {
        return level != null && level.toInt() != ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLevel().toInt(); // NOSONAR BULL.
    }
}