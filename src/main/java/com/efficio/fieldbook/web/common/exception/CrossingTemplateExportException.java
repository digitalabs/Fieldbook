
package com.efficio.fieldbook.web.common.exception;

/**
 * Created by cyrus on 2/13/15.
 */
public class CrossingTemplateExportException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4976888301866579819L;

	public CrossingTemplateExportException(String message) {
		super(message);
	}

	public CrossingTemplateExportException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
