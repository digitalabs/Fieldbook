
package com.efficio.fieldbook.web.study.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
			return new HSSFWorkbook(new FileInputStream(new File(currentFile)));
		} catch (final OfficeXmlFileException officeException) {
			LOG.error(officeException.getMessage(), officeException);

			return new XSSFWorkbook(new FileInputStream(new File(currentFile)));

		}

	}

    public abstract int getObservationSheetNumber();

	@Override
	protected void performStudyDataImport(final Set<ChangeType> modes, final Workbook parsedData,
			final Map<String, MeasurementRow> rowsMap, final String trialInstanceNumber,
			final List<GermplasmChangeDetail> changeDetailsList, final org.generationcp.middleware.domain.etl.Workbook workbook)
			throws WorkbookParserException {

		final List<MeasurementVariable> variablesFactors = workbook.getFactors();
		final List<MeasurementRow> observations = workbook.getObservations();
		final Sheet observationSheet = parsedData.getSheetAt(getObservationSheetNumber());

		final Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<>();
		for (final MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (rowsMap != null && !rowsMap.isEmpty()) {

			workbook.setHasExistingDataOverwrite(false);
			final int lastXlsRowIndex = observationSheet.getLastRowNum();
			final String indexes = this.getColumnIndexesFromXlsSheet(observationSheet, variablesFactors, trialInstanceNumber);
			final int desigColumn = this.findColumn(observationSheet, this.getColumnLabel(variablesFactors, TermId.DESIG.getId()));
			final Row headerRow = observationSheet.getRow(0);
			final int lastXlsColIndex = headerRow.getLastCellNum();

			for (int i = 1; i <= lastXlsRowIndex; i++) {
				final Row xlsRow = observationSheet.getRow(i);
				final String key = this.getKeyIdentifierFromXlsRow(xlsRow, indexes);
				if (key != null) {
					final MeasurementRow wRow = rowsMap.get(key);
					if (wRow == null) {
						throw new WorkbookParserException("confirmation.import.add.or.delete.rows");
					} else {
						rowsMap.remove(key);

						final String originalDesig = wRow.getMeasurementDataValue(TermId.DESIG.getId());
						final Cell desigCell = xlsRow.getCell(desigColumn);
						if (desigCell == null) {
							// throw an error
							throw new WorkbookParserException("error.workbook.import.designation.empty.cell");
						}
						final String newDesig = desigCell.getStringCellValue().trim();
						final String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
						final String entryNumber = wRow.getMeasurementDataValue(TermId.ENTRY_NO.getId());
						String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
						if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
							plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
						}

						if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
							final List<Integer> newGids = this.fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
							if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
								final MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
								wData.setValue(newDesig);
							} else {
								final int index = observations.indexOf(wRow);
								final GermplasmChangeDetail changeDetail =
										new GermplasmChangeDetail(index, originalDesig, originalGid, newDesig, "", trialInstanceNumber,
												entryNumber, plotNumber);
								if (newGids != null && !newGids.isEmpty()) {
									changeDetail.setMatchingGids(newGids);
								}
								changeDetailsList.add(changeDetail);
							}
						}

						for (int j = 0; j <= lastXlsColIndex; j++) {
							final Cell headerCell = headerRow.getCell(j);
							if (headerCell != null) {
								final MeasurementData wData = wRow.getMeasurementData(headerCell.getStringCellValue());
								this.importDataCellValues(wData, xlsRow, j, workbook, factorVariableMap);
							}
						}
					}
				}
			}
			if (!rowsMap.isEmpty()) {
				// meaning there are items in the original list, so there are items deleted
				throw new WorkbookParserException("confirmation.import.add.or.delete.rows");
			}

		}
	}

	protected String getColumnIndexesFromXlsSheet(final Sheet observationSheet, final List<MeasurementVariable> variables,
			final String trialInstanceNumber) throws WorkbookParserException {
		String plotLabel = null, entryLabel = null;
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = variable.getName();
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = variable.getName();
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

	private String getKeyIdentifierFromXlsRow(final Row xlsRow, final String indexes) throws WorkbookParserException {
		if (indexes != null) {
			final String[] indexArray = indexes.split(",");
			// plot no
			final Cell plotCell = xlsRow.getCell(Integer.valueOf(indexArray[1]));
			// entry no
			final Cell entryCell = xlsRow.getCell(Integer.valueOf(indexArray[2]));

			if (plotCell == null) {
				throw new WorkbookParserException("error.workbook.import.plot.no.empty.cell");
			} else if (entryCell == null) {
				throw new WorkbookParserException("error.workbook.import.entry.no.empty.cell");
			}

			return indexArray[0] + "-" + this.getRealNumericValue(plotCell) + "-" + this.getRealNumericValue(entryCell);
		}
		return null;
	}

	protected void importDataCellValues(final MeasurementData wData, final Row xlsRow, final int columnIndex,
			final org.generationcp.middleware.domain.etl.Workbook workbook, final Map<Integer, MeasurementVariable> factorVariableMap) {
		if (wData != null && wData.isEditable()) {
			final Cell cell = xlsRow.getCell(columnIndex);
			final String xlsValue;
			if (cell != null && this.hasCellValue(cell)) {
				if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
						&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {

					wData.setAccepted(false);

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
						xlsValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(tempVal,
										wData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell.getStringCellValue();
						xlsValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(), wData.getMeasurementVariable()
										.getPossibleValues(), true);
					}
//					final Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : 0;
//					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
//						workbook.setHasExistingDataOverwrite(true);
//						System.err.println("WRITE THE MEASUREMENTS");
//					}

					if (wData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() && !xlsValue.equals(tempVal)) {
						wData.setcValueId(xlsValue);
						workbook.setHasExistingDataOverwrite(true);
					} else {
						wData.setcValueId(null);
					}

				} else {

					if (wData.getMeasurementVariable() != null
							&& wData.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
						wData.setAccepted(false);
					}
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						xlsValue = this.getRealNumericValue(cell);
					} else {
						xlsValue = cell.getStringCellValue();
					}
//					final Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : 0;
//					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
//						workbook.setHasExistingDataOverwrite(true);
//						System.err.println("WRITE THE MEASUREMENTS");
//					}
				}
				
				if(!wData.getValue().equals(xlsValue)){
				wData.setValue(xlsValue);
                wData.setOldValue(xlsValue);
                workbook.setHasExistingDataOverwrite(true);
				}
			}
		}
	}

	private String getRealNumericValue(final Cell cell) {
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
}
