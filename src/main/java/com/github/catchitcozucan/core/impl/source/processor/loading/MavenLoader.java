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
    private final JarLoader loader;
    private final File m2RepoBaseDir;
    private static MavenLoader INSTANCE;

    private MavenLoader() {
        try {
            loader = JarLoader.getInstance(Thread.currentThread().getContextClassLoader());
            if (!MAVEN_REPO_SETTINGS_FILE.exists()) {
                throw new ProcessRuntimeException(String.format("Maven settings file assumed to be %s does not exist", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            if (!MAVEN_REPO_SETTINGS_FILE.canRead()) {
                throw new ProcessRuntimeException(String.format("Maven settings file assumed to be %s exists but could not be read", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            String m2Content = IO.fileToString(MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), StandardCharsets.UTF_8);
            if (!m2Content.contains(LOCAL_REPOSITORY)) {
                throw new ProcessRuntimeException(String.format("Maven settings file %s does not contain any local repo tag", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()));
            }
            String m2Location = m2Content.substring(m2Content.indexOf(LOCAL_REPOSITORY) + LOCAL_REPOSITORY.length(), m2Content.indexOf(LOCAL_REPOSITORY, m2Content.indexOf(LOCAL_REPOSITORY) + LOCAL_REPOSITORY.length()) - 2);
            m2RepoBaseDir = new File(m2Location);
            if (!m2RepoBaseDir.exists()) {
                throw new ProcessRuntimeException(String.format("Maven settings file %s pointed to local repo %s which does not exist", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), m2RepoBaseDir.getAbsolutePath()));
            }
            if (!m2RepoBaseDir.canRead()) {
                throw new ProcessRuntimeException(String.format("Maven settings file %s pointed to local repo %s which could not be read", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), m2RepoBaseDir.getAbsolutePath()));
            }
            if (!m2RepoBaseDir.isDirectory()) {
                throw new ProcessRuntimeException(String.format("Maven settings file %s pointed to local repo %s which is not a directory", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), m2RepoBaseDir.getAbsolutePath()));
            }
            if (m2RepoBaseDir.list().length < 2) {
                throw new ProcessRuntimeException(String.format("Maven settings file %s pointed to local repo %s which is empty", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), m2RepoBaseDir.getAbsolutePath()));
            }
        } catch (NoSuchMethodException e1) {
            throw new ProcessRuntimeException("Could not initiated jar loader", e1);
        } catch (IOException e2) {
            throw new ProcessRuntimeException(String.format("Could not read up aven settings file contents from %s", MAVEN_REPO_SETTINGS_FILE.getAbsolutePath()), e2);
        }
    }

    public static synchronized MavenLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MavenLoader();
        }
        return INSTANCE;
    }

    public void tryLoadJarsBasedOnModulePath(String pathToJar) {
        if (!IO.hasContents(pathToJar)) {
            throw new ProcessRuntimeException("tryLoadJarsBasedOnModulePath() we cannot work empty paths");
        }
        if (!pathToJar.contains(DOT) && !pathToJar.contains(SLASH)) {
            throw new ProcessRuntimeException(String.format("No path separator elementfound in %s", pathToJar));
        }
        pathToJar = pathToJar.replace(DOT, File.separator);
        if (!SLASH.equals(File.separator)) {
            pathToJar = pathToJar.replace(SLASH, File.separator);
        }
        File dirToLoad = new File(new StringBuilder(m2RepoBaseDir.getAbsolutePath()).append(File.separator).append(pathToJar).toString());
        if (!dirToLoad.exists()) {
            throw new ProcessRuntimeException(String.format("Maven module path %s to append to class path does not exist", dirToLoad.getAbsolutePath()));
        }
        if (!dirToLoad.canRead()) {
            throw new ProcessRuntimeException(String.format("Maven module path %s to append to class path is not readable", dirToLoad.getAbsolutePath()));
        }
        if (dirToLoad.list().length < 2) {
            throw new ProcessRuntimeException(String.format("Maven module path %s to append to class path is empty", dirToLoad.getAbsolutePath()));
        }
        loader.addAllFilesInOlderToClassPath(dirToLoad.getAbsolutePath());
    }

    public Class<?> loadClass(String classPath) {
        if (classPath.startsWith("java")) {
            throw new ProcessRuntimeException(String.format("Please do not try to load the JVM's own classes such as %s through me", classPath));
        }
        Object loadedClass = loader.tryLoadClassAndGetInstance(classPath);
        if (loadedClass.getClass().isAssignableFrom(Boolean.class)) {
            throw new ProcessRuntimeException(String.format("Please do not try to load the JVM's own classes such as %s through me", classPath));
        }
        return loadedClass.getClass();
    }
}
