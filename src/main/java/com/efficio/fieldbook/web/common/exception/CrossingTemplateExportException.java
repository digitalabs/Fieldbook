package com.efficio.fieldbook.web.common.exception;

/**
 * Created by cyrus on 2/13/15.
 */
public class CrossingTemplateExportException extends Exception {
	public CrossingTemplateExportException(String message) {
		super(message);
	}

	public CrossingTemplateExportException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
