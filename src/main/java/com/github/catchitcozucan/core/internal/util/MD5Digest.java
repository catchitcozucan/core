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
package com.github.catchitcozucan.core.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class MD5Digest {

	private static final String FILE_0_DOES_NOT_EXIST = "File {0} does not exist";
	private static final String FILE_0_CANNOT_BE_READ = "File {0} cannot be read";
	private static final String COULD_NOT_DIGEST_FILE_0 = "Could not digest file {0}";
	private static final String MESSAGE_WAS_NULL = "Message was null";
	private static final String MESSAGE_WAS_EMPTY = "Message was empty";
	private static final String COULD_NOT_DIGEST_MESSAGE_0 = "Could not digest message {0}";
	private static final String UTF_8 = "UTF-8";
	private static final int BUFFER_SIZE = 4096;
	private static final String HEX_FORMAT = "%02X";
	private static final String COULD_NOT_EXTRACT_CONTENT_FROM_FILE_0 = "Could not extract content from file {0}";
	private static final String COULD_NOT_INTIALIZE_MD_5_DIGESTER_NO_ALGORITHM = "Could not intialize MD5Digester - no algorithm";
	private static final String MD_5 = "MD5";
	private static MD5Digest instance;
	private final MessageDigest md;

	private enum DigestType {
		FILE, MESSAGE
	}

	private MD5Digest() {
		try {
			md = MessageDigest.getInstance(MD_5);
		} catch (NoSuchAlgorithmException e) {
			throw new ProcessRuntimeException(COULD_NOT_INTIALIZE_MD_5_DIGESTER_NO_ALGORITHM, e);
		}
	}

	public static synchronized MD5Digest getInstance() {
		if (instance == null) {
			instance = new MD5Digest();
		}
		return instance;
	}

	public String digest(File f) {
		if (f == null) {
			throw new IllegalArgumentException("File was null");
		}
		if (!f.exists()) {
			throw new IllegalArgumentException(MessageFormat.format(FILE_0_DOES_NOT_EXIST, f.getAbsolutePath()));
		}
		if (!f.canRead()) {
			throw new IllegalArgumentException(MessageFormat.format(FILE_0_CANNOT_BE_READ, f.getAbsolutePath()));
		}
		try {
			byte[] hash = md.digest(fileOrMessageToBytes(f, null, DigestType.FILE));
			return bytes2HEX(hash);
		} catch (IOException e) {
			throw new ProcessRuntimeException(MessageFormat.format(COULD_NOT_DIGEST_FILE_0, f.getAbsolutePath()), e);
		}
	}

	public String digestMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException(MESSAGE_WAS_NULL);
		}
		if (!IO.hasContents(message)) {
			throw new IllegalArgumentException(MESSAGE_WAS_EMPTY);
		}

		try {
			byte[] hash = md.digest(fileOrMessageToBytes(null, message, DigestType.MESSAGE));
			return bytes2HEX(hash);
		} catch (IOException e) {
			throw new ProcessRuntimeException(MessageFormat.format(COULD_NOT_DIGEST_MESSAGE_0, message), e);
		}
	}

	private byte[] fileOrMessageToBytes(File f, String message, DigestType digestType) throws IOException {
		InputStream is = null;
		byte[] result;
		try { // NOSONAR
			if (digestType.equals(DigestType.FILE)) {
				is = new FileInputStream(f);
			} else {
				is = new ByteArrayInputStream(message.getBytes(UTF_8)); // NOSONAR
			}
			result = inputStreamToBytes(is, BUFFER_SIZE);
		} catch (IOException e) {
			throw new ProcessRuntimeException(MessageFormat.format(COULD_NOT_EXTRACT_CONTENT_FROM_FILE_0, f.getAbsolutePath()));
		} finally {
			IO.close(is);
		}
		return result;
	}

	private String bytes2HEX(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format(HEX_FORMAT, b));
		}
		return sb.toString();
	}

	private static byte[] inputStreamToBytes(InputStream inputstream, int bufferSize) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[bufferSize];
		while ((nRead = inputstream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}
}
