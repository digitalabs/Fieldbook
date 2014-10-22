package com.efficio.fieldbook.util;

import java.io.IOException;

public class JsonIoException extends IOException{
	
	private static final long serialVersionUID = 5410017966103003826L;
	
	public JsonIoException(String message) {
		super(message);
	}
	/**
	 * Instantiates a new fieldbook exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public JsonIoException(String message, Throwable cause) {
		super(message, cause);
	}
}
