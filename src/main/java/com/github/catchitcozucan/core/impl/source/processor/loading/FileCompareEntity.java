package com.github.catchitcozucan.core.impl.source.processor.loading;

import java.io.File;

public class FileCompareEntity {

	private File f;
	private String lcs;

	public FileCompareEntity(File f, String lcs) {
		this.lcs = lcs;
		this.f = f;
	}

	public File getFile() {
		return f;
	}

	public int geLcsLen() {
		return lcs.length();
	}

	public String getLcs() {
		return lcs;
	}

}
