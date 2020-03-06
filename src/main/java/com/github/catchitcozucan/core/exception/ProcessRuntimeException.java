package com.github.catchitcozucan.core.exception;

import com.github.catchitcozucan.core.ErrorCodeCarrier;

public class ProcessRuntimeException extends RuntimeException implements ErrorCodeCarrier {

	private static final int NO_ERROR = 0;
	private final int errorCode;

	public ProcessRuntimeException() {
		super();
		this.errorCode = NO_ERROR;
	}

	public ProcessRuntimeException(String message) {
		super(message);
		this.errorCode = NO_ERROR;
	}

	public ProcessRuntimeException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = NO_ERROR;
	}

	public ProcessRuntimeException(Throwable cause) {
		super(cause);
		this.errorCode = NO_ERROR;
	}

	public ProcessRuntimeException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public ProcessRuntimeException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public ProcessRuntimeException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ProcessRuntimeException(Throwable cause, int errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}

	@Override
	public int getErrorCode() {
		return errorCode;
	}
}
