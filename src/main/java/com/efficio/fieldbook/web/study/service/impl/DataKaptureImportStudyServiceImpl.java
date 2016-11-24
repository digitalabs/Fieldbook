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
public class DataKaptureImportStudyServiceImpl extends AbstractImportStudyService<CSVOziel> implements ImportStudyService {


    public DataKaptureImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName) {
        super(workbook, currentFile, originalFileName);
    }

    @Override
    protected void detectAddedTraitsAndPerformRename(final Set modes) {
        // no added trait checking for DataKapture format
    }

    @Override
    void validateObservationColumns() throws WorkbookParserException {
        // self contained within the CSVOziel class
    }

    @Override
    void validateImportMetadata() throws WorkbookParserException {
        // self contained within the CSVOziel class
    }

    @Override
    protected CSVOziel parseObservationData() throws IOException {
        return new CSVOziel(workbook, workbook.getObservations(), workbook.getTrialObservations(), true);
    }

    @Override
    protected void performStudyDataImport(final Set modes, final CSVOziel parsedData, final Map rowsMap, final String trialInstanceNumber, final List changeDetailsList, final Workbook workbook) throws WorkbookParserException {
        final File file = new File(currentFile);
        parsedData.readDATACapture(file, ontologyService, fieldbookMiddlewareService);
    }

	@Override
	protected void detectAddedTraitsAndPerformRename(Set<ChangeType> modes, List<String> addedVariates,
			List<String> removedVariates) {
		// TODO Auto-generated method stub
		
	}

}
