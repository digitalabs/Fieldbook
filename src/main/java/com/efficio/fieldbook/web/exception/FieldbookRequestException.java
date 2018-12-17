package com.efficio.fieldbook.web.exception;

public class FieldbookRequestException extends RuntimeException {

	private static final long serialVersionUID = -5159592964572276588L;

	private final String errorCode;

	public FieldbookRequestException(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

}
