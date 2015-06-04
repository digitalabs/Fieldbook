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

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;

@Service
public class FieldroidImportStudyServiceImpl implements FieldroidImportStudyService {

	@Override
	public ImportResult importWorkbook(Workbook workbook, String filename, OntologyService ontologyService,
			FieldbookService fieldbookMiddlewareService) throws WorkbookParserException {

		File file = new File(filename);
		CSVOziel csv = new CSVOziel(workbook, workbook.getObservations(), workbook.getTrialObservations());

		this.validate(csv, file, workbook);

		csv.readDATAnew(file, ontologyService, fieldbookMiddlewareService);
		Set<ChangeType> modes = new HashSet<ChangeType>();
		return new ImportResult(modes, new ArrayList<GermplasmChangeDetail>());
	}

	private void validate(CSVOziel csv, File file, Workbook workbook) throws WorkbookParserException {
		if (!csv.isValid(file)) {
			throw new WorkbookParserException("error.workbook.import.invalidFieldroidFile");
		}
	}

}
