/**
 *    Copyright [2020] [Ola Aronsson, courtesy of nollettnoll AB]
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
