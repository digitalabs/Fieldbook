package com.efficio.fieldbook.web.common.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/25/2015
 * Time: 11:04 AM
 */
public class FileParsingException extends Exception{

	private final String message;
	private final int errorRowIndex;
	private final String errorValue;
	private final String errorColumn;

	public FileParsingException() {
		this(null, 0, null, null);
	}

	public FileParsingException(String message) {
		this(message, 0, null, null);
	}

	public FileParsingException(String message, int errorRowIndex, String errorValue,
			String errorColumn) {
		this.message = message;
		this.errorRowIndex = errorRowIndex;
		this.errorValue = errorValue;
		this.errorColumn = errorColumn;
	}

	@Override public String getMessage() {
		return message;
	}

	public int getErrorRowIndex() {
		return errorRowIndex;
	}

	public String getErrorValue() {
		return errorValue;
	}

	public String getErrorColumn() {
		return errorColumn;
	}

	public Object[] getMessageParameters() {
		return new Object[] {errorRowIndex, errorColumn, errorValue};
	}
}
