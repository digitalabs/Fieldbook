
package com.efficio.fieldbook.web.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.KsuExcelImportStudyService;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil.KsuRequiredColumnEnum;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
public class KsuExcelImportStudyServiceImpl extends ExcelImportStudyServiceImpl implements KsuExcelImportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(KsuExcelImportStudyServiceImpl.class);

	@Override
	public ImportResult importWorkbook(Workbook workbook, String filename, OntologyService ontologyService,
			FieldbookService fieldbookMiddlewareService) throws WorkbookParserException {

		try {
			// read the file
			org.apache.poi.ss.usermodel.Workbook xlsBook = this.parseFile(filename);

			this.validate(xlsBook);

			String trialInstanceNumber = this.getTrialInstanceNo(workbook, xlsBook.getSheetName(0));

			Map<String, MeasurementRow> rowsMap =
					this.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNumber, workbook.isNursery());

			Set<ChangeType> modes = new HashSet<ChangeType>();
			List<GermplasmChangeDetail> changeDetailsList = new ArrayList<GermplasmChangeDetail>();
			this.importDataToWorkbook(modes, xlsBook.getSheetAt(0), rowsMap, trialInstanceNumber, changeDetailsList, workbook);

			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			this.validationService.validateObservationValues(workbook, trialInstanceNumber);

			return new ImportResult(new HashSet<ChangeType>(), new ArrayList<GermplasmChangeDetail>());

		} catch (IOException e) {
			throw new WorkbookParserException(e.getMessage(), e);
		} catch (MiddlewareQueryException e) {
			KsuExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			WorkbookUtil.resetWorkbookObservations(workbook);
			return new ImportResult(e.getMessage());
		} catch (WorkbookParserException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw e;
		}
	}

	protected String getTrialInstanceNo(Workbook workbook, String filename) throws WorkbookParserException {
		String trialInstanceNumber = workbook != null && workbook.isNursery() ? "1" : this.getTrialInstanceNoFromFileName(filename);
		if (trialInstanceNumber == null || "".equalsIgnoreCase(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}
		return trialInstanceNumber;
	}

	protected String getTrialInstanceNoFromFileName(String filename) throws WorkbookParserException {
		String trialInstanceNumber = "";

		Integer startIndex = filename.lastIndexOf("-") + 1;
		Integer endIndex = filename.lastIndexOf(".");

		String pattern = "(.+)[-](\\d+)[\\.]xls";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(filename);
		if (m.find()) {
			trialInstanceNumber = filename.substring(startIndex, endIndex);
		}

		if (!NumberUtils.isNumber(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}

		return trialInstanceNumber;
	}

	protected void validate(org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {

		this.validateNumberOfSheets(xlsBook);

		Sheet observationSheet = xlsBook.getSheetAt(0);
		String[] headerNames = this.getColumnHeaders(observationSheet);
		if (!this.isValidHeaderNames(headerNames)) {
			throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
		}
	}

	protected boolean isValidHeaderNames(String[] headerNames) {
		return KsuFieldbookUtil.isValidHeaderNames(headerNames);
	}

	@Override
	protected void validateNumberOfSheets(org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 1) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}

	protected String[] getColumnHeaders(Sheet sheet) throws WorkbookParserException {
		Row row = sheet.getRow(0);
		int noOfColumnHeaders = row.getLastCellNum();

		String[] headerNames = new String[noOfColumnHeaders];
		for (int i = 0; i < noOfColumnHeaders; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
			}

			headerNames[i] = cell.getStringCellValue();
		}

		return headerNames;
	}

	@Override
	protected String getColumnIndexesFromXlsSheet(Sheet observationSheet, List<MeasurementVariable> variables, String trialInstanceNumber)
			throws WorkbookParserException {
		String plotLabel = null, entryLabel = null;
		for (MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = this.getLabelFromKsuRequiredColumn(variable);
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = this.getLabelFromKsuRequiredColumn(variable);
			}
		}
		if (plotLabel != null && entryLabel != null) {
			String indexes = this.findColumns(observationSheet, trialInstanceNumber, plotLabel, entryLabel);
			for (String index : indexes.split(",")) {
				if (!NumberUtils.isNumber(index) || "-1".equalsIgnoreCase(index)) {
					return null;
				}
			}
			return indexes;
		}
		return null;
	}

	protected String getLabelFromKsuRequiredColumn(MeasurementVariable variable) {
		String label = "";

		if (KsuRequiredColumnEnum.get(variable.getTermId()) != null) {
			label = KsuRequiredColumnEnum.get(variable.getTermId()).getLabel();
		}

		if (label.trim().length() > 0) {
			return label;
		}

		return variable.getName();
	}
}
