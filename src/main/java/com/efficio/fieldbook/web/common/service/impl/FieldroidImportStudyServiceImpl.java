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
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
@Service
public class FieldroidImportStudyServiceImpl implements
		FieldroidImportStudyService {

	@Override
	public int importWorkbook(Workbook workbook, String filename)
			throws WorkbookParserException {
	
		File file = new File(filename);
		CSVOziel csv = new CSVOziel(workbook, workbook.getObservations(), workbook.getTrialObservations());
		
		validate(csv, file, workbook);
		
		csv.readDATAnew(file);
		return EDIT_ONLY;
	}
	
	private void validate(CSVOziel csv, File file, Workbook workbook) throws WorkbookParserException {
		if (!csv.isValid(file)) {
			throw new WorkbookParserException("error.workbook.import.invalidFieldroidFile");
		}
	}

}
