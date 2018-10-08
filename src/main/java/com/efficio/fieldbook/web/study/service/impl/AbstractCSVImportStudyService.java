
package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.GenericCsvFileProcessor;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractCSVImportStudyService extends AbstractImportStudyService<Map<Integer, List<String>>> {

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private GenericCsvFileProcessor csvFileParser;

	@Resource
	private ContextUtil contextUtil;

	public AbstractCSVImportStudyService(final Workbook workbook, final String currentFile, final String originalFileName) {
		super(workbook, currentFile, originalFileName);
	}

	@Override
	void validateImportMetadata() throws WorkbookParserException {
		// intentionally blank. CSV files do not contain import metadata (variable information, etc)
	}

	protected abstract String getLabelFromRequiredColumn(MeasurementVariable variable);

	@Override
	protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes) throws IOException {
		final List<String> measurementHeaders = this.getMeasurementHeaders(this.workbook);
		final List<String> headers = this.parseObservationData().get(0);
		for (int i = 0; i < headers.size(); i++) {
			final String header = headers.get(i);
			if (header.equals(KsuFieldbookUtil.PLOT) || measurementHeaders.contains(header)) {
				continue;
			}

			final Set<StandardVariable> standardVariables =
				this.ontologyDataManager.findStandardVariablesByNameOrSynonym(header, this.contextUtil.getCurrentProgramUUID());
			boolean found = false;
			for (final StandardVariable standardVariable : standardVariables) {
				if (measurementHeaders.contains(standardVariable.getName())) {
					headers.set(i, standardVariable.getName());
					found = true;
					break;
				}
			}

			if (!found) {
				modes.add(ChangeType.ADDED_TRAITS);
			}

		}
		this.parsedData.put(0, headers);
	}



	@Override
	protected Map<Integer, List<String>> parseObservationData() throws IOException {
        try {
            return this.csvFileParser.parseFile(this.currentFile);
        } catch (final FileParsingException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
	protected void performStudyDataImport(final Set<ChangeType> modes, final Map<Integer, List<String>> parsedData,
		final Map<String, MeasurementRow> measurementRowsMap,
		final List<GermplasmChangeDetail> changeDetailsList, final Workbook workbook) throws WorkbookParserException {

		final List<MeasurementVariable> variablesFactors = workbook.getFactors();
		// setup factor map
		final Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<>();
		for (final MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (!Objects.equals(measurementRowsMap, null) && !measurementRowsMap.isEmpty()) {
			workbook.setHasExistingDataOverwrite(false);
			workbook.setPlotsIdNotfound(0);
			int countObsUnitIdNotFound = 0;
			final List<String> headerRowData = parsedData.get(0);
			final Integer obsUnitIdIndex = this.getIndexOfObsUnitIdFromObservation(parsedData, variablesFactors);
			final Integer desigIndex =
				this.findIndexOfColumn(headerRowData, this.getColumnLabel(variablesFactors, TermId.DESIG.getId())).get(0);

			for (int i = 1; i < parsedData.size(); i++) {
				final List<String> rowData = parsedData.get(i);
				final String obsUnitId = this.getObsUnitIdFromRow(rowData, obsUnitIdIndex);
				final MeasurementRow measurementRow = measurementRowsMap.get(obsUnitId);

				if (measurementRow == null) {
					countObsUnitIdNotFound++;
					continue;
				}

				measurementRowsMap.remove(obsUnitId);

				if (desigIndex == null) {
					throw new WorkbookParserException("error.workbook.import.designation.empty.cell");
				}

				this.validateAndSetNewDesignation(desigIndex, rowData, measurementRow);

				for (int j = 0; j < headerRowData.size(); j++) {
					final String header = headerRowData.get(j);
					final MeasurementData wData = measurementRow.getMeasurementData(header);
					this.importDataCellValues(wData, rowData, j, workbook, factorVariableMap);
				}
			}

			if (countObsUnitIdNotFound != 0) {
				workbook.setPlotsIdNotfound(countObsUnitIdNotFound);
			}
		}
	}

	private void validateAndSetNewDesignation(final Integer desigIndex, final List<String> rowData, final MeasurementRow measurementRow) {
		final String newDesig = rowData.get(desigIndex);

		this.setNewDesignation(measurementRow, newDesig);
	}

	protected Integer getIndexOfObsUnitIdFromObservation(final Map<Integer, List<String>> csvMap,
			final List<MeasurementVariable> variables) throws WorkbookParserException {

		String obsUnitIdLabel = null;

		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.OBS_UNIT_ID.getId()) {
				obsUnitIdLabel = this.getLabelFromRequiredColumn(variable);
				break;
			}
		}

		final List<Integer> indexes = this.findIndexOfColumn(csvMap.get(0), obsUnitIdLabel);
		if (indexes.size() == 0 || indexes.get(0) == -1) {
			throw new WorkbookParserException("error.workbook.import.plot.id.empty.cell");
		}
		return indexes.get(0);
	}

	protected List<Integer> findIndexOfColumn(final List<String> headers, final String... cellValue) {
		final List<Integer> results = new ArrayList<>();

		for (final String aCellValue : cellValue) {
			results.add(headers.indexOf(aCellValue));
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

	String getObsUnitIdFromRow(final List<String> row, final Integer index) throws WorkbookParserException{
		final String obsUnitId = row.get(index);

		if (StringUtils.isBlank(obsUnitId)) {
			throw new WorkbookParserException("error.workbook.import.plot.id.empty.cell");
		}

		return obsUnitId;
	}

	protected void importDataCellValues(final MeasurementData wData, final List<String> row, final int columnIndex,
			final Workbook workbook, final Map<Integer, MeasurementVariable> factorVariableMap) {
		if (wData != null && wData.isEditable()) {
			final String cell = row.get(columnIndex);
			String csvValue = "";
			if (StringUtils.isEmpty(cell)) {
				return;
			}
			if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
					&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {

				csvValue = this.processDataCellCategoricalValue(wData, cell, factorVariableMap);

			} else {

				if (TermId.NUMERIC_VARIABLE.getId() == wData.getMeasurementVariable().getDataTypeId()) {
					wData.setAccepted(false);
				}
				if (NumberUtils.isNumber(cell)) {
					csvValue = this.getRealNumericValue(cell);
				} else {
					csvValue = cell;
				}

				final Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
				if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
					workbook.setHasExistingDataOverwrite(true);
				}
			}
			wData.setValue(csvValue);

            wData.setOldValue(csvValue);

		}
	}

    protected String processDataCellCategoricalValue(final MeasurementData wData, final String cell, final Map<Integer, MeasurementVariable> factorVariableMap) {
        wData.setAccepted(false);

        String tempVal = "";
        final String csvValue;

        if (NumberUtils.isNumber(cell)) {
            tempVal = this.getRealNumericValue(cell);
            csvValue = ExportImportStudyUtil.getCategoricalIdCellValue(tempVal, wData.getMeasurementVariable().getPossibleValues(),
                            true);
        } else {
            tempVal = cell;
            csvValue = ExportImportStudyUtil.getCategoricalIdCellValue(cell, wData.getMeasurementVariable().getPossibleValues(), true);
        }

        final Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
        if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
			this.workbook.setHasExistingDataOverwrite(true);
        }

        if (TermId.CATEGORICAL_VARIABLE.getId() == wData.getMeasurementVariable().getDataTypeId() && !csvValue.equals(tempVal)) {
            wData.setcValueId(csvValue);
        } else {
            wData.setcValueId(null);
        }

        return csvValue;
    }

	String getRealNumericValue(final String cell) {
		// to remove trailing zeroes
		String realValue = "";

		if (NumberUtils.isNumber(cell)) {
			final Double doubleVal = NumberUtils.createDouble(cell);
			final int intVal = doubleVal.intValue();
			// trim zeroes
			if (doubleVal == Math.ceil(doubleVal)) {
				realValue = Integer.toString(intVal);
			} else {
				realValue = doubleVal.toString();
			}
		}

		return realValue;
	}

	@Override
	protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes, final List<String> addedVariates,
			final List<String> removedVariates) throws IOException {
		this.detectAddedTraitsAndPerformRename(modes);

	}

	private String getPlotNo(final MeasurementRow wRow) {
		String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
		if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
			plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
		}
		return plotNumber;
	}

}
