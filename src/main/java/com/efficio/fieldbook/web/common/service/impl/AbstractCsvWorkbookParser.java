package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.AbstractCsvFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

abstract class AbstractCsvWorkbookParser<T> extends AbstractCsvFileParser<T> {

	public AbstractCsvWorkbookParser() {
	}

	@Override
	public	abstract T parseCsvMap(Map<Integer, List<String>> csvMap) throws FileParsingException;

	public abstract List<Integer> getGermplasmIdsByName(String newDesig);
	
	public abstract String getLabelFromRequiredColumn(MeasurementVariable variable);
	
	protected void importDataToWorkbook(Map<Integer, List<String>> csvMap, Workbook workbook, String trialInstanceNo, Map<String, MeasurementRow> rowsMap)
			throws FileParsingException {
		List<MeasurementVariable> variablesFactors = workbook.getFactors();

		// setup factor map
		Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<Integer, MeasurementVariable>();
		for (MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (!Objects.equals(rowsMap, null) && !rowsMap.isEmpty()) {
			workbook.setHasExistingDataOverwrite(false);
			List<String> headerRow = csvMap.get(0);
			List<Integer> indexes = this.getColumnIndexesFromObservation(csvMap, variablesFactors, trialInstanceNo);
			Integer desigIndex = this.findIndexOfColumn(headerRow, this.getColumnLabel(variablesFactors, TermId.DESIG.getId())).get(0);

			for (int i = 1; i < csvMap.size(); i++) {
				List<String> row = csvMap.get(i);
				String key = this.getKeyIdentifierFromRow(row, indexes);

				MeasurementRow wRow = rowsMap.get(key);
				if (wRow != null) {
					rowsMap.remove(key);

					if (desigIndex == null) {
						throw new FileParsingException("error.workbook.import.designation.empty.cell");
					}

					String originalDesig = wRow.getMeasurementDataValue(TermId.DESIG.getId());
					String newDesig = row.get(desigIndex);

					String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
					String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
					if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
						plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
					}

					if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
						List<Integer> newGids = this.getGermplasmIdsByName(newDesig);
						if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
							MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
							wData.setValue(newDesig);
						}					
					}

					for (int j = 0; j < headerRow.size(); j++) {
						String headerCell = headerRow.get(j);
						MeasurementData wData = wRow.getMeasurementData(headerCell);
						this.importDataCellValues(wData, row, j, workbook, factorVariableMap);
					}

				}
			}

		}
	}

	protected List<Integer> getColumnIndexesFromObservation(Map<Integer, List<String>> csvMap, List<MeasurementVariable> variables, String trialInstanceNumber)
			throws FileParsingException {
		String plotLabel = null, entryLabel = null;
		for (MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = this.getLabelFromRequiredColumn(variable);
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = this.getLabelFromRequiredColumn(variable);
			}
		}
		if (plotLabel != null && entryLabel != null) {
			List<Integer> indexes = this.findIndexOfColumn(csvMap.get(0), plotLabel, entryLabel);
			indexes.add(0,NumberUtils.createInteger(trialInstanceNumber));

			for (int index : indexes) {
				if (index == -1) {
					return new ArrayList<>();
				}
			}

			return indexes;
		}
		return new ArrayList<>();
	}

	protected List<Integer> findIndexOfColumn(List<String> headers, String... cellValue) throws FileParsingException {
		List<Integer> results = new ArrayList<>();

		for (int i = 0; i < cellValue.length; i++) {
			results.add(headers.indexOf(cellValue[i]));
		}

		return results;
	}

	private String getColumnLabel(List<MeasurementVariable> variables, int termId) {
		for (MeasurementVariable variable : variables) {
			if (variable.getTermId() == termId) {
				return variable.getName();
			}
		}
		return "";
	}

	private String getKeyIdentifierFromRow(List<String> row, List<Integer> indexes) throws FileParsingException {
		String plot = row.get(indexes.get(1));
		String entry = row.get(indexes.get(2));

		if (plot == null || StringUtils.isEmpty(plot)) {
			throw new FileParsingException("error.workbook.import.plot.no.empty.cell");
		} else if (entry == null || StringUtils.isEmpty(entry)) {
			throw new FileParsingException("error.workbook.import.entry.no.empty.cell");
		}

		return indexes.get(0) + "-" + (int)Float.parseFloat(plot) + "-" + (int)Float.parseFloat(entry);
	}

	protected void importDataCellValues(MeasurementData wData, List<String> row, int columnIndex, Workbook workbook,
			Map<Integer, MeasurementVariable> factorVariableMap) {
		if (wData != null && wData.isEditable()) {
			String cell = row.get(columnIndex);
			String csvValue = "";
			if (StringUtils.isNotEmpty(cell)) {
				if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null && !wData
						.getMeasurementVariable().getPossibleValues().isEmpty()) {

					wData.setAccepted(false);

					String tempVal = "";

					if (NumberUtils.isNumber(cell)) {
						double doubleVal = Double.valueOf(cell);
						double intVal = Double.valueOf(cell).intValue();
						boolean getDoubleVal = false;
						if (doubleVal - intVal > 0) {
							getDoubleVal = true;
						}

						tempVal = String.valueOf(Double.valueOf(cell).intValue());
						if (getDoubleVal) {
							tempVal = String.valueOf(Double.valueOf(cell));
						}
						csvValue = ExportImportStudyUtil
								.getCategoricalIdCellValue(tempVal, wData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell;
						csvValue = ExportImportStudyUtil
								.getCategoricalIdCellValue(cell, wData.getMeasurementVariable().getPossibleValues(), true);
					}
					Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
						workbook.setHasExistingDataOverwrite(true);
					}

					if (TermId.CATEGORICAL_VARIABLE.getId() == wData.getMeasurementVariable().getDataTypeId() && !csvValue.equals(tempVal)) {
						wData.setcValueId(csvValue);
					} else {
						wData.setcValueId(null);
					}

				} else {

					if (TermId.NUMERIC_VARIABLE.getId() == wData.getMeasurementVariable().getDataTypeId()) {
						wData.setAccepted(false);
					}
					if (NumberUtils.isNumber(cell)) {
						csvValue = this.getRealNumericValue(cell);
					} else {
						csvValue = cell;
					}

					Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
						workbook.setHasExistingDataOverwrite(true);
					}
				}
				wData.setValue(csvValue);
			}
		}
	}

	private String getRealNumericValue(String cell) {
		String realValue = "";

		if (NumberUtils.isNumber(cell)) {
			Double doubleVal = NumberUtils.createDouble(cell);
			Integer intVal = doubleVal.intValue();
			//trim zeroes
			if (doubleVal ==  Math.ceil(doubleVal)) {
				realValue = intVal.toString();
			} else {
				realValue = doubleVal.toString();
			}
		}

		return realValue;
	}
}
