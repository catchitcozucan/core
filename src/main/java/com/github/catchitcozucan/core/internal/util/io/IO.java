package com.github.catchitcozucan.core.internal.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("restriction")
public class IO {

    private IO() {
    }

    /**
     * Default encoding to use
     */
    public static final String DEF_ENCODING = "UTF-8";

    public static File makeOrUseDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new FileExistsButIsNotADirectoryException();
            } else if (!dir.canWrite()) {
                throw new DirExistsButCannotBeWritteToException();
            } else {
                return dir;
            }
        } else {
            if (!dir.mkdirs()) {
                throw new CouldNotCreateDirException();
            } else {
                return dir;
            }
        }
    }

    /**
     * Method to delete all files recursively from a given starting point
     *
     * @param fileOrDir the starting point or file to delete
     * @return true if we succeed
     */
    public static boolean deleteRecursively(File fileOrDir) {
        if (fileOrDir.exists()) {
            if (fileOrDir.isDirectory()) {
                return deleteRecursively(fileOrDir, null);
            } else {
                return fileOrDir.delete(); // NOSONAR
            }
        }
        return true;// resultCode already achieved..
    }

    public static void overwriteStringToFileWithEncoding(String path, String content, String encoding) throws IOException {
        Writer out = null;
        if (hasContents(encoding)) {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path), encoding));
        } else {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path)));
        }
        try {
            out.write(content);
        } finally {
            out.close();
        }
    }

    public static String fileToString(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        if (encoding != null) {
            return new String(encoded, encoding);
        } else {
            return new String(encoded);
        }
    }

    private static boolean deleteRecursively(File rootDir, File[] dirs) { // NOSONAR COMPLEX stuff..

        if (dirs == null) {
            List<File> oldDirs = new ArrayList<>();
            for (File g : rootDir.listFiles()) {
                if (g.isDirectory()) {
                    oldDirs.add(g);
                } else if (g.isFile()) {
                    g.delete(); // NOSONAR
                }
            }
            dirs = oldDirs.toArray(new File[oldDirs.size()]);
        }

        List<File> newDirs = null;
        if (dirs != null && dirs.length > 0) {
            for (File dir : dirs) {
                final File[] files = dir.listFiles();
                newDirs = new ArrayList<>();
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete(); // NOSONAR
                    } else if (f.isDirectory()) {
                        newDirs.add(f);
                    }
                }
            }
        }

        if (newDirs != null && !newDirs.isEmpty()) {
            deleteRecursively(rootDir, newDirs.toArray(new File[newDirs.size()]));
        } else {
            if (!rootDir.delete()) { // NOSONAR
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception ignore) {} // NOSONAR BULL.
        }
    }

    public static boolean hasContents(String input) {
        return input != null && input.trim().length() > 0;
    }

    public static class FileExistsButIsNotADirectoryException extends RuntimeException {
    }

    public static class DirExistsButCannotBeWritteToException extends RuntimeException {
    }

    public static class CouldNotCreateDirException extends RuntimeException {
    }
}
