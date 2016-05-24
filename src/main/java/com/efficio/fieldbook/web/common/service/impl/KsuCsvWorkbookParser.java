
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.AbstractCsvFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

class KsuCsvWorkbookParser extends AbstractCsvFileParser<KsuCsvWorkbookParser> {

	private final KsuCsvImportStudyServiceImpl ksuCsvImportStudyService;
	private final Workbook workbook;
	private final String trialInstanceNo;
	private final Map<String, MeasurementRow> rowsMap;
	private List<GermplasmChangeDetail> changeDetailsList;
	private Set<ChangeType> modes;

	public KsuCsvWorkbookParser(final KsuCsvImportStudyServiceImpl ksuCsvImportStudyService, final Workbook workbook,
			final String trialInstanceNo, final Map<String, MeasurementRow> rowsMap) {
		this.ksuCsvImportStudyService = ksuCsvImportStudyService;
		this.workbook = workbook;
		this.trialInstanceNo = trialInstanceNo;
		this.rowsMap = rowsMap;
	}

	@Override
	public KsuCsvWorkbookParser parseCsvMap(final Map<Integer, List<String>> csvMap) throws FileParsingException {
		// validate headers
		final String[] rowHeaders = csvMap.get(0).toArray(new String[csvMap.get(0).size()]);

		if (!this.ksuCsvImportStudyService.isValidHeaderNames(rowHeaders)) {
			throw new FileParsingException("error.workbook.import.requiredColumnsMissing");
		}

		// this does the big parsing import task
		this.importDataToWorkbook(csvMap, this.workbook, this.trialInstanceNo, this.rowsMap);

		return this;
	}

	protected void importDataToWorkbook(final Map<Integer, List<String>> csvMap, final Workbook workbook, final String trialInstanceNo,
			final Map<String, MeasurementRow> rowsMap) throws FileParsingException {
		this.modes = new HashSet<>();
		this.changeDetailsList = new ArrayList<>();

		final List<MeasurementVariable> variablesFactors = workbook.getFactors();
		final List<MeasurementRow> observations = workbook.getObservations();

		// setup factor map
		final Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<Integer, MeasurementVariable>();
		for (final MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (!Objects.equals(rowsMap, null) && !rowsMap.isEmpty()) {
			workbook.setHasExistingDataOverwrite(false);
			final List<String> headerRow = csvMap.get(0);
			final List<Integer> indexes = this.getColumnIndexesFromObservation(csvMap, variablesFactors, trialInstanceNo);
			final Integer desigIndex =
					this.findIndexOfColumn(headerRow, this.getColumnLabel(variablesFactors, TermId.DESIG.getId())).get(0);

			for (int i = 1; i < csvMap.size(); i++) {
				final List<String> row = csvMap.get(i);
				final String key = this.getKeyIdentifierFromRow(row, indexes);

				final MeasurementRow wRow = rowsMap.get(key);
				if (wRow == null) {
					this.modes.add(ChangeType.ADDED_ROWS);
				} else {
					rowsMap.remove(key);

					if (desigIndex == null) {
						throw new FileParsingException("error.workbook.import.designation.empty.cell");
					}

					final String originalDesig = wRow.getMeasurementDataValue(TermId.DESIG.getId());
					final String newDesig = row.get(desigIndex);

					final String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
					final String entryNumber = wRow.getMeasurementDataValue(TermId.ENTRY_NO.getId());
					String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
					if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
						plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
					}

					if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
						final List<Integer> newGids =
								this.ksuCsvImportStudyService.fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
						if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
							final MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
							wData.setValue(newDesig);
						} else {
							final int index = observations.indexOf(wRow);
							final GermplasmChangeDetail changeDetail =
									new GermplasmChangeDetail(index, originalDesig, originalGid, newDesig, "", trialInstanceNo,
											entryNumber, plotNumber);
							if (newGids != null && !newGids.isEmpty()) {
								changeDetail.setMatchingGids(newGids);
							}
							this.changeDetailsList.add(changeDetail);
						}
					}

					for (int j = 0; j < headerRow.size(); j++) {
						final String headerCell = headerRow.get(j);
						final MeasurementData wData = wRow.getMeasurementData(headerCell);
						this.importDataCellValues(wData, row, j, workbook, factorVariableMap);
					}

				}
			}

			if (!rowsMap.isEmpty()) {
				this.modes.add(ChangeType.DELETED_ROWS);
			}

		}
	}

	protected List<Integer> getColumnIndexesFromObservation(final Map<Integer, List<String>> csvMap,
			final List<MeasurementVariable> variables, final String trialInstanceNumber) throws FileParsingException {
		String plotLabel = null, entryLabel = null;
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = this.ksuCsvImportStudyService.getLabelFromKsuRequiredColumn(variable);
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = this.ksuCsvImportStudyService.getLabelFromKsuRequiredColumn(variable);
			}
		}
		if (plotLabel != null && entryLabel != null) {
			final List<Integer> indexes = this.findIndexOfColumn(csvMap.get(0), plotLabel, entryLabel);
			indexes.add(0, NumberUtils.createInteger(trialInstanceNumber));

			for (final int index : indexes) {
				if (index == -1) {
					return new ArrayList<>();
				}
			}

			return indexes;
		}
		return new ArrayList<>();
	}

	protected List<Integer> findIndexOfColumn(final List<String> headers, final String... cellValue) throws FileParsingException {
		final List<Integer> results = new ArrayList<>();

		for (int i = 0; i < cellValue.length; i++) {
			results.add(headers.indexOf(cellValue[i]));
		}

		return results;
	}

	private String getColumnLabel(final List<MeasurementVariable> variables, final int termId) {
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == termId) {
				return variable.getName();
			}
		}
		return "";
	}

	private String getKeyIdentifierFromRow(final List<String> row, final List<Integer> indexes) throws FileParsingException {
		final String plot = row.get(indexes.get(1));
		final String entry = row.get(indexes.get(2));

		if (plot == null) {
			throw new FileParsingException("error.workbook.import.plot.no.empty.cell");
		} else if (entry == null) {
			throw new FileParsingException("error.workbook.import.entry.no.empty.cell");
		}

		return indexes.get(0) + "-" + plot + "-" + entry;
	}

	protected void importDataCellValues(final MeasurementData wData, final List<String> row, final int columnIndex,
			final Workbook workbook, final Map<Integer, MeasurementVariable> factorVariableMap) {
		if (wData != null && wData.isEditable()) {
			final String cell = row.get(columnIndex);
			String csvValue = "";
			if (StringUtils.isNotEmpty(cell)) {
				if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
						&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {

					wData.setAccepted(false);

					String tempVal = "";

					if (NumberUtils.isNumber(cell)) {
						final double doubleVal = Double.valueOf(cell);
						final double intVal = Double.valueOf(cell).intValue();
						boolean getDoubleVal = false;
						if (doubleVal - intVal > 0) {
							getDoubleVal = true;
						}

						tempVal = String.valueOf(Double.valueOf(cell).intValue());
						if (getDoubleVal) {
							tempVal = String.valueOf(Double.valueOf(cell));
						}
						csvValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(tempVal,
										wData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell;
						csvValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(cell, wData.getMeasurementVariable().getPossibleValues(),
										true);
					}
					final Integer termId =
							wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
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

					final Integer termId =
							wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
						workbook.setHasExistingDataOverwrite(true);
					}
				}
				wData.setValue(csvValue);
				// Keep the imported value so that when the value is set to "missing"
				// we can still track the old value.
				wData.setOldValue(csvValue);
			}
		}
	}

	protected String getRealNumericValue(final String cell) {
		String realValue = "";

		if (NumberUtils.isNumber(cell)) {
			final Double doubleVal = NumberUtils.createDouble(cell);

			if (doubleVal.doubleValue() == doubleVal.intValue()) {
				realValue = Integer.toString(doubleVal.intValue());
			} else {
				realValue = doubleVal.toString();
			}
		}

		return realValue;
	}

}
