package com.github.catchitcozucan.core.demo.test.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import com.github.catchitcozucan.core.internal.util.domain.StringUtils;
import com.github.catchitcozucan.core.demo.test.support.io.IO;

public class MD5Digest {

	private static MD5Digest instance;
	private final MessageDigest md;

	private enum DigestType {
		FILE, MESSAGE
	}

	private MD5Digest() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not intialize MD5Digester - no algorithm", e);
		}
	}

	public static synchronized MD5Digest getInstance() {
		if (instance == null) {
			instance = new MD5Digest();
		}
		return instance;
	}

	public String digest(String filePath) {
		return digest(new File(filePath));
	}

	public String digest(File f) {
		if (f == null) {
			throw new IllegalArgumentException("File was null");
		}
		if (!f.exists()) {
			throw new IllegalArgumentException(MessageFormat.format("File {0} does not exist", f.getAbsolutePath()));
		}
		if (!f.canRead()) {
			throw new IllegalArgumentException(MessageFormat.format("File {0} cannot be read", f.getAbsolutePath()));
		}
		try {
			byte[] hash = md.digest(fileOrMessageToBytes(f, null, DigestType.FILE));
			return bytes2HEX(hash);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Could not digest file {0}", f.getAbsolutePath()), e);
		}
	}

	public String digestMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message was null");
		}
		if (!StringUtils.hasContents(message)) {
			throw new IllegalArgumentException("Message was empty");
		}

		try {
			byte[] hash = md.digest(fileOrMessageToBytes(null, message, DigestType.MESSAGE));
			return bytes2HEX(hash);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Could not digest message {0}", message), e);
		}
	}

	private byte[] fileOrMessageToBytes(File f, String message, DigestType digestType) throws IOException {
		InputStream is = null;
		byte[] result;
		try {
			if (digestType.equals(DigestType.FILE)) {
				is = new FileInputStream(f);
			} else {
				is = new ByteArrayInputStream(message.getBytes("UTF-8"));
			}
			result = inputStreamToBytes(is, 4096);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Could not extract content from file {0}", f.getAbsolutePath()));
		} finally {
			IO.close(is);
		}
		return result;
	}

	private String bytes2HEX(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
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
