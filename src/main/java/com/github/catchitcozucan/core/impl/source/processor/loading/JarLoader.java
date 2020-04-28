package com.github.catchitcozucan.core.impl.source.processor.loading;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.EMPTY;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;

public class JarLoader {

    private static final String ADD_URL = "addURL";
    private static final String NUMBERS = "[0-9]";
    private static final String JAR = ".jar";
    private static JarLoader INSTANCE;
    List<String> pathsLoaded;
    private final Method addURL;
    private final ClassLoader classLoader;

    private JarLoader(ClassLoader classLoader) throws NoSuchMethodException {
        addURL = URLClassLoader.class.getDeclaredMethod(ADD_URL, new Class[] { URL.class });
        addURL.setAccessible(true);
        pathsLoaded = new ArrayList<>();
        this.classLoader = classLoader;
    }

    public static synchronized JarLoader getInstance(ClassLoader classLoader) throws NoSuchMethodException {
        if (INSTANCE == null) {
            INSTANCE = new JarLoader(classLoader);
        }
        return INSTANCE;
    }

    public void addAllFilesInOlderToClassPath(String folder) {
        if (!pathsLoaded.contains(folder)) {
            doLoadTheseUrls(getJarFilesInDir(doChecks(folder), null));
            pathsLoaded.add(folder);
        }
    }

    public Object tryLoadClassAndGetInstance(String namedClassFullPath) {
        Object resp = tryLoadClass(namedClassFullPath, true);
        if (resp instanceof Boolean) {
            boolean k = (Boolean) resp;
            if (!k) {
                return null;
            } else {
                return k;
            }
        } else {
            return resp;
        }
    }

    private Object tryLoadClass(String namedClassFullPath, boolean getInstance) {
        try {
            Class<?> cl = classLoader.loadClass(namedClassFullPath);
            if (getInstance) {
                try {
                    Object ret = cl.newInstance();
                    return ret;
                } catch (InstantiationException e) {
                    return new Boolean(false);
                } catch (IllegalAccessException e) {
                    return new Boolean(false);
                }
            } else {
                return new Boolean(true);
            }
        } catch (ClassNotFoundException e) {
            return new Boolean(false);
        }
    }

    private void doLoadTheseUrls(List<URL> selFiles) {
        try {
            loadTheseUrlsIntotheClasspath(selFiles);
        } catch (IllegalArgumentException e) {
            throw new ProcessRuntimeException("Could not load urls onto classpath", e);
        } catch (IllegalAccessException e) {
            throw new ProcessRuntimeException("Could not load urls onto classpath", e);
        } catch (InvocationTargetException e) {
            throw new ProcessRuntimeException("Could not load urls onto classpath", e);
        }
    }

    private void loadTheseUrlsIntotheClasspath(List<URL> selFiles) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (URL u : selFiles) {
            addURL.invoke(classLoader, new Object[] { u });
            classLoader.getResourceAsStream(u.toExternalForm());
        }
    }

    private static File doChecks(String absPath) {
        File dir = new File(absPath);
        if (!dir.exists()) {
            throw new IllegalArgumentException("Expected dir " + absPath + " does not exist!");
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Expected dir " + absPath + " is not a folder!");
        }
        return dir;
    }

    private List<URL> getJarFilesInDir(File dir, String toMatch) {
        List<URL> selFiles = new ArrayList<>();
        List<File> dirsToProcess = getJarFileUrls(toMatch, 0, selFiles, dir);
        if (!dirsToProcess.isEmpty()) {
            List<URL> urls = new ArrayList<>();
            List<URL> urlz = new ArrayList<>();
            urlz.addAll(selFiles);
            dirsToProcess.stream().forEach(d -> {
                getJarFileUrls(toMatch, 1, urlz, d);
            });
            selFiles.addAll(urlz);
        }
        if (selFiles.size() > 1) {
            AtomicInteger index = new AtomicInteger(selFiles.size()-1);
            File[] toCheck = new File[selFiles.size()];
            selFiles.stream().forEach(f -> {
                toCheck[index.getAndDecrement()] = new File(f.getFile());
            });
            File[] checked = filterOutElderFilesWithSimilarFilesNames(toCheck, 5, true, true);
            final List<URL> chechedUrlz = new ArrayList<>();
            Arrays.stream(checked).forEach(ff -> {
                URL u = null;
                try {
                    u = ff.toURI().toURL();
                } catch (MalformedURLException ignore) {}
                if (u != null) {
                    chechedUrlz.add(u);
                }
            });
            return chechedUrlz;
        }
        return selFiles;
    }

    private List<File> getJarFileUrls(String toMatch, int depth, List<URL> accumResult, File rootDir) {
        File[] files = rootDir.listFiles();
        List<File> dirsToProcess = new ArrayList<>();
        for (File f : files) {
            try {
                if (f.isFile() && f.getName().endsWith(JAR)) {
                    if (toMatch == null) {
                        URL u = f.toURI().toURL();
                        if (!accumResult.contains(u)) {
                            accumResult.add(u);
                        }
                    } else {
                        if (f.getName().contains(toMatch)) {
                            URL u = f.toURI().toURL();
                            if (!accumResult.contains(u)) {
                                accumResult.add(u);
                            }
                        }
                    }
                } else if (depth == 0 && f.isDirectory() && f.canRead()) {
                    dirsToProcess.add(f);
                }
            } catch (MalformedURLException ignore) {}
        }
        return dirsToProcess;
    }

    private static File[] filterOutElderFilesWithSimilarFilesNames(File[] files, int minimumLcs, boolean ignoreNumbers, boolean mavenStyle) {

        List<String> index = new ArrayList<>();
        List<File> selFiles = new ArrayList<File>();
        for (File f : files) {
            List<FileCompareEntity> hits = new ArrayList<>();
            for (File other : files) {

                String srcName = stringify(f, mavenStyle, ignoreNumbers);
                String otherName = stringify(other, mavenStyle, ignoreNumbers);
                if (!otherName.equals(srcName)) {
                    String lcs = firstCommonSequence(otherName.replace(JAR, EMPTY), srcName.replace(JAR, EMPTY));
                    if (lcs != null && lcs.length() > minimumLcs) {
                        if (other.lastModified() > f.lastModified()) {
                            hits.add(new FileCompareEntity(other, lcs));
                        }
                    }
                }
            }

            if (hits.size() > 0) {
                int longestMatch = 0;
                for (FileCompareEntity entry : hits) {
                    if (entry.geLcsLen() > longestMatch) {
                        longestMatch = entry.geLcsLen();
                    }
                }

                for (FileCompareEntity entry : hits) {
                    if (entry.geLcsLen() == longestMatch) {
                        selFiles.add(entry.getFile());
                        break;
                    }
                }
            } else {
                String key = stringify(f, mavenStyle, ignoreNumbers);
                if (!index.contains(key)) {
                    selFiles.add(f);
                    index.add(key);
                }
            }
        }
        return selFiles.toArray(new File[selFiles.size()]);
    }

    private static String stringify(File f, boolean mavenStyle, boolean ignoreNumbers) {
        String ff = f.getName();
        if (mavenStyle) {
            ff = ff.substring(0, ff.indexOf("-"));
        }
        if (ignoreNumbers || mavenStyle) {
            ff = ff.replaceAll(NUMBERS, EMPTY);
        }
        return ff;
    }

    public static String firstCommonSequence(String a, String b) {
        char[] src = a.toCharArray();
        char[] other = b.toCharArray();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < src.length && i < other.length; i++) {
            if (src[i] == other[i]) {
                res.append(src[i]);
            } else {
                break;
            }
        }
        if (res.length() < 1) {
            return null;
        } else {
            return res.toString();
        }
    }
}
