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
package com.github.catchitcozucan.core.impl.source.processor.loading;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.DOT;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.SLASH;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.info;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.warn;

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
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST = "Maven settings file %s pointed to local repo %s which does not exist";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE = "Maven settings file %s pointed to local repo %s which seems functional yet module %s does not exists there";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE_R = "Maven settings file %s pointed to local repo %s is not readable";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE_READ = "Maven settings file %s pointed to local repo %s which seems functional yet module %s is not readable";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST = "Provided path to local repo %s does not exist";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE = "Provided path to local repo %s seems functional yet module path %s does not exist";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_READ = "Provided path to local repo %s is not readable";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_DIRECTORY_MODULE = "Provided path to local repo %s seems functional yet module path %s is not a directory";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_READ_MODULE = "Provided path to local repo %s seems functional yet module path %s is not readable";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY = "Maven settings file %s pointed to local repo %s which is not a directory";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY_MODULE = "Maven settings file %s pointed to local repo %s which seems functional yet module path %s is not a directory";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY = "Maven settings file %s pointed to local repo %s which is empty";
	private static final String MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY_MODULE = "Maven settings file %s pointed to local repo %s which seems functional yet module path %s is empty";
	private static final String PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME = "Please do not try to load the JVM's own classes such as %s through me";
	private static final String PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME_BOOL = "Please do not try to load the JVM's own classes such as %s through me - like Boolean";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NON_DIR = "Provided path to local repo %s which is not a directory";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY = "Provided path to local repo %s which is empty";
	private static final String PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY_MODULE = "Provided path to local repo %s which seems functional yet module path %s is empty";
	private static final String JAVA = "java";
	public static final String ATTEMPTING_LOADING_IT_VIA_YOUR_SETTINGS_XML_HOME_M_2_SETTINGS_XML = "            Attempting loading it via your settings.xml ($HOME/.m2/settings.xml)..";
	public static final String FAILED_LOCATING_YOUR_MAVEN_REPO_VIA_PROVIDED_PATH_S = "            Failed locating your maven repo via provided path %s : %s";
	private final JarLoader loader;
	private static MavenLoader INSTANCE; //NOSONAR

	private enum TestType {
		FOR_MAVEN_REPO_LOCATION, FOR_ACTUAL_MODULE_IN_REPO
	}

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
		boolean basedOnSettingsXml = false;
		boolean foundAGoodRepoPath = false;

		// if we got an actual direct path, try it out!
		if (IO.hasContents(repoPath)) {
			m2RepoBaseDir = new File(repoPath);
			try {
				testRepoDir(m2RepoBaseDir, null, basedOnSettingsXml, TestType.FOR_MAVEN_REPO_LOCATION);
				foundAGoodRepoPath = true;
			} catch (ProcessRuntimeException failure) {
				warn(String.format(FAILED_LOCATING_YOUR_MAVEN_REPO_VIA_PROVIDED_PATH_S, repoPath, failure.getMessage()));
				info(ATTEMPTING_LOADING_IT_VIA_YOUR_SETTINGS_XML_HOME_M_2_SETTINGS_XML);
			}
		}

		// it seems we got no path or we failed. try the settings.xml
		if (!foundAGoodRepoPath) {
			m2RepoBaseDir = fetchMavenrepoPathFromSettingsFile();
			basedOnSettingsXml = true;
			testRepoDir(m2RepoBaseDir, null, basedOnSettingsXml, TestType.FOR_MAVEN_REPO_LOCATION);
		}

		// so - we know the repo s readable and contains stuff - let's try loading the actual module's jars onto
		// our classpath
		File dirToLoad = new File(new StringBuilder(m2RepoBaseDir.getAbsolutePath()).append(File.separator).append(pathToJar).toString());
		testRepoDir(m2RepoBaseDir, dirToLoad, basedOnSettingsXml, TestType.FOR_ACTUAL_MODULE_IN_REPO);
		loader.addAllFilesInOlderToClassPath(dirToLoad.getAbsolutePath());
	}

	private boolean testRepoDir(File repoDir, File moduleDir, boolean basedOnSettingsXML, TestType testType) {
		return chkExists(repoDir, moduleDir, basedOnSettingsXML, testType)  //NOSONAR.. it will throw or chain..
				&& chkCanRead(repoDir, moduleDir, basedOnSettingsXML, testType)  //NOSONAR.. it will throw or chain..
				&& chkIsDirectory(repoDir, moduleDir, basedOnSettingsXML, testType)  //NOSONAR.. it will throw or chain..
				&& chkHasContents(repoDir, moduleDir, basedOnSettingsXML, testType); //NOSONAR.. it will throw or chain..
	}

	private boolean chkHasContents(File repoDir, File moduleDir, boolean basedOnSettingsXML, TestType testType) {
		if (repoDir.list().length < 2) {
			if (basedOnSettingsXML) {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY_MODULE, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			} else {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY, repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_EMPTY_MODULE, repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			}
		}
		return true;
	}

	private boolean chkIsDirectory(File repoDir, File moduleDir, boolean basedOnSettingsXML, TestType testType) {
		if (!repoDir.isDirectory()) {
			if (basedOnSettingsXML) {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NOT_A_DIRECTORY_MODULE, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			} else {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_IS_NON_DIR, repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_DIRECTORY_MODULE, repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			}
		}
		return true;
	}

	private boolean chkCanRead(File repoDir, File moduleDir, boolean basedOnSettingsXML, TestType testType) {
		if (!repoDir.canRead()) {
			if (basedOnSettingsXML) {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE_R, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE_READ, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			} else {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_READ, repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_READ_MODULE, repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			}
		}
		return true;
	}

	private boolean chkExists(File repoDir, File moduleDir, boolean basedOnSettingsXML, TestType testType) {
		if (!repoDir.exists()) {
			if (basedOnSettingsXML) {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(MAVEN_SETTINGS_FILE_S_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE, MAVEN_REPO_SETTINGS_FILE.getAbsolutePath(), repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			} else {
				if (testType.equals(TestType.FOR_MAVEN_REPO_LOCATION)) {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST, repoDir.getAbsolutePath()));
				} else {
					throw new ProcessRuntimeException(String.format(PATH_POINTED_TO_LOCAL_REPO_S_WHICH_DOES_NOT_EXIST_MODULE, repoDir.getAbsolutePath(), moduleDir.getAbsolutePath()));
				}
			}
		}
		return true;
	}

	public Class<?> loadClass(String classPath) {
		if (classPath.startsWith(JAVA)) {
			throw new ProcessRuntimeException(String.format(PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME, classPath));
		}
		Object loadedClass = loader.tryLoadClassAndGetInstance(classPath);
		if (loadedClass.getClass().isAssignableFrom(Boolean.class)) {
			throw new ProcessRuntimeException(String.format(PLEASE_DO_NOT_TRY_TO_LOAD_THE_JVM_S_OWN_CLASSES_SUCH_AS_S_THROUGH_ME_BOOL, classPath));
		}
		return loadedClass.getClass();
	}
}