package com.github.catchitcozucan.core.impl;

import com.github.catchitcozucan.core.ErrorCodeCarrier;

class InternalProcessOtherRuntimeException extends RuntimeException implements ErrorCodeCarrier {

	private static final int NO_ERROR = 0;
	private final int errorCode;

	protected InternalProcessOtherRuntimeException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = NO_ERROR;
	}

	@Override
	public int getErrorCode() {
		return errorCode;
	}
}
