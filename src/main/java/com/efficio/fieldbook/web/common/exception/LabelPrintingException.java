
package com.efficio.fieldbook.web.common.exception;

public class LabelPrintingException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -8996509611008955257L;
	private String errorCode;
	private String labelError;

	public LabelPrintingException(final String errorCode, final String labelError, final String message) {
		super(message);
		this.errorCode = errorCode;
		this.labelError = labelError;
	}

    public LabelPrintingException(final Throwable cause) {
        super(cause);
    }

	public LabelPrintingException(final String message) {
		super(message);
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(final String errorCode) {
		this.errorCode = errorCode;
	}

	public String getLabelError() {
		return this.labelError;
	}

	public void setLabelError(final String labelError) {
		this.labelError = labelError;
	}

}
