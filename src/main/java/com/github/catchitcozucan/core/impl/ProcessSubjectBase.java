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
package com.github.catchitcozucan.core.impl;

import java.io.Serializable;

import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.domain.ToStringBuilder;


public abstract class ProcessSubjectBase extends BaseDomainObject implements ProcessSubject, Serializable {

	protected Enum<?> status;
	private Integer errorCode;
	private String processName;
	private static final String IDENTFIER = "id";
	private static final String SUBJECT_IDENTIFIER = "subjectIdentifier";

	public enum Status {
		PRE_PROCESS('£'), PROCESS_FINISHED('¤');
		private final Character sts;

		Status(Character sts) {
			this.sts = sts;
		}

		public Character getSts() {
			return sts;
		}
	}

	public void setStatus(Enum<?> status) {
		this.status = status;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode == null ? 0 : errorCode;
	}

	boolean hasError() {
		return errorCode != null;
	}

	void clearError() {
		this.errorCode = null;
	}

	@Override
	public String doToString() {
		return new ToStringBuilder(IDENTFIER, id()).append(SUBJECT_IDENTIFIER, subjectIdentifier()).toString();
	}

	Enum<?> getStatus() { // NOSONAR
		return status;
	}

	@Override
	public Enum getCurrentStatus() {
		return status;
	}

	@Override
	public String getCurrentProcess() {
		return processName;
	}
}
