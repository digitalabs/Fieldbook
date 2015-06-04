/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service.api;

import java.util.List;

/**
 * Handles errors thrown at the Middleware.
 */
public interface ErrorHandlerService {

	/**
	 * Gets the error messages as list.
	 *
	 * @param errorCodes the error codes
	 * @param parameters the parameters
	 * @return the error messages as list
	 */
	List<String> getErrorMessagesAsList(String errorCodes, Object[] parameters);

	/**
	 * Gets the error messages as string.
	 *
	 * @param errorCodes the error codes
	 * @param parameters the parameters
	 * @param nextLine the next line
	 * @return the error messages as string
	 */
	String getErrorMessagesAsString(String errorCodes, Object[] parameters, String nextLine);
}
