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

package com.efficio.fieldbook.web.study.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.study.service.ImportStudyService;

@Transactional
public class FieldroidImportStudyServiceImpl extends AbstractImportStudyService<CSVOziel> implements ImportStudyService {

	public FieldroidImportStudyServiceImpl(Workbook workbook, String currentFile, String originalFileName) {
		super(workbook, currentFile, originalFileName);
	}

	@Override
	protected void detectAddedTraitsAndPerformRename(Set modes) {
		// no added trait checking for Fieldroid
	}

	@Override
	void validateObservationColumns() throws WorkbookParserException {
		// intentionally left blank. validation self contained in CSVOziel class
	}

	@Override
	void validateImportMetadata() throws WorkbookParserException {
		if (!parsedData.isValid(new File(currentFile))) {
			throw new WorkbookParserException("error.workbook.import.invalidFieldroidFile");
		}
	}

	@Override
	protected CSVOziel parseObservationData() throws IOException {

		return new CSVOziel(workbook, workbook.getObservations(), workbook.getTrialObservations());

	}

	@Override
	protected void performStudyDataImport(Set<ChangeType> modes, CSVOziel parsedData, Map<String, MeasurementRow> rowsMap,
			String trialInstanceNumber, List<GermplasmChangeDetail> changeDetailsList, Workbook workbook) throws WorkbookParserException {
		final File file = new File(currentFile);
		parsedData.readDATAnew(file, ontologyService, fieldbookMiddlewareService);
	}

}
