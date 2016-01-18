package com.efficio.fieldbook.web.exception;

public class FieldbookRequestValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;

	public FieldbookRequestValidationException(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

}
