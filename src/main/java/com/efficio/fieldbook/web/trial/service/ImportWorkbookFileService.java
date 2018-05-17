/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.service;

import java.io.File;
import java.io.IOException;

import com.efficio.fieldbook.web.common.bean.UserSelection;

/**
 * File service for the dataset workbook import needs.
 *
 * @author Joyce Avestro
 *
 */
public interface ImportWorkbookFileService {

	/**
	 * Returns the File object based on the file name stored in UserSelection.
	 * 
	 * @param userSelection
	 * @return
	 * @throws IOException
	 */
	File retrieveCurrentWorkbookAsFile(UserSelection userSelection) throws IOException;
}
