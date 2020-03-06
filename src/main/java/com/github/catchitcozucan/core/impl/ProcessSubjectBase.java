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
