
package com.efficio.fieldbook.web.study.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.efficio.fieldbook.web.study.service.ImportStudyService;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.util.ImportStudyUtil;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

import javax.annotation.Resource;

@Service
@Transactional
public class KsuExcelImportStudyServiceImpl extends ExcelImportStudyServiceImpl implements ImportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(KsuExcelImportStudyServiceImpl.class);

    @Resource
    private OntologyService ontologyService;

    @Resource
    private FieldbookService fieldbookMiddlewareService;

	@Override
	public ImportResult importWorkbook(final Workbook workbook, final String currentFile, final String originalFilename) throws WorkbookParserException {

		try {
			// read the file
			final org.apache.poi.ss.usermodel.Workbook xlsBook = this.parseFile(currentFile);

			this.validate(xlsBook);

			final String trialInstanceNumber = ImportStudyUtil.getTrialInstanceNo(workbook, xlsBook.getSheetName(0));

			final Map<String, MeasurementRow> rowsMap =
					ImportStudyUtil.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNumber, workbook.isNursery());

			final Set<ChangeType> modes = new HashSet<ChangeType>();
			final List<GermplasmChangeDetail> changeDetailsList = new ArrayList<GermplasmChangeDetail>();
			this.importDataToWorkbook(modes, xlsBook.getSheetAt(0), rowsMap, trialInstanceNumber, changeDetailsList, workbook);

			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			this.validationService.validateObservationValues(workbook, trialInstanceNumber);

			return new ImportResult(new HashSet<ChangeType>(), new ArrayList<GermplasmChangeDetail>());

		} catch (final IOException e) {
			throw new WorkbookParserException(e.getMessage(), e);
		} catch (final MiddlewareQueryException e) {
			KsuExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			WorkbookUtil.resetWorkbookObservations(workbook);
			return new ImportResult(e.getMessage());
		} catch (final WorkbookParserException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw e;
		}
	}

	protected void validate(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {

		this.validateNumberOfSheets(xlsBook);

		final Sheet observationSheet = xlsBook.getSheetAt(0);
		final String[] headerNames = this.getColumnHeaders(observationSheet);
		if (!this.isValidHeaderNames(headerNames)) {
			throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
		}
	}

	protected boolean isValidHeaderNames(final String[] headerNames) {
		return KsuFieldbookUtil.isValidHeaderNames(headerNames);
	}

	@Override
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
