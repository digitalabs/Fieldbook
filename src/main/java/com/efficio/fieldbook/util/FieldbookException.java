/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.util;

public class FieldbookException extends Exception {

	private static final long serialVersionUID = -1639961960516233500L;

	public FieldbookException(String message) {
		super(message);
	}

	public FieldbookException(String message, Throwable cause) {
		super(message, cause);
	}
}
