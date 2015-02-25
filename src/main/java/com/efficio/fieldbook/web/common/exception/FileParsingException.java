package com.efficio.fieldbook.web.common.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/25/2015
 * Time: 11:04 AM
 */
public class FileParsingException extends Exception{

	private String[] messages;

	public FileParsingException() {
		super();
	}

	public FileParsingException(String internationalizableMessage) {
		if (messages == null) {
			messages = new String[1];
		}

		messages[0] = internationalizableMessage;
	}

	public FileParsingException(String[] messages) {
		this.messages = messages;
	}

	public String[] getMessages() {
		return messages;
	}
}
