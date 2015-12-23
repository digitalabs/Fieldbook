package com.efficio.fieldbook.web.exception;

public class ApiRequestValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;

	public ApiRequestValidationException(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

}
