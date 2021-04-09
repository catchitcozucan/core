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
package com.github.catchitcozucan.core.impl.source.processor.loading;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.DOT;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.SLASH;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class MavenLoader {
    private static final File MAVEN_REPO_SETTINGS_FILE = new File(new StringBuilder(System.getProperty("user.home")).append(File.separator).append(".m2").append(File.separator).append("settings.xml").toString());
    private static final String LOCAL_REPOSITORY = "localRepository>";
    private static final String COULD_NOT_INITIATED_JAR_LOADER = "Could not initiated jar loader";
    private static final String MAVEN_SETTINGS_FILE_ASSUMED_TO_BE_S_DOES_NOT_EXIST = "Maven settings file assumed to be %s does not exist";
    private static final String MAVEN_SETTINGS_FILE_ASSUMED_TO_BE_S_EXISTS_BUT_COULD_NOT_BE_READ = "Maven settings file assumed to be %s exists but could not be read";
    private static final String MAVEN_SETTINGS_FILE_S_DOES_NOT_CONTAIN_ANY_LOCAL_REPO_TAG = "Maven settings file %s does not contain any local repo tag";
    private static final String COULD_NOT_READ_UP_EVEN_SETTINGS_FILE_CONTENTS_FROM_S = "Could not read up even settings file contents from %s";
    private static final String TRY_LOAD_JARS_BASED_ON_MODULE_PATH_WE_CANNOT_WORK_EMPTY_PATHS = "tryLoadJarsBasedOnModulePath() we cannot work empty paths";
    private static final String NO_PATH_SEPARATOR_ELEMENTFOUND_IN_S = "No path separator elementfound in %s";
    private static final String MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_DOES_NOT_EXIST = "Maven module path %s to append to class path does not exist";
    private static final String MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_IS_NOT_READABLE = "Maven module path %s to append to class path is not readable";
    private static final String MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_IS_EMPTY = "Maven module path %s to append to class path is empty";
    private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST = "Maven settings file %s pointed to local repo %s which does not exist";
    private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_COULD_NOT_BE_READ = "Maven settings file %s pointed to local repo %s which could not be read";
    private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY = "Maven settings file %s pointed to local repo %s which is not a directory";
    private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY = "Maven settings file %s pointed to local repo %s which is empty";
    private static final String PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME = "Please do not try to load the JVM's own classes such as %s through me";
    private static final String PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME1 = "Please do not try to load the JVM's own classes such as %s through me";
    private static final String JAVA = "java";
    private final JarLoader loader;
    private static MavenLoader INSTANCE; //NOSONAR

    private MavenLoader() {
        try {
            loader = JarLoader.getInstance(Thread.currentThread().getContextClassLoader());
        } catch (NoSuchMethodException e1) {
            throw new ProcessRuntimeException(COULD_NOT_INITIATED_JAR_LOADER, e1);
        }
    }

    private File fetchMavenrepoPathFromSettingsFile() {
        try {
            if (!MAVEN_REPO_SETTINGS_FILE.exists()) {
                throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_ASSUMED_TO_BE_S_DOES_NOT_EXIST, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            if (!MAVEN_REPO_SETTINGS_FILE.canRead()) {
                throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_ASSUMED_TO_BE_S_EXISTS_BUT_COULD_NOT_BE_READ, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            String m2Content = IO.fileToString(MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), StandardCharsets.UTF_8);
            if (!m2Content.contains(LOCAL_REPOSITORY)) {
                throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_DOES_NOT_CONTAIN_ANY_LOCAL_REPO_TAG, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            return new File(m2Content.substring(m2Content.indexOf(LOCAL_REPOSITORY) + LOCAL_REPOSITORY.length(), m2Content.indexOf(LOCAL_REPOSITORY, m2Content.indexOf(LOCAL_REPOSITORY) + LOCAL_REPOSITORY.length()) - 2));
        } catch (IOException e2) {
            throw new ProcessRuntimeException(String.format(COULD_NOT_READ_UP_EVEN_SETTINGS_FILE_CONTENTS_FROM_S, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()), e2);
        }
    }

    public static synchronized MavenLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MavenLoader();
        }
        return INSTANCE;
    }

    public void tryLoadJarsBasedOnModulePath(String pathToJar, String repoPath) {
        if (!IO.hasContents(pathToJar)) {
            throw new ProcessRuntimeException(TRY_LOAD_JARS_BASED_ON_MODULE_PATH_WE_CANNOT_WORK_EMPTY_PATHS);
        }
        if (!pathToJar.contains(DOT) && !pathToJar.contains(SLASH)) {
            throw new ProcessRuntimeException(String.format(NO_PATH_SEPARATOR_ELEMENTFOUND_IN_S, pathToJar));
        }
        pathToJar = pathToJar.replace(DOT, File.separator);
        if (!SLASH.equals(File.separator)) {
            pathToJar = pathToJar.replace(SLASH, File.separator);
        }

        File m2RepoBaseDir = null;
        if (IO.hasContents(repoPath)) {
            m2RepoBaseDir = new File(repoPath);
        } else {
            m2RepoBaseDir = fetchMavenrepoPathFromSettingsFile();
        }
        testRepoDir(m2RepoBaseDir);

        File dirToLoad = new File(new StringBuilder(m2RepoBaseDir.getAbsolutePath()).append(File.separator).append(pathToJar).toString());
        if (!dirToLoad.exists()) {
            throw new ProcessRuntimeException(String.format(MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_DOES_NOT_EXIST, dirToLoad.getAbsolutePath()));
        }
        if (!dirToLoad.canRead()) {
            throw new ProcessRuntimeException(String.format(MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_IS_NOT_READABLE, dirToLoad.getAbsolutePath()));
        }
        if (dirToLoad.list().length < 2) {
            throw new ProcessRuntimeException(String.format(MAVEN_MODULE_PATH_S_TO_APPEND_TO_CLASS_PATH_IS_EMPTY, dirToLoad.getAbsolutePath()));
        }
        loader.addAllFilesInOlderToClassPath(dirToLoad.getAbsolutePath());
    }

    private void testRepoDir(File repoDir) {
        if (!repoDir.exists()) {
            throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
        }
        if (!repoDir.canRead()) {
            throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_COULD_NOT_BE_READ, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
        }
        if (!repoDir.isDirectory()) {
            throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
        }
        if (repoDir.list().length < 2) {
            throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
        }
    }

    public Class<?> loadClass(String classPath) {
        if (classPath.startsWith(JAVA)) {
            throw new ProcessRuntimeException(String.format(PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME, classPath));
        }
        Object loadedClass = loader.tryLoadClassAndGetInstance(classPath);
        if (loadedClass.getClass().isAssignableFrom(Boolean.class)) {
            throw new ProcessRuntimeException(String.format(PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME1, classPath));
        }
        return loadedClass.getClass();
    }
}