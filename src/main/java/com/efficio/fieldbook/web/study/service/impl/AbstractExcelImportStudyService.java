
package com.efficio.fieldbook.web.study.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

public abstract class AbstractExcelImportStudyService extends AbstractImportStudyService<Workbook> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractImportStudyService.class);

	public AbstractExcelImportStudyService(final org.generationcp.middleware.domain.etl.Workbook workbook, final String currentFile,
			final String originalFileName) {
		super(workbook, currentFile, originalFileName);
	}

	/**
	 * Current implementation of study imports using Excel files use the Workbook object representing the Excel file as its basis
	 *
	 * TODO : Look into using the results of the Workbook parser for the study import
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	protected Workbook parseObservationData() throws IOException {

		try {
			return new HSSFWorkbook(new FileInputStream(new File(this.currentFile)));
		} catch (final OfficeXmlFileException officeException) {
			LOG.error(officeException.getMessage(), officeException);

			return new XSSFWorkbook(new FileInputStream(new File(this.currentFile)));

		}

	}

    public abstract int getObservationSheetNumber();

	@Override
	protected void performStudyDataImport(final Set<ChangeType> modes, final Workbook parsedData, final Map<String, MeasurementRow> measurementRowsMap,
		final List<GermplasmChangeDetail> changeDetailsList,
		final org.generationcp.middleware.domain.etl.Workbook workbook) throws WorkbookParserException {

		final Map<MeasurementVariable, List<MeasurementVariable>> formulasMap =
			WorkbookUtil.getVariatesMapUsedInFormulas(workbook.getVariates());

		final List<MeasurementVariable> variablesFactors = workbook.getFactors();
		final Sheet observationSheet = parsedData.getSheetAt(this.getObservationSheetNumber());

		final Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<>();
		for (final MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (!Objects.equals(measurementRowsMap,null) && !measurementRowsMap.isEmpty()) {

			workbook.setHasExistingDataOverwrite(false);
			workbook.setPlotsIdNotfound(0);
			int countObsUnitIdNotFound = 0;

			final int lastXlsRowIndex = observationSheet.getLastRowNum();
			final String obsUnitIdIndex = this.getIndexOfObsUnitIdFromXlsSheet(observationSheet, variablesFactors);
			final int desigColumn = this.findColumn(observationSheet, this.getColumnLabel(variablesFactors, TermId.DESIG.getId()));
			final Row headerRow = observationSheet.getRow(0);
			final int lastXlsColIndex = headerRow.getLastCellNum();

			for (int i = 1; i <= lastXlsRowIndex; i++) {
				final Row xlsRow = observationSheet.getRow(i);
				final String obsUnitId = this.getObsUnitIdFromRow(xlsRow, obsUnitIdIndex);
				final MeasurementRow measurementRow = measurementRowsMap.get(obsUnitId);

				if (measurementRow == null) {
					countObsUnitIdNotFound++;
					continue;
				}

				measurementRowsMap.remove(obsUnitId);

				this.validateAndSetNewDesignation(desigColumn, xlsRow, measurementRow);

				for (int j = 0; j <= lastXlsColIndex; j++) {
					final Cell headerCell = headerRow.getCell(j);
					if (headerCell != null) {
						final MeasurementData wData = measurementRow.getMeasurementData(headerCell.getStringCellValue());
						this.importDataCellValues(wData, xlsRow, j, workbook, factorVariableMap);
					}
				}

				this.setMeasurementDataAsOutOfSync(formulasMap, measurementRow);
			}

			if (countObsUnitIdNotFound != 0) {
				workbook.setPlotsIdNotfound(countObsUnitIdNotFound);
			}

		}
	}

	private void setMeasurementDataAsOutOfSync(final Map<MeasurementVariable, List<MeasurementVariable>> formulasMap,
		final MeasurementRow measurementRow) {
		for (final MeasurementVariable measurementVariable : formulasMap.keySet()) {
			final MeasurementData key = measurementRow.getMeasurementData(measurementVariable.getTermId());
			final List<MeasurementVariable> formulas = formulasMap.get(measurementVariable);
			for (final MeasurementVariable formula : formulas) {
				final MeasurementData value = measurementRow.getMeasurementData(formula.getTermId());
				if (key != null && key.isChanged()) {
					value.setValueStatus(Phenotype.ValueStatus.OUT_OF_SYNC);
				}
			}
		}
	}

	private void validateAndSetNewDesignation(final int desigColumn, final Row xlsRow, final MeasurementRow measurementRow)
		throws WorkbookParserException {

		final String newDesig = this.getDesignation(xlsRow, desigColumn);

		this.setNewDesignation(measurementRow, newDesig);
	}

	protected String getIndexOfObsUnitIdFromXlsSheet(final Sheet observationSheet, final List<MeasurementVariable> variables)
		throws WorkbookParserException {
		String obsUnitIdLabel = null;
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.OBS_UNIT_ID.getId()) {
				obsUnitIdLabel = variable.getName();
				break;
			}
		}

		if (obsUnitIdLabel != null) {
			return this.findColumns(observationSheet, obsUnitIdLabel);
		}

		throw new WorkbookParserException("error.workbook.import.plot.id.empty.cell");
	}

	private String getObsUnitIdFromRow(final Row xlsRow, final String index) throws WorkbookParserException {
		final String obsUnitId = this.getCellValue(xlsRow.getCell(Integer.valueOf(index)));

		if (StringUtils.isBlank(obsUnitId)) {
			throw new WorkbookParserException("error.workbook.import.plot.id.empty.cell");
		}

		return obsUnitId;
	}

	protected void importDataCellValues(
		final MeasurementData workbookMeasurementData, final Row xlsRow, final int columnIndex,
		final org.generationcp.middleware.domain.etl.Workbook workbook, final Map<Integer, MeasurementVariable> factorVariableMap) {
		if (workbookMeasurementData != null && workbookMeasurementData.isEditable()) {
			final Cell cell = xlsRow.getCell(columnIndex);
			final String xlsValue;
			if (cell != null && this.hasCellValue(cell)) {
				if (workbookMeasurementData.getMeasurementVariable() != null && workbookMeasurementData.getMeasurementVariable().getPossibleValues() != null
						&& !workbookMeasurementData.getMeasurementVariable().getPossibleValues().isEmpty()) {

					workbookMeasurementData.setAccepted(false);
					String tempVal;

					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						final double doubleVal = cell.getNumericCellValue();
						final double intVal = Double.valueOf(cell.getNumericCellValue()).intValue();
						boolean getDoubleVal = false;
						if (doubleVal - intVal > 0) {
							getDoubleVal = true;
						}

						tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
						if (getDoubleVal) {
							tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()));
						}
						xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(tempVal,
								workbookMeasurementData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell.getStringCellValue();
						xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(),
								workbookMeasurementData.getMeasurementVariable().getPossibleValues(), true);
					}

					if (workbookMeasurementData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							&& !xlsValue.equals(tempVal)) {
						workbookMeasurementData.setcValueId(xlsValue);
					} else {
						workbookMeasurementData.setcValueId(null);
					}

				} else {

					if (workbookMeasurementData.getMeasurementVariable() != null
						&& workbookMeasurementData.getMeasurementVariable().getDataTypeId() != null &&
							workbookMeasurementData.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
						workbookMeasurementData.setAccepted(false);
					}
					xlsValue = this.getCellValue(cell);
				}

				if (!workbookMeasurementData.getValue().equals(xlsValue)) {
					if (!workbookMeasurementData.getValue().isEmpty()) {
						workbook.setHasExistingDataOverwrite(true);
					}
					if (workbookMeasurementData.getMeasurementVariable().getFormula() != null) {
						workbookMeasurementData.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
					}
					workbookMeasurementData.setValue(xlsValue);
					workbookMeasurementData.setOldValue(xlsValue);
					workbookMeasurementData.setChanged(true);
				}
			}
		}
	}

	private String getCellValue(final Cell cell) {
		String realValue = "";
		if (cell != null) {
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				final Double doubleVal = cell.getNumericCellValue();
				final Integer intVal = doubleVal.intValue();
				if (Double.parseDouble(intVal.toString()) == doubleVal) {
					realValue = intVal.toString();
				} else {
					realValue = doubleVal.toString();
				}
			} else {
				realValue = cell.getStringCellValue();
			}
		}
		return realValue;
	}

	protected boolean hasCellValue(final Cell cell) {
		if (cell == null) {
			return false;
		} else if (PoiUtil.getCellValue(cell) == null) {
			return false;
		} else if ("".equals(PoiUtil.getCellValue(cell).toString())) {
			return false;
		}
		return true;
	}

	protected String findColumns(final Sheet sheet, final String... cellValue) throws WorkbookParserException {
		final List<String> cellValueList = Arrays.asList(cellValue);
		String result = StringUtils.join(cellValue, ",");

		final Row row = sheet.getRow(0);
		final int cells = row.getLastCellNum();
		for (int i = 0; i < cells; i++) {
			final Cell cell = row.getCell(i);
			if (cell == null) {
				throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
			} else if (cellValueList.contains(cell.getStringCellValue())) {
				result = result.replace(cell.getStringCellValue(), String.valueOf(i));
			}
		}
		return result;
	}

	protected String getColumnLabel(final org.generationcp.middleware.domain.etl.Workbook workbook, final int termId) {
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		return this.getColumnLabel(variables, termId);
	}

	protected String getColumnLabel(final List<MeasurementVariable> variables, final int termId) {
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == termId) {
				return variable.getName();
			}
		}
		return null;
	}

	protected int findColumn(final Sheet sheet, final String cellValue) throws WorkbookParserException {
		final int result = -1;
		if (cellValue != null) {
			final Row row = sheet.getRow(0);
			final int cells = row.getLastCellNum();
			for (int i = 0; i < cells; i++) {
				final Cell cell = row.getCell(i);
				if (cell == null) {
					throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
				} else if (cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
					return i;
				}
			}
		}
		return result;
	}

	private String getDesignation(final Row xlsRow,final int desigColumn) throws WorkbookParserException{
		final Cell desigCell = xlsRow.getCell(desigColumn);
		if (desigCell == null) {
			throw new WorkbookParserException("error.workbook.import.designation.empty.cell");
		}
		return desigCell.getStringCellValue().trim();
	}

	private String getPlotNo(final MeasurementRow wRow) {
		String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
		if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
			plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
		}
		return plotNumber;
	}

}
