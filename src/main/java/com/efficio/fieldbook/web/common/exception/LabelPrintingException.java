package com.efficio.fieldbook.web.common.exception;

public class LabelPrintingException extends Exception {
	private String errorCode;
	private String labelError;
	public LabelPrintingException(String errorCode, String labelError, String message) {
        super(message);
        this.errorCode = errorCode;
        this.labelError = labelError;
    }
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getLabelError() {
		return labelError;
	}
	public void setLabelError(String labelError) {
		this.labelError = labelError;
	}
	
	
}
