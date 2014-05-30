/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.common.service;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;

import com.efficio.fieldbook.web.common.bean.ImportResult;

public interface ImportStudyService {

	final static int EDIT_ONLY = 0;
	final static int ADD_ONLY = 1;
	final static int DELETE_ONLY = 2;
	final static int MIXED = 3;
	
	ImportResult importWorkbook(Workbook workbook, String filename) throws WorkbookParserException;
}
