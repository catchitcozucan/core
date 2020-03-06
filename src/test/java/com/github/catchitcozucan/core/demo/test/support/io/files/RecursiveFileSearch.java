package com.github.catchitcozucan.core.demo.test.support.io.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.catchitcozucan.core.internal.util.domain.StringUtils;

public class RecursiveFileSearch {

	private final File root;
	private FileFilter includeExceludeFileFilter;
	private boolean includeDirs;

	public RecursiveFileSearch(File root, String includedInFilenames, boolean caseSensitive) {
		this.root = root;
		this.includeExceludeFileFilter = new IncludeExceludeFileFilter(includedInFilenames, caseSensitive);
	}

	public List<File> match() {
		List<File> files = new ArrayList<File>();
		if (!root.canRead()) {
			return files;
		}
		if (!root.isDirectory()) {
			files.add(root);
			return files;
		}
		files.addAll(listFileTree(root));
		return files;
	}

	private Collection<File> listFileTree(File dir) {

		Set<File> fileTree = new HashSet<File>();
		if (dir.isDirectory() && includeDirs) {
			fileTree.add(dir);
		}

		for (File entry : dir.listFiles(includeExceludeFileFilter)) {
			if (entry.isFile() && entry.canRead()) {
				fileTree.add(entry);
			} else {

				if (entry.isDirectory() && includeDirs) {
					fileTree.add(entry);
				}

				fileTree.addAll(listFileTree(entry));
			}
		}
		return fileTree;
	}

	private static class IncludeExceludeFileFilter implements FilenameFilter, FileFilter {

		private final String include;
		private final String exclude;
		private boolean caseSensitive;
		private final Mode mode;

		private enum Mode {
			INCLUDE_ONLY, EXCLUDE_ONLY, INCLUDE_AND_EXCLUDE, NO_FILTER
		}

		public IncludeExceludeFileFilter(String includeOrExclude, boolean include) {
			if (include) {
				this.include = includeOrExclude;
				this.exclude = null;
				this.mode = getMode(includeOrExclude, null);
			} else {
				this.exclude = includeOrExclude;
				this.include = null;
				this.mode = getMode(null, includeOrExclude);
			}
			this.caseSensitive = false;
		}

		@Override
		public boolean accept(File file) {
			return accept_inner(file.getName());
		}

		@Override
		public boolean accept(File dir, String name) {
			return accept_inner(name);
		}

		private boolean accept_inner(String fileName) {
			switch (mode) {
				case INCLUDE_AND_EXCLUDE:
					if (!caseSensitive) {
						return fileName.toLowerCase().contains(include.toLowerCase()) && !fileName.toLowerCase().contains(exclude.toLowerCase());
					} else {
						return fileName.contains(include) && !fileName.contains(exclude);
					}
				case INCLUDE_ONLY:
					if (!caseSensitive) {
						return fileName.toLowerCase().contains(include.toLowerCase());
					} else {
						return fileName.contains(include);
					}
				case EXCLUDE_ONLY:
					if (!caseSensitive) {
						return !fileName.toLowerCase().contains(exclude.toLowerCase());
					} else {
						return !fileName.contains(exclude);
					}
				default:
					return true;
			}
		}

		private Mode getMode(String include, String exclude) {
			if (StringUtils.isBlank(include) && StringUtils.isBlank(exclude)) {
				return Mode.NO_FILTER;
			} else if (StringUtils.isBlank(include) && !StringUtils.isBlank(exclude)) {
				return Mode.EXCLUDE_ONLY;
			} else if (!StringUtils.isBlank(include) && StringUtils.isBlank(exclude)) {
				return Mode.INCLUDE_ONLY;
			} else {
				return Mode.INCLUDE_AND_EXCLUDE;
			}
		}
	}
}
