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
package com.efficio.fieldbook.web.nursery.service.impl;

import java.io.File;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;

import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.nursery.service.FieldroidImportStudyService;

public class FieldroidImportStudyServiceImpl implements
		FieldroidImportStudyService {

	@Override
	public void importWorkbook(Workbook workbook, String filename)
			throws WorkbookParserException {
	
		File file = new File(filename);
		CSVOziel csv = new CSVOziel(workbook);
		
		validate(csv, file);
		
	}
	
	private void validate(CSVOziel csv, File file) throws WorkbookParserException {
		if (!csv.isValid(file)) {
			throw new WorkbookParserException("error.workbook.import.invalidFieldroidFile");
		}
	}

}
