
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
		final List<String> measurementHeaders = this.getMeasurementHeaders(workbook);
		final List<String> headers = parseObservationData().get(0);
		for (int i = 0; i < headers.size(); i++) {
			final String header = headers.get(i);
			if (header.equals(KsuFieldbookUtil.PLOT) || measurementHeaders.contains(header)) {
				continue;
			}

			final Set<StandardVariable> standardVariables =
					ontologyDataManager.findStandardVariablesByNameOrSynonym(header, contextUtil.getCurrentProgramUUID());
			Boolean found = false;
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
		parsedData.put(0, headers);
	}



	@Override
	protected Map<Integer, List<String>> parseObservationData() throws IOException {
        try {
            return csvFileParser.parseFile(currentFile);
        } catch (final FileParsingException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
	protected void performStudyDataImport(final Set<ChangeType> modes, final Map<Integer, List<String>> parsedData,
		final Map<String, MeasurementRow> measurementRowsMap, final String trialInstanceNumber,
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
			int countPlotIdNotFound = 0;
			final List<String> headerRowData = parsedData.get(0);
			final List<Integer> indexes = this.getColumnIndexesFromObservation(parsedData, variablesFactors, trialInstanceNumber);
			final Integer desigIndex =
				this.findIndexOfColumn(headerRowData, this.getColumnLabel(variablesFactors, TermId.DESIG.getId())).get(0);

			for (int i = 1; i < parsedData.size(); i++) {
				final List<String> rowData = parsedData.get(i);
				final String key = this.getKeyIdentifierFromRow(rowData, indexes);
				final MeasurementRow measurementRow = measurementRowsMap.get(key);

				if (measurementRow == null) {
					countPlotIdNotFound++;
					continue;
				}

				measurementRowsMap.remove(key);

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

			if (!measurementRowsMap.isEmpty()) {
				// meaning there are items in the original list, so there are items deleted
				throw new WorkbookParserException("confirmation.import.add.or.delete.rows");
			}

			if (countPlotIdNotFound != 0) {
				workbook.setPlotsIdNotfound(countPlotIdNotFound);
			}
		}
	}

	private void validateAndSetNewDesignation(final Integer desigIndex, final List<String> rowData, final MeasurementRow measurementRow) {
		final String originalDesig = measurementRow.getMeasurementDataValue(TermId.DESIG.getId());
		final String newDesig = rowData.get(desigIndex);
		final String originalGid = measurementRow.getMeasurementDataValue(TermId.GID.getId());

		if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
			final List<Integer> newGids = this.getGermplasmIdsByName(newDesig);
			if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
				final MeasurementData wData = measurementRow.getMeasurementData(TermId.DESIG.getId());
				wData.setValue(newDesig);
			}
		}
	}

	protected List<Integer> getColumnIndexesFromObservation(final Map<Integer, List<String>> csvMap,
			final List<MeasurementVariable> variables, final String trialInstanceNumber) throws WorkbookParserException {
		String plotLabel = null, entryLabel = null, plot_label = null;

		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = this.getLabelFromRequiredColumn(variable);
				continue;
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = this.getLabelFromRequiredColumn(variable);
				continue;
			} else if (variable.getTermId() == TermId.PLOT_ID.getId()) {
				plot_label = this.getLabelFromRequiredColumn(variable);
				continue;
			}
		}

		if (plotLabel != null && entryLabel != null && plot_label != null) {
			final List<Integer> indexes = this.findIndexOfColumn(csvMap.get(0), plotLabel, entryLabel,plot_label);
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

	String getKeyIdentifierFromRow(final List<String> row, final List<Integer> indexes) throws WorkbookParserException{
		final String plot = row.get(indexes.get(1));
		final String entry = row.get(indexes.get(2));
		final String plotId = row.get(indexes.get(3));

		if (null == plot) {
			throw new WorkbookParserException("error.workbook.import.plot.no.empty.cell");
		} else if (null == entry) {
			throw new WorkbookParserException("error.workbook.import.entry.no.empty.cell");
		} else if (null == plotId) {
			throw new WorkbookParserException("error.workbook.import.plot.id.empty.cell");
		}

		return plotId;
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
            workbook.setHasExistingDataOverwrite(true);
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
			final Integer intVal = doubleVal.intValue();
			// trim zeroes
			if (doubleVal == Math.ceil(doubleVal)) {
				realValue = intVal.toString();
			} else {
				realValue = doubleVal.toString();
			}
		}

		return realValue;
	}

    private List<Integer> getGermplasmIdsByName(final String newDesig) {
        return this.fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
    }

	@Override
	protected void detectAddedTraitsAndPerformRename(Set<ChangeType> modes, List<String> addedVariates,
			List<String> removedVariates) throws IOException {
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
