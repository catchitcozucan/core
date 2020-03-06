package com.github.catchitcozucan.core.demo.test.support.io.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.catchitcozucan.core.demo.test.support.io.IO;
import com.github.catchitcozucan.core.internal.util.domain.StringUtils;


public class SerializationService {

	private static SerializationService instance = null;
	public static final String STOREPATH = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(".serializations").toString();
	boolean imSilent;


	public void setSilent(boolean imSilent) {
		this.imSilent = true;
	}

	public static enum OP {
		LOAD, SAVE, DELETE, SEARCH, SERIALIZE;
	}

	;

	private String storePath;

	private SerializationService() {
		IO.makeOrUseDir(STOREPATH);
		this.storePath = STOREPATH;
	}

	private SerializationService(String storeDirectoryPath) {
		this.storePath = storeDirectoryPath;
	}

	public static synchronized SerializationService getInstance() {
		if (instance == null) {
			instance = new SerializationService();
		}
		return instance;
	}

	public static synchronized SerializationService getInstance(String storeDirectoryPath) {
		// AccessService.getInstance().hasAccess(user, UserService.class);
		if (instance == null) {
			instance = new SerializationService(storeDirectoryPath);
		}
		return instance;
	}

	public synchronized void performTest() {
		System.out.println();
		System.out.println("Performing serialization tests");
		Integer test = 666;
		File saved = (File) perform(OP.SAVE, test);
		Object loaded = perform(OP.LOAD, saved);
		System.out.println(MessageFormat.format("We have just loaded an that should have value 666. It is : {0}", loaded));
		perform(OP.DELETE, saved);
		System.out.println("Done.");
		System.out.println();
	}

	/**
	 * Method for searching the file store for serialized instances of type
	 * class
	 *
	 * @param clazz
	 * @return loaded serialized instances
	 */
	public synchronized List<Object> search(Class<?> clazz) {
		String className = clazz.getName().toLowerCase();
		return search(clazz, OP.LOAD, className);
	}

	/**
	 * Method for searching the file store for serialized instances of the same
	 * type as provided object instance
	 *
	 * @param object
	 * @return loaded serialized instances
	 */
	public synchronized List<Object> search(Object object) {
		String className = object.getClass().getName().toLowerCase();
		return search(object.getClass(), OP.LOAD, className);
	}

	public Object perform(OP operation, Object object) {
		return perform(operation, object, null);
	}

	/**
	 * Method for performing the three standard IO type operations on the file
	 * store
	 *
	 * @param operation operation top be carried out
	 * @param object    object to be operated upon
	 * @return Object referenced by/from the filestore - the
	 */
	public synchronized Object perform(OP operation, Object object, String storePath) {
		String className = object.getClass().getName().toLowerCase();
		switch (operation) {
			case DELETE:
				return delete(object, OP.DELETE, className, storePath);
			case LOAD:
				return load(object, OP.LOAD, className, storePath);
			case SAVE:
				return save(object, OP.SAVE, className, storePath);
			default:
				throw new IllegalStateException(MessageFormat.format("Got non-supported IO operation {0}", operation.name()));
		}
	}

	// --------------------- PRIVATE
	// -------------------------------------------------

	private String delete(Object object, OP op, String className, String storePath) {
		File file = null;
		String path = null;
		if (object instanceof String) {
			file = new File((String) object);
		} else if (object instanceof File) {
			file = (File) object;
		}
		if (file == null) {
			file = (File) getSerilizationFileHandle(op, className, object.hashCode(), storePath);
			path = file.getAbsolutePath();
		} else {
			if (!file.delete()) {
				throw new IllegalStateException(MessageFormat.format("Could not {0} file {1}", op.name(), path));
			}
		}
		if (!imSilent) {
			System.out.println(MessageFormat.format("Performed {0} for {1} on file {2}", op, className, path));
		}
		return path;
	}

	private List<Object> search(Object object, OP op, String className) {
		StringBuilder b = new StringBuilder().append("^").append(((Class<?>) object).getName().toLowerCase()).append("_[0-9]?.+.ser$");
		final String regExpForClassSerilizations = b.toString();
		FileFilter ff = new FileFilter() {

			@Override
			public boolean accept(File file) {
				return (file.getName().matches(regExpForClassSerilizations));
			}

		};
		File[] filesInStore = new File(storePath).listFiles(ff);
		List<Object> objectsFound = new ArrayList<Object>();
		if (filesInStore.length == 0) {
			return objectsFound;
		} else {
			for (File f : filesInStore) {
				objectsFound.add(fileToObject(op, className, f));
			}
			return objectsFound;
		}
	}

	private Object load(Object object, OP op, String className, String storePath) {
		int hashCode = object.hashCode();
		File fileHandle = null;
		if (object instanceof Class) {
			throw new IllegalArgumentException("Please use search methods when trying to load any found class serilizations!");
		} else if (object instanceof File) {
			fileHandle = (File) object;
		} else {
			fileHandle = getSerilizationFileHandle(op, className, hashCode, storePath);
		}
		return fileToObject(op, className, fileHandle);
	}

	private Object fileToObject(OP op, String className, File fileHandle) {
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(fileHandle);
			in = new ObjectInputStream(fileIn);
			Object readUp = in.readObject();
			if (!imSilent) {
				System.out.println(MessageFormat.format("Performed {0} for {1} on file {2}", op, className, fileHandle.getAbsolutePath()));
			}
			return readUp;
		} catch (IOException i) {
			throw new RuntimeException(MessageFormat.format("Could not perform {0} for {1} on file {2}", op, className, fileHandle.getAbsolutePath()), i);
		} catch (ClassNotFoundException i) {
			throw new RuntimeException(MessageFormat.format("Could not perform {0} for {1} on file {2}", op, className, fileHandle.getAbsolutePath()), i);
		} finally {
			IO.closeQuietly(in);
			IO.closeQuietly(fileIn);
		}
	}

	private String save(Object object, OP op, String className, String storePath) {
		File fileHandle = getSerilizationFileHandle(op, className, object.hashCode(), storePath);
		String path = fileHandle.getAbsolutePath();
		FileOutputStream fileOut = null;
		ObjectOutputStream out = null;
		try {
			fileOut = new FileOutputStream(fileHandle);
			out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			if (!imSilent) {
				System.out.println(MessageFormat.format("Performed {0} for {1} on file {2}", op, className, path));
			}
		} catch (IOException i) {
			throw new RuntimeException(MessageFormat.format("Could not perform {0} for {1} on file {2}", op, className, path), i);
		} finally {
			IO.closeQuietly(out);
			IO.closeQuietly(fileOut);
		}
		return path;
	}

	/**
	 * Method returning the file instance to be operated upon; it will also
	 * perform/test that instructed operation will succeed
	 *
	 * @param operation the IO-type operation to be carried out
	 * @param className the class name of the actual entity being handled
	 * @param hashCode  the hash code of the object instance being operated upon
	 * @return the file store file reference
	 */
	private File getSerilizationFileHandle(OP operation, String className, int hashCode, String storePathIncoming) {
		String serializationPath = null;
		if (!StringUtils.isBlank(storePathIncoming)) {
			serializationPath = new StringBuilder(storePathIncoming).append(File.separator).append(className).append("_").append(hashCode).append(".ser").toString();
		} else {
			serializationPath = new StringBuilder(storePath).append(File.separator).append(className).append("_").append(hashCode).append(".ser").toString();
		}
		File serializationFile = new File(serializationPath);
		switch (operation) {
			case DELETE:
				if (serializationFile.exists() && !serializationFile.delete()) {
					throw new IllegalStateException(MessageFormat.format("Could not {0} file {1}", operation.name(), serializationFile.getAbsolutePath()));
				}
				return serializationFile;
			case LOAD:
				if (!serializationFile.canRead()) {
					throw new IllegalStateException(MessageFormat.format("Could not {0} file {1}", operation.name(), serializationFile.getAbsolutePath()));
				}
				return serializationFile;
			case SAVE:
				if (serializationFile.exists()) {
					if (!serializationFile.delete()) {
						throw new IllegalStateException(MessageFormat.format("Could not {0} file {1}", operation.name(), serializationFile.getAbsolutePath()));
					}
				}
				try {
					serializationFile.createNewFile();
				} catch (IOException e) {
					throw new IllegalStateException(MessageFormat.format("Could not {0} file {1}", operation.name(), serializationFile.getAbsolutePath()), e);
				}
				return serializationFile;
			default:
				throw new IllegalStateException(MessageFormat.format("Got non-supported IO operation {0}", operation.name()));
		}
	}
}
