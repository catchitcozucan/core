package com.github.catchitcozucan.core.demo.test.support.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;

import com.github.catchitcozucan.core.demo.test.support.io.files.RecursiveFileSearch;

@SuppressWarnings("restriction")
public class IO {

	private enum Type {
		File, String
	}

	public static final String UTF_8 = "UTF-8";

	public static final int DEF_READ_SIZE = (8 * 1024);

	public static void closeQuietly(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException ignore) {
			}
		}
	}

	public static void sleep(long millisec) {
		try {
			Thread.sleep(millisec); // NOSONAR, used in tests..
		} catch (InterruptedException ignore) {
		}
	}

	public static void setFieldValue(Object instanceToSetItTo, String namedField, Object value) {
		try {
			Field f = instanceToSetItTo.getClass().getDeclaredField(namedField);
			f.setAccessible(true);
			f.set(instanceToSetItTo, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not assign field : access problem", e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Could not assign field : it was not found", e);
		}
	}

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

	public static File[] locateFilesRecursively(String rootDir, String toMatch, boolean incluseDirsInfiltering) {
		return locateFilesRecursively(new File(rootDir), toMatch);
	}

	public static File[] locateFilesRecursively(File rootDir, final String toMatch) {
		if (!rootDir.exists() || !rootDir.canRead()) {
			throw new IllegalArgumentException(MessageFormat.format("file {0} is either not readable or simply does not exist", rootDir.getAbsolutePath()));
		}
		RecursiveFileSearch fs = new RecursiveFileSearch(rootDir, toMatch, true);
		List<File> matches = fs.match();
		return matches.toArray(new File[matches.size()]);
	}

	public static void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (Exception ignore) {
			}
		}
	}

	public static class FileExistsButIsNotADirectoryException extends RuntimeException {
	}

	public static class DirExistsButCannotBeWritteToException extends RuntimeException {
	}

	public static class CouldNotCreateDirException extends RuntimeException {
	}

}
