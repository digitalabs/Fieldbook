
package com.efficio.fieldbook.web.study.service.impl;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

@Service
@Transactional
public class KsuCsvImportStudyServiceImpl extends AbstractCSVImportStudyService implements ImportStudyService {

	public KsuCsvImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName) {
		super(workbook, currentFile, originalFileName);
	}


    @Override
    void validateObservationColumns() throws WorkbookParserException {

        final String[] rowHeaders = parsedData.get(0).toArray(new String[parsedData.get(0).size()]);

        if (!KsuFieldbookUtil.isValidHeaderNames(rowHeaders)) {
            throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
        }
    }

    @Override
    protected String getLabelFromRequiredColumn(MeasurementVariable variable) {
        return KsuFieldbookUtil.getLabelFromKsuRequiredColumn(variable);
    }
}
