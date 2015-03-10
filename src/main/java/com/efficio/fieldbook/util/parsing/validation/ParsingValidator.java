package com.efficio.fieldbook.util.parsing.validation;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 5:18 PM
 */
public abstract class ParsingValidator {

	private String message;
	private boolean skipIfEmpty;

	public ParsingValidator(boolean skipIfEmpty) {
		this.skipIfEmpty = skipIfEmpty;
	}

	public abstract boolean isParsedValueValid(String value);

	public void setValidationErrorMessage(String message) {
		this.message = message;
	}

	public String getValidationErrorMessage() {
		return message;
	}

	public boolean isSkipIfEmpty() {
		return skipIfEmpty;
	}
}
