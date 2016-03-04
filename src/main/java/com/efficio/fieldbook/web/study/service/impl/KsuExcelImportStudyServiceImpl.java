
package com.efficio.fieldbook.web.study.service.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

@Transactional
public class KsuExcelImportStudyServiceImpl extends AbstractExcelImportStudyService implements ImportStudyService {

    public KsuExcelImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName){
        super(workbook, currentFile, originalFileName);
    }

    @Override
    protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes) {
        // TODO added trait checking / header renaming
    }

    @Override
    void validateObservationColumns() throws WorkbookParserException {
        final Sheet observationSheet = parsedData.getSheetAt(0);
        final String[] headerNames = this.getColumnHeaders(observationSheet);
        if (!this.isValidHeaderNames(headerNames)) {
            throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
        }
    }

    @Override
    void validateImportMetadata() throws WorkbookParserException {
        this.validateNumberOfSheets(parsedData);
    }

    protected boolean isValidHeaderNames(final String[] headerNames) {
		return KsuFieldbookUtil.isValidHeaderNames(headerNames);
	}

	protected void validateNumberOfSheets(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 1) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}

	protected String[] getColumnHeaders(final Sheet sheet) throws WorkbookParserException {
		final Row row = sheet.getRow(0);
		final int noOfColumnHeaders = row.getLastCellNum();

		final String[] headerNames = new String[noOfColumnHeaders];
		for (int i = 0; i < noOfColumnHeaders; i++) {
			final Cell cell = row.getCell(i);
			if (cell == null) {
				throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
			}

			headerNames[i] = cell.getStringCellValue();
		}

		return headerNames;
	}

	@Override
	protected String getColumnIndexesFromXlsSheet(final Sheet observationSheet, final List<MeasurementVariable> variables,
			final String trialInstanceNumber) throws WorkbookParserException {
		String plotLabel = null, entryLabel = null;
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(variable);
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(variable);
			}
		}
		if (plotLabel != null && entryLabel != null) {
			final String indexes = this.findColumns(observationSheet, trialInstanceNumber, plotLabel, entryLabel);
			for (final String index : indexes.split(",")) {
				if (!NumberUtils.isNumber(index) || "-1".equalsIgnoreCase(index)) {
					return null;
				}
			}
			return indexes;
		}
		return null;
	}
}